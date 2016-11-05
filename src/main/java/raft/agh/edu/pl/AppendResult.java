package raft.agh.edu.pl;

/**
 * Created by quirell on 04.11.2016.
 */
public class AppendResult implements TermStore{
    private int term;
    private boolean success;
    private boolean connectionError;
    public AppendResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }
    private AppendResult(){
        this.connectionError = true;
    }

    public int getTerm() {
        return term;
    }


    public boolean isSuccess() {
        return success;
    }

    public boolean isConnectionError() {
        return connectionError;
    }

    public static AppendResult connectionFailed() {
        return new AppendResult();
    }
}
