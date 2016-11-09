package raft.agh.edu.pl;


public class RetrieveResult {

    private String value;

    private String redirectUrl;

    private boolean success;

    private RetrieveResult(String value, boolean success) {
        this.success = success;
        if (success)
            this.value = value;
        else
            redirectUrl = value;
    }

    public String getValue() {
        return value;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public static RetrieveResult success(String value) {
        return new RetrieveResult(value, true);
    }

    public static RetrieveResult redirect(String redirectUrl) {
        return new RetrieveResult(redirectUrl, false);
    }
}
