package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by quirell on 06.11.2016.
 */
@Service
public class RequestExecutorService {


    private final Logger logger = LoggerFactory.getLogger(RequestExecutorService.class);

    @Autowired
    private CommunicationService communication;
    @Autowired
    private CompletionService<TermStore> completionService;
    @Value("${servers}")
    private List<String> servers;
    @Value("${selfId}")
    private String selfId;
    private List<String> otherServers;
    private List<Future<TermStore>> scheduled;

    @PostConstruct
    public void init() {
        otherServers = servers.stream().filter(s -> !s.equals(selfId)).collect(Collectors.toList());
    }

    public List<TermStore> sendRequestToAll(Request request) {
        scheduled = new LinkedList<>();
        for (String server : otherServers) {
            Request clone = request.clone();
            clone.setRecipient(server);
            scheduled.add(completionService.submit(() -> delegateRequest(clone)));
        }
        int waiting = otherServers.size();
        List<TermStore> result = new LinkedList<>();
        while (waiting-- > 0) {
            try {
                Future<TermStore> future = completionService.take();
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.info("sendRequest exception", e);
            }
        }
        return result;
    }


    private TermStore delegateRequest(Request request) {
        if (request.getClass() == AppendRequest.class)
            return communication.appendEntries((AppendRequest) request);
        else
            return communication.requestVote((VoteRequest) request);
    }


    public void cancelAll() {
        if (scheduled == null)
            return;
        for (Future<TermStore> future : scheduled) {
            future.cancel(true);
        }
        scheduled = null;
    }
}
