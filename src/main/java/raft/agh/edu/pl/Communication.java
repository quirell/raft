package raft.agh.edu.pl;

import java.util.concurrent.Future;

/**
 * Created by quirell on 04.11.2016.
 */
public interface Communication {

    Future<AppendResult> appendEntries(AppendRequest request);

    Future<VoteResult> requestVote(VoteRequest request);

}
