package raft.agh.edu.pl;

import java.util.ArrayList;

/**
 * Created by quirell on 04.11.2016.
 */
public class State {
    private int currentTerm;
    private String voteFor;
    private ArrayList<Entry> log;
    private int commitIndex = 1;
    private int lastApplied = 1;
    private String leaderId;

    public State() {
        this.log = new ArrayList<>();
        this.log.add(new Entry(new KeyValue("", ""), 0));
        this.log.add(new Entry(new KeyValue("", ""), 0));
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getVoteFor() {
        return voteFor;
    }

    public void setVoteFor(String voteFor) {
        this.voteFor = voteFor;
    }

    public ArrayList<Entry> getLog() {
        return log;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public int getLastApplied() {
        return lastApplied;
    }

    public void incrementeLastApplied() {
        lastApplied++;
    }

    public int getLastLogIndex() {
        return log.size() - 1;
    }

    public int getLastLogTerm() {
        return log.get(getLastLogIndex()).getTerm();
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void incrementCurrentTerm() {
        currentTerm += 1;
    }
}
