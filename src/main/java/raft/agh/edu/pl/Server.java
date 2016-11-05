package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Component
public class Server {

    private final Logger logger = LoggerFactory.getLogger(Server.class);

    private State state = new State();
    private LeaderState leaderState;
    private HashMap<String, String> store = new HashMap<>();
    @Value("${servers}")
    private List<String> servers;
    @Value("${selfId}")
    private String selfId;
    @Autowired
    private TimerService timer;
    @Autowired
    private CommunicationService communication;
    @Autowired
    private CompletionService<TermStore> completionService;

    @PostConstruct
    private void start() {
        if(servers == null || selfId == null) {
            logger.error("Configuration error. servers and selfId cannot be null");
            throw new ServiceConfigurationError("Configuration error. servers and selfId cannot be null");
        }
        timer.setElectionCallback(this::convertToCandidate);
        timer.setHeartBeatCallback(this::heartBeat);
        timer.setLeaderCallback(this::loop);
        logger.info("server {} started",selfId);
    }


    public synchronized AppendResult appendEntries(AppendRequest request) {
        if(shouldConvertToFollower(request))
            convertToFollower(request);
        state.setLeaderId(request.getLeaderId());

        if (request.getTerm() < state.getCurrentTerm() || state.getLog().get(request.getPrevLogIndex()).getTerm() != request.getPrevLogTerm())
            return new AppendResult(state.getCurrentTerm(), false);
        if(request.isHeartBeat())
            return new AppendResult(state.getCurrentTerm(),true);

        if (state.getLastLogIndex() > request.getPrevLogIndex() && request.getTerm() != state.getLog().get(request.getPrevLogIndex() + 1).getTerm()) {
            for (int i = state.getLastLogIndex(); i > request.getPrevLogIndex(); i--)
                state.getLog().remove(i);
        }
        if(state.getLastLogIndex() == request.getPrevLogIndex())
            state.getLog().add(new Entry(request.getPayload(), state.getCurrentTerm()));
        if (request.getLeaderCommit() > state.getCommitIndex())
            state.setCommitIndex(Math.min(state.getLastLogIndex(), request.getLeaderCommit()));
        storeCommitted();
        return new AppendResult(state.getCurrentTerm(), true);
    }

    public synchronized VoteResult requestVote(VoteRequest request) {
        if(shouldConvertToFollower(request))
            convertToFollower(request);
        if (request.getTerm() < state.getCurrentTerm())
            return new VoteResult(state.getCurrentTerm(), false);
        boolean candidateHasUpToDateLog = request.getLastLogTerm() > state.getLastLogTerm() || request.getLastLogTerm() == state.getLastLogTerm() && request.getLastLogIndex() >= state.getLastLogIndex();
        if (!request.getCandidateId().equals(state.getVoteFor()) || !candidateHasUpToDateLog)
            return new VoteResult(state.getCurrentTerm(), false);
        state.setVoteFor(request.getCandidateId());

        return new VoteResult(state.getCurrentTerm(), true);
    }


    private boolean shouldConvertToFollower(TermStore termStore){
        return termStore.getTerm() > state.getCurrentTerm();
    }
    private void convertToFollower(TermStore termStore) {
        state.setCurrentTerm(termStore.getTerm());
        state.setVoteFor(null);
        timer.resetElectionTimer();
        timer.stopHeartBeat();
        leaderState = null;
    }

    private synchronized void convertToLeader() {
        timer.startHeartBeat();
        leaderState = new LeaderState(servers, state.getLastLogIndex());

    }

    private void convertToCandidate() {
        state.incremmentCurrentTerm();
        state.setVoteFor(selfId);
        timer.resetElectionTimer();
        List<Future<TermStore>> futures = new LinkedList<>();
        for (String server : servers) {
            if (server.equals(selfId))
                continue;
            VoteRequest request = new VoteRequest(server, state.getCurrentTerm(), selfId, state.getLastLogIndex(), state.getLastLogTerm());
            futures.add(completionService.submit(() -> communication.requestVote(request)));
        }
        int waiting = servers.size() - 1;
        int votesForMe = 1;
        while (waiting > 0) {
            try {
                Future<TermStore> future = completionService.take();
                VoteResult voteResult = (VoteResult) future.get();
                if(shouldConvertToFollower(voteResult)){
                    futures.forEach(f -> f.cancel(true));
                    convertToFollower(voteResult);
                    return;
                }
                waiting--;
                votesForMe += voteResult.isVoteGranted() ? 1 : 0;
            } catch (InterruptedException | ExecutionException e) {
                return;
            }
        }

        if (votesForMe > servers.size() / 2)
            convertToLeader();
    }

    private synchronized void appendEntries(String server){
        int nextIndex = leaderState.getNextIndex(server);
        if(state.getLastLogIndex() >= nextIndex){
            int prevLogIndex = nextIndex -1;
            int prevLogTerm = state.getLog().get(prevLogIndex).getTerm();
            KeyValue keyValue = state.getLog().get(leaderState.getNextIndex(server)).getKeyValue();
            AppendRequest request = new AppendRequest(server, state.getCurrentTerm(), selfId, prevLogIndex, prevLogTerm, keyValue, state.getCommitIndex());
            AppendResult result = communication.appendEntries(request);
            if(shouldConvertToFollower(result)) {
                convertToFollower(result);
                //return
            }
            if(result.isSuccess()){
                leaderState.setMatchIndex(server,leaderState.getNextIndex(server));
                leaderState.incrementNextIndex(server);
            }else if(!result.isConnectionError()){
                leaderState.decrementNextIndex(server);
            }

        }
    }

    private synchronized void updateCommitIndex(){
        List<Integer> greater = leaderState.getMatchIndex().values().stream().filter(i -> i > state.getCommitIndex()).collect(Collectors.toList());
        if(greater.size() < servers.size()/2)
            return;
        int newCommitIndex = greater.stream().min(Integer::compare).get();
        if(state.getLog().get(newCommitIndex).getTerm() == state.getCurrentTerm())
            state.setCommitIndex(newCommitIndex);
    }

    private void loop(){
        while(true) {
            for (String server : servers) {
                if (server.equals(selfId))
                    continue;
                appendEntries(server);
            }
            updateCommitIndex();
        }
    }

    private void heartBeat() {
        List<Future<TermStore>> futures = new LinkedList<>();
        for (String server : servers) {
            if (server.equals(selfId))
                continue;
            AppendRequest request = new AppendRequest(server, state.getCurrentTerm(), selfId, state.getLastLogIndex(), state.getLastLogTerm(), null, state.getCommitIndex());
            futures.add(completionService.submit(() -> communication.appendEntries(request)));
        }
        int waiting = servers.size() - 1;
        while(waiting > 0){
            try {
                Future<TermStore> future = completionService.take();

                if(shouldConvertToFollower(future.get())){
                    futures.forEach(f -> f.cancel(true));
                    convertToFollower(future.get());
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
               return;
            }
        }

    }

    private void storeCommitted() {
        if (state.getCommitIndex() <= state.getLastApplied())
            return;
        state.incrementeLastApplied();
        KeyValue toCommit = state.getLog().get(state.getLastApplied()).getKeyValue();
        store.put(toCommit.getKey(), toCommit.getValue());
    }

    public synchronized StoreResult store(String key, String value) {
        if (!isLeader())
            return StoreResult.redirect(state.getLeaderId());
        state.getLog().add(new Entry(new KeyValue(key, value), state.getCurrentTerm()));
        return StoreResult.success();
    }


    public synchronized String retrieve(String key) {
        return store.get(key);
    }

    private boolean isLeader() {
        return leaderState != null;
    }
}
