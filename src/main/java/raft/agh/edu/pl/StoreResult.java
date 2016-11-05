package raft.agh.edu.pl;

/**
 * Created by quirell on 05.11.2016.
 */
public class StoreResult {

    private boolean success;
    private String redirectUrl;

    private StoreResult() {
        this.success = true;
    }

    private StoreResult(String redirecUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public static StoreResult success(){
        return new StoreResult();
    }

    public static StoreResult redirect(String redirectUrl){
        return new StoreResult(redirectUrl);
    }
}
