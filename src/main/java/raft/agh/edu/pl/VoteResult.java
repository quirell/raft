package raft.agh.edu.pl;

/**
 * Created by quirell on 04.11.2016.
 */
public class VoteResult implements TermStore{
    private int term;
    private boolean voteGranted;

    public VoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }
}
