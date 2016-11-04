package raft.agh.edu.pl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;


@Component
public class Server {

    private State state;
    private LeaderState leaderState;
    private HashMap<String, String> store;
    @Autowired
    private TimerService timer;
    private List<String> servers;
    private Communication communication;

    @PostConstruct
    private void start() {
        timer.setElectionCallback(this::convertToCandidate);
        timer.setHeartBeatCallback(this::heartBeat);
    }


    public AppendResult appendEntries(AppendRequest request) {
        timer.resetElectionTimer();
        if (request.getTerm() < state.getCurrentTerm() || state.getLog().get(request.getPrevLogIndex()).getTerm() != request.getPrevLogTerm())
            return new AppendResult(state.getCurrentTerm(), false);

        if (state.getLastLogIndex() > request.getPrevLogIndex() && request.getTerm() != state.getLog().get(request.getPrevLogIndex() + 1).getTerm()) {
            for (int i = state.getLastLogIndex(); i > request.getPrevLogIndex(); i--)
                state.getLog().remove(i);
        }

        state.getLog().add(new Entry(request.getPayload(), state.getCurrentTerm()));
        if (request.getLeaderCommit() > state.getCommitIndex())
            state.setCommitIndex(Math.min(state.getLastLogIndex(), request.getLeaderCommit()));
        convertToFollower(request);
        storeCommitted();
        return new AppendResult(state.getCurrentTerm(), true);
    }

    public VoteResult requestVote(VoteRequest request) {
        if (request.getTerm() < state.getCurrentTerm())
            return new VoteResult(state.getCurrentTerm(), false);
        boolean candidateHasUpToDateLog = request.getLastLogTerm() > state.getLastLogTerm() || request.getLastLogTerm() == state.getLastLogTerm() && request.getLastLogIndex() >= state.getLastLogIndex();
        if (!request.getCandidateId().equals(state.getVoteFor()) || !candidateHasUpToDateLog)
            return new VoteResult(state.getCurrentTerm(), false);
        state.setVoteFor(request.getCandidateId());
        convertToFollower(request);
        return new VoteResult(state.getCurrentTerm(), true);
    }

    private void convertToFollower(TermStore termStore) {
        if (termStore.getTerm() > state.getCurrentTerm())
            state.setCurrentTerm(termStore.getTerm());
        //return to follower
    }

    private void convertToFolower() {

    }

    private void convertToCandidate() {
        state.incremmentCurrentTerm();
        state.setVoteFor(state.getSelfId());
        timer.resetElectionTimer();
        for (String server : servers) {
            Future<VoteResult> result = communication.requestVote(new VoteRequest(server, state.getCurrentTerm(), state.getSelfId(), state.getLastLogIndex(), state.getLastLogTerm()));
            result.
        }

    }

    private void heartBeat() {

    }


    private void storeCommitted() {
        if (state.getCommitIndex() <= state.getLastApplied())
            return;
        state.incrementeLastApplied();
        KeyValue toCommit = state.getLog().get(state.getLastApplied()).getKeyValue();
        store.put(toCommit.getKey(), toCommit.getValue());
    }

    public String store(KeyValue request) {
        if (!isLeader())
            return state.getLeaderId();
        state.getLog().add(new Entry(request, state.getCurrentTerm()));

        storeCommitted();
        return null;
    }


    public String get(String key) {
        return null;
    }

    private boolean isLeader() {
        return leaderState != null;
    }
}
