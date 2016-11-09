package raft.agh.edu.pl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class LeaderState {
    Map<String,Integer> nextIndex;
    Map<String,Integer> matchIndex;

    public LeaderState(List<String> servers,int lastLogIndex) {
        nextIndex = servers.stream().collect(Collectors.toMap(Function.identity(), (s) -> lastLogIndex + 1));
        matchIndex = servers.stream().collect(Collectors.toMap(Function.identity(), (s) -> 1));
    }

    public Map<String, Integer> getNextIndex() {
        return nextIndex;
    }

    public Map<String, Integer> getMatchIndex() {
        return matchIndex;
    }

    public void incrementNextIndex(String server){
        nextIndex.put(server,nextIndex.get(server)+1);
    }

    public void setMatchIndex(String server,int index){
        matchIndex.put(server,index);
    }

    public int getNextIndex(String server){
        return nextIndex.get(server);
    }

    public int getMatchIndex(String server){
        return matchIndex.get(server);
    }

    public void decrementNextIndex(String server) {
        nextIndex.put(server,nextIndex.get(server)-1);
    }

    public void setNextIndex(String server, int index) {
        nextIndex.put(server, index);
    }
}
