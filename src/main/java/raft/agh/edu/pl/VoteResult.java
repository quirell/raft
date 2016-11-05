package raft.agh.edu.pl;

/**
 * Created by quirell on 04.11.2016.
 */
public class VoteResult implements TermStore{
    private int term;
    private boolean voteGranted;
    private boolean connectionError;

    public VoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public VoteResult() {
        connectionError = true;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public boolean isConnectionError() {
        return connectionError;
    }

    public static VoteResult connectionFailed() {
        return new VoteResult();
    }
}
