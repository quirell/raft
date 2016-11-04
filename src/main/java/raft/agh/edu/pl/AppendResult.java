package raft.agh.edu.pl;

/**
 * Created by quirell on 04.11.2016.
 */
public class AppendResult implements TermStore{
    private int term;
    private boolean success;

    public AppendResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
