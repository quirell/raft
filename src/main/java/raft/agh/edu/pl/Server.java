package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
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
    private List<String> otherServers;
    @Autowired
    private TimerService timer;
    @Autowired
    private CommunicationService communication;
    @Autowired
    private RequestExecutorService requestService;
    @Value("${logHeartBeat}")
    private boolean logHeartBeat;

    private volatile boolean connected;

    private volatile boolean electionInProgress;

    @PostConstruct
    private void start() {
        otherServers = servers.stream().filter(s -> !s.equals(selfId)).collect(Collectors.toList());
        timer.setElectionCallback(this::convertToCandidate);
        timer.setHeartBeatCallback(this::heartBeat);
        timer.setReplicationCallback(this::replicate);
        logger.info("initial election scheduled");
        timer.startElectionTimer();
        logger.info("server {} started", selfId);
        logger.info("servers in cluster: {}", servers.stream().collect(Collectors.joining(",")));
        connected = true;

    }


    public synchronized AppendResult appendEntries(AppendRequest request) {
        timer.resetElectionTimer();
        if (shouldConvertToFollower(request))
            convertToFollower(request);
        state.setLeaderId(request.getLeaderId());
        if (request.isHeartBeat()) {
            if (logHeartBeat)
                logger.info("heartBeat received");
        } else {
            logger.info("append Entries requested");
            if (isLeader() || request.getTerm() < state.getCurrentTerm() || state.getLog().get(request.getPrevLogIndex()).getTerm() != request.getPrevLogTerm()) {
                logger.info("Append Failed: is leader: {} || requestTerm({}) < currentTerm({}) = {} || myPrevLogTerm({}) != leaderPrevLogTerm({}) = {}", isLeader(), request.getTerm(), state.getCurrentTerm(), request.getTerm() < state.getCurrentTerm(), state.getLog().get(request.getPrevLogIndex()).getTerm(), request.getPrevLogTerm(), state.getLog().get(request.getPrevLogIndex()).getTerm() != request.getPrevLogTerm());
                return new AppendResult(state.getCurrentTerm(), false);
            }

            if (state.getLastLogIndex() > request.getPrevLogIndex() && request.getTerm() != state.getLog().get(request.getPrevLogIndex() + 1).getTerm()) {
                logger.info("Removing outdated entries: lastLogIndex({}) > requestPrevLogIndex({}) = {} && requestTerm({}) != myTerm(requestPrevLogIndex + 1)({}) = {}", state.getLastLogIndex(), request.getPrevLogIndex(), state.getLastLogIndex() > request.getPrevLogIndex(), request.getTerm(), state.getLog().get(request.getPrevLogIndex() + 1).getTerm(), request.getTerm() != state.getLog().get(request.getPrevLogIndex() + 1).getTerm());
                for (int i = state.getLastLogIndex(); i > request.getPrevLogIndex(); i--)
                    state.getLog().remove(i);
                logger.info("After pruning lastLogIndex {} == prevLogIndex {}", state.getLastLogIndex(), request.getPrevLogIndex());
            }
            if (state.getLastLogIndex() == request.getPrevLogIndex()) {
                state.getLog().add(new Entry(request.getPayload(), state.getCurrentTerm()));
                logger.info("appended To log ({},{}) in term {}", request.getPayload().getKey(), request.getPayload().getValue(), state.getCurrentTerm());
            }
        }
        if (request.getLeaderCommit() > state.getCommitIndex()) {
            state.setCommitIndex(Math.min(state.getLastLogIndex(), request.getLeaderCommit()));
            logger.info("Commit Index set to min ({},{})", state.getCommitIndex(), request.getLeaderCommit());
        }
        storeCommitted();
        return new AppendResult(state.getCurrentTerm(), true);
    }

    public synchronized VoteResult requestVote(VoteRequest request) {
        logger.info("vote requested");
        timer.resetElectionTimer();
        if (shouldConvertToFollower(request))
            convertToFollower(request);
        if (isLeader() || request.getTerm() < state.getCurrentTerm()) {
            logger.info("vote for {} not granted, term lower than mine", request.getCandidateId());
            return new VoteResult(state.getCurrentTerm(), false);
        }
        boolean candidateHasUpToDateLog = request.getLastLogTerm() > state.getLastLogTerm() || request.getLastLogTerm() == state.getLastLogTerm() && request.getLastLogIndex() >= state.getLastLogIndex();
        if (state.getVoteFor() != null || !candidateHasUpToDateLog) {
            logger.info("vote for {} not granted, upToDateLog {},voteFor {}", request.getCandidateId(), candidateHasUpToDateLog, state.getVoteFor());
            return new VoteResult(state.getCurrentTerm(), false);
        }
        state.setVoteFor(request.getCandidateId());

        return new VoteResult(state.getCurrentTerm(), true);
    }


    private boolean shouldConvertToFollower(TermStore termStore) {
        return termStore.getTerm() > state.getCurrentTerm();
    }

    private void convertToFollower(TermStore termStore) {
        logger.info("back to follower (term: {})", termStore.getTerm());
        electionInProgress = false;
        state.setCurrentTerm(termStore.getTerm());
        state.setVoteFor(null);
        leaderState = null;
        timer.stopHeartBeat();
        timer.stopReplication();
        timer.resetElectionTimer();
    }

    private void convertToLeader() {
        timer.stopElectionTimer();
        electionInProgress = false;
        logger.info("leader selected: {}", selfId);
        leaderState = new LeaderState(servers, state.getLastLogIndex());
        timer.startHeartBeat();
        timer.startReplication();
    }

    private void convertToCandidate() {
        logger.info("election started by candidate: {}", selfId);
        state.incrementCurrentTerm();
        state.setVoteFor(selfId);
        if (electionInProgress)
            timer.resetElectionTimer();
        else
            timer.startElectionTimer();
        electionInProgress = true;
        VoteRequest request = new VoteRequest(state.getCurrentTerm(), selfId, state.getLastLogIndex(), state.getLastLogTerm());
        List<TermStore> results = requestService.sendRequestToAll(request);
        int votesForMe = 1;
        for (TermStore result : results) {
            VoteResult voteResult = (VoteResult) result;
            if (shouldConvertToFollower(voteResult)) {
                convertToFollower(voteResult);
                return;
            }
            votesForMe += voteResult.isVoteGranted() ? 1 : 0;
        }
        if (votesForMe > servers.size() / 2)
            convertToLeader();
        logger.info("election failed, votes for me {}", votesForMe);
        electionInProgress = false;
    }

    private boolean appendEntries(String server) {
        int nextIndex = leaderState.getNextIndex(server);
        if (state.getLastLogIndex() >= nextIndex) {
            int prevLogIndex = nextIndex - 1;
            int prevLogTerm = state.getLog().get(prevLogIndex).getTerm();
            KeyValue keyValue = state.getLog().get(leaderState.getNextIndex(server)).getKeyValue();
            AppendRequest request = new AppendRequest(server, state.getCurrentTerm(), selfId, prevLogIndex, prevLogTerm, keyValue, state.getCommitIndex());
            AppendResult result = communication.appendEntries(request);
            if (shouldConvertToFollower(result)) {
                convertToFollower(result);
                return false;
            }
            logger.info("append request sent (to,term,leader,prevIdx,prevTerm,keyValue,commitIdx) ({},{},{},{},{},({},{}),{})", server, state.getCurrentTerm(), selfId, prevLogIndex, prevLogTerm, keyValue.getKey(), keyValue.getValue(), state.getCommitIndex());
            if (result.isSuccess()) {
                leaderState.setMatchIndex(server, leaderState.getNextIndex(server));
                leaderState.incrementNextIndex(server);
                logger.info("{} appended successfully, matchIndex,nextIndex ({},{})", server, leaderState.getMatchIndex(server), leaderState.getNextIndex(server));
            } else if (!result.isConnectionError()) {
                leaderState.decrementNextIndex(server);
                logger.info("{} failed to append, retrying with previous log entry {}", server, leaderState.getNextIndex(server));
            }
            return false;
        }
        return true;
    }

    private void updateCommitIndex() {
        leaderState.setMatchIndex(selfId, state.getLastLogIndex());
        leaderState.setNextIndex(selfId, state.getLastLogIndex() + 1);
        List<Integer> greater = leaderState.getMatchIndex().values().stream().filter(i -> i > state.getCommitIndex()).collect(Collectors.toList());
        if (greater.size() <= servers.size() / 2)
            return;
        int newCommitIndex = greater.stream().min(Integer::compare).get();
        if (state.getLog().get(newCommitIndex).getTerm() == state.getCurrentTerm()) {
            state.setCommitIndex(newCommitIndex);
            logger.info("updated commit index to {}", newCommitIndex);
        }
    }

    private void replicate() {
        logger.info("replication started");
        boolean replicated = false;
        while (!replicated) {
            for (String server : otherServers) {
                replicated &= appendEntries(server);
            }
            updateCommitIndex();
            storeCommitted();
        }
        logger.info("replication finished");
    }

    private void heartBeat() {
        AppendRequest request = new AppendRequest(state.getCurrentTerm(), selfId, state.getLastLogIndex(), state.getLastLogTerm(), null, state.getCommitIndex());
        List<TermStore> results = requestService.sendRequestToAll(request);
        for (TermStore result : results) {
            if (shouldConvertToFollower(result)) {
                convertToFollower(result);
                return;
            }
        }
    }

    private boolean storeCommitted() {
        if (state.getCommitIndex() <= state.getLastApplied())
            return false;
        state.incrementeLastApplied();
        KeyValue toCommit = state.getLog().get(state.getLastApplied()).getKeyValue();
        store.put(toCommit.getKey(), toCommit.getValue());
        logger.info("stored ({},{})", toCommit.getKey(), toCommit.getValue());
        return true;
    }

    public synchronized StoreResult store(String key, String value) {
        if (!isLeader())
            return StoreResult.redirect(state.getLeaderId());
        state.getLog().add(new Entry(new KeyValue(key, value), state.getCurrentTerm()));
        timer.startReplication();
        return StoreResult.success();
    }

    public synchronized RetrieveResult retrieve(String key) {
        if (!isLeader())
            return RetrieveResult.redirect(state.getLeaderId());
        return RetrieveResult.success(store.get(key));
    }

    private boolean isLeader() {
        return leaderState != null;
    }


    public boolean isConnected() {
        return connected;
    }

    public void connect() {
        connected = true;
        timer.startElectionTimer();
    }

    public void disconnect() {
        connected = false;
        timer.stopElectionTimer();
        timer.stopHeartBeat();
        timer.stopReplication();
    }


}
