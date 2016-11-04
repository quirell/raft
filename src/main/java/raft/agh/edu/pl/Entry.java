package raft.agh.edu.pl;

/**
 * Created by quirell on 04.11.2016.
 */
public class Entry {
    private KeyValue keyValue;
    private int term;

    public Entry(KeyValue keyValue, int term) {
        this.keyValue = keyValue;
        this.term = term;
    }

    public KeyValue getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(KeyValue keyValue) {
        this.keyValue = keyValue;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
