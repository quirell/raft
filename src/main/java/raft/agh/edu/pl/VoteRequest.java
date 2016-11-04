package raft.agh.edu.pl;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by quirell on 04.11.2016.
 */
public class VoteRequest implements TermStore {
    private int term;
    private String candidateId;
    private int lastLogIndex;
    private int lastLogTerm;
    @JsonIgnore
    private String recipient;

    public VoteRequest(String recipient, int term, String candidateId, int lastLogIndex, int lastLogTerm) {
        this.recipient = recipient;
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public int getTerm() {
        return term;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public String getRecipient() {
        return recipient;
    }
}
