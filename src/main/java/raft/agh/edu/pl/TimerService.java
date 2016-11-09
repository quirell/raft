package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;

@Service
public class TimerService {

    private final Logger logger = LoggerFactory.getLogger(TimerService.class);

    @Autowired
    private ScheduledExecutorService timerExecutorService;
    @Autowired
    private RequestExecutorService requestService;
    @Value("${lowerElectionTimeout}")
    private int lowerElectionTimeout;
    @Value("${upperElectionTimeout}")
    private int upperElectionTimeout;
    @Value("${heartBeatTimeout}")
    private int heartBeatTimeout;
    private ScheduledFuture<?> electionTimer;
    private ScheduledFuture<?> heartBeatTimer;
    private ScheduledFuture<?> replicationThread;
    private TimerCallback electionCallback;
    private TimerCallback heartBeatCallback;
    private TimerCallback replicationCallback;
    private Random random = new Random();

    public TimerService() {
    }

    private int electionTimeout(){
       return random.nextInt(upperElectionTimeout-lowerElectionTimeout) + lowerElectionTimeout;
    }
    public void resetElectionTimer(){
        if(electionTimer != null){
            electionTimer.cancel(true);
            requestService.cancelAll();
        }
        startElectionTimer();
    }

    public void stopElectionTimer() {
        electionTimer.cancel(false);
    }

    public void startElectionTimer() {
        int delay = electionTimeout();
        electionTimer = timerExecutorService.schedule(() -> electionCallback.callback(), delay, TimeUnit.MILLISECONDS);
    }


    public void startHeartBeat(){
        heartBeatTimer = timerExecutorService.scheduleAtFixedRate(() -> heartBeatCallback.callback(), 0, heartBeatTimeout, TimeUnit.MILLISECONDS);
    }

    public void stopHeartBeat(){
        if(heartBeatTimer != null){
            heartBeatTimer.cancel(true);
            requestService.cancelAll();
        }
    }

    public void startReplication() {
        if (replicationThread == null || replicationThread.isDone())
            replicationThread = timerExecutorService.schedule(() -> replicationCallback.callback(), 0, TimeUnit.MILLISECONDS);
    }

    public void stopReplication() {
        if (replicationThread != null) {
            replicationThread.cancel(true);
        }
    }

    public void setElectionCallback(TimerCallback electionCallback) {
        this.electionCallback = electionCallback;
    }

    public void setHeartBeatCallback(TimerCallback heartBeatCallback) {
        this.heartBeatCallback = heartBeatCallback;
    }

    public void setReplicationCallback(TimerCallback leaderCallback) {
        this.replicationCallback = leaderCallback;
    }


    @FunctionalInterface
    interface TimerCallback {
        void callback();
    }
}
