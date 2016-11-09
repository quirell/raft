package raft.agh.edu.pl;

/**
 * Created by quirell on 06.11.2016.
 */
public interface Request {
    void setRecipient(String recipient);

    Request clone();
}
