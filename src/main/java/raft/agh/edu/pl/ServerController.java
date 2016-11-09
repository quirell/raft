package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class ServerController {

    private final Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private Server server;

    @RequestMapping(path = "/vote", method = RequestMethod.POST)
    public VoteResult vote(@RequestBody VoteRequest request, HttpServletResponse httpServletResponse) throws IOException {
        if (server.isConnected())
            return server.requestVote(request);
        httpServletResponse.sendError(HttpServletResponse.SC_GONE);
        return null;
    }

    @RequestMapping(path = "/append", method = RequestMethod.POST)
    public AppendResult append(@RequestBody AppendRequest request, HttpServletResponse httpServletResponse) throws IOException {
        if (server.isConnected())
            return server.appendEntries(request);
        httpServletResponse.sendError(HttpServletResponse.SC_GONE);
        return null;
    }

    @RequestMapping(path = "store/{key}/{value}", method = RequestMethod.GET)
    public void store(@PathVariable String key, @PathVariable String value, HttpServletResponse httpServletResponse) throws IOException {
        if (server.isConnected()) {
            StoreResult result = server.store(key, value);
            if (!result.isSuccess()) {
                logger.info("redirecting to leader {}", result.getRedirectUrl());
                httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpServletResponse.getWriter().write(result.getRedirectUrl());
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_GONE);
        }
    }

    @RequestMapping(path = "retrieve/{key}", method = RequestMethod.GET)
    public void get(@PathVariable String key, HttpServletResponse httpServletResponse) throws IOException {
        if (server.isConnected()) {
            RetrieveResult result = server.retrieve(key);
            if (!result.isSuccess()) {
                logger.info("redirecting to leader {}", result.getRedirectUrl());
                httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpServletResponse.getWriter().write(result.getRedirectUrl());
            }
            if (result.getValue() != null)
                httpServletResponse.getWriter().write(result.getValue());
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_GONE);
        }
    }

    //cancel all requests if disconnected
    @RequestMapping(path = "connect", method = RequestMethod.GET)
    public void disconnect() {
        server.connect();
    }

    @RequestMapping(path = "disconnect", method = RequestMethod.GET)
    public void connect() {
        server.disconnect();
    }

}
