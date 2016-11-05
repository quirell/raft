package raft.agh.edu.pl;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AppendRequest implements TermStore {
    private int term;
    private String leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private KeyValue payload;
    private int leaderCommit;
    @JsonIgnore
    private String recipient;

    public AppendRequest(String recipient, int term, String leaderId, int prevLogIndex, int prevLogTerm, KeyValue payload, int leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.payload = payload;
        this.leaderCommit = leaderCommit;
        this.recipient = recipient;
    }

    @Override
    public int getTerm() {
        return term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public KeyValue getPayload() {
        return payload;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isHeartBeat(){
        return payload == null;
    }
}
