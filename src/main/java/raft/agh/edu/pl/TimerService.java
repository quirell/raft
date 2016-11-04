package raft.agh.edu.pl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class TimerService {

    @Autowired
    private ScheduledExecutorService executorService;

    private int electionTimeout;
    private int heartBeatTimeout;
    private ScheduledFuture<?> electionTimer;
    private ScheduledFuture<?> heartBeatTimer;
    private TimerCallback electionCallback;
    private TimerCallback heartBeatCallback;

    public TimerService() {
    }

    public void resetElectionTimer(){
        if(electionTimer != null){
            electionTimer.cancel(false);
        }
        electionTimer = executorService.scheduleAtFixedRate(() -> {
        }, electionTimeout, electionTimeout, TimeUnit.MILLISECONDS);
    }

    public void resetHeartBeatTimer(){
        if(heartBeatTimer != null){
            heartBeatTimer.cancel(false);
        }
        heartBeatTimer = executorService.scheduleAtFixedRate(() -> {
        }, 0, heartBeatTimeout, TimeUnit.MILLISECONDS);
    }

    public void setElectionCallback(TimerCallback electionCallback) {
        this.electionCallback = electionCallback;
    }

    public void setHeartBeatCallback(TimerCallback heartBeatCallback) {
        this.heartBeatCallback = heartBeatCallback;
    }
    @FunctionalInterface
    interface TimerCallback {
        void callback();
    }
}
