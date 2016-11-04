package raft.agh.edu.pl;

import java.util.Map;

/**
 * Created by quirell on 04.11.2016.
 */
public class LeaderState {
    Map<String,Integer> nextIndex;
    Map<String,Integer> matchIndex;

    public Map<String, Integer> getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(Map<String, Integer> nextIndex) {
        this.nextIndex = nextIndex;
    }

    public Map<String, Integer> getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(Map<String, Integer> matchIndex) {
        this.matchIndex = matchIndex;
    }
}
