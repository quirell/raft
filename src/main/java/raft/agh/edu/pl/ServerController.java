package raft.agh.edu.pl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class ServerController {

    @Autowired
    private Server server;

    @RequestMapping(path = "/vote",method = RequestMethod.POST)
    public VoteResult vote(@RequestBody VoteRequest request){
        return server.requestVote(request);
    }

    @RequestMapping(path = "/append", method = RequestMethod.POST)
    public AppendResult append(@RequestBody AppendRequest request){
        return server.appendEntries(request);
    }

    @RequestMapping(path = "store/{key}",method = RequestMethod.POST)
    public void store(@PathVariable String key, @RequestBody String value, HttpServletResponse httpServletResponse) throws IOException {
        StoreResult result = server.store(key, value);
        if(!result.isSuccess())
            httpServletResponse.sendRedirect(result.getRedirectUrl());

    }

    @RequestMapping(path = "retrieve/{key}",method = RequestMethod.GET)
    public String get(@PathVariable String key){
        return server.retrieve(key);
    }
}
