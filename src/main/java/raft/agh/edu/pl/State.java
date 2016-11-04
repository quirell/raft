package raft.agh.edu.pl;

import java.util.ArrayList;

/**
 * Created by quirell on 04.11.2016.
 */
public class State {
    private int currentTerm;
    private String voteFor;
    private ArrayList<Entry> log;
    private int commitIndex;
    private int lastApplied;
    private String leaderId;
    private String selfId;

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

    public void setLog(ArrayList<Entry> log) {
        this.log = log;
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

    public void incrementeLastApplied(){
        lastApplied++;
    }

    public int getLastLogIndex(){
        return log.size() - 1;
    }

    public int getLastLogTerm(){
        return log.get(getLastLogIndex()).getTerm();
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void incremmentCurrentTerm(){
        currentTerm += 1;
    }

    public String getSelfId() {
        return selfId;
    }

    public void setSelfId(String selfId) {
        this.selfId = selfId;
    }
}
