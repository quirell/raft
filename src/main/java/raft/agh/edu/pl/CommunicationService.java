package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CommunicationService {

    private final Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    private static String VOTE = "/vote";
    private static String APPEND = "/append";
    @Autowired
    private RestTemplate restTemplate;

    AppendResult appendEntries(AppendRequest request){
        logger.info("appendEntries to {}",request.getRecipient());
        ResponseEntity<AppendResult> result = restTemplate.postForEntity(request.getRecipient()+APPEND, request, AppendResult.class);
        if(result.getStatusCode() == HttpStatus.OK)
            return result.getBody();
        return AppendResult.connectionFailed();
    }


    VoteResult requestVote(VoteRequest request){
        logger.info("request vote from {}",request.getRecipient());
        ResponseEntity<VoteResult> result = restTemplate.postForEntity(request.getRecipient()+VOTE, request, VoteResult.class);
        if(result.getStatusCode() == HttpStatus.OK)
            return result.getBody();
        return VoteResult.connectionFailed();
    }

}
