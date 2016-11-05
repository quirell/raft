package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Service
public class TimerService {

    private final Logger logger = LoggerFactory.getLogger(TimerService.class);

    @Autowired
    private ScheduledExecutorService executorService;
    @Value("${lowerElectionTimeout}")
    private int lowerElectionTimeout;
    @Value("${upperElectionTimeout")
    private int upperElectionTimeout;
    @Value("${heartBeatTimeout")
    private int heartBeatTimeout;
    private ScheduledFuture<?> electionTimer;
    private ScheduledFuture<?> heartBeatTimer;
    private ScheduledFuture<?> leaderThread;
    private TimerCallback electionCallback;
    private TimerCallback heartBeatCallback;
    private TimerCallback leaderCallback;
    private Random random = new Random();

    public TimerService() {
    }

    private int electionTimeout(){
       return random.nextInt(upperElectionTimeout-lowerElectionTimeout) + lowerElectionTimeout;
    }
    public void resetElectionTimer(){
        if(electionTimer != null){
            boolean cancelled = electionTimer.cancel(true);
            logger.info("election timer cancel attempt result: {}",cancelled);
        }
        int delay = electionTimeout();
        int interval = electionTimeout();
        electionTimer = executorService.scheduleAtFixedRate(() -> electionCallback.callback(), delay, interval, TimeUnit.MILLISECONDS);
        logger.info("election timer scheduled (delay,interval) ({},{})",delay,interval);
    }

    public void startHeartBeat(){
        heartBeatTimer = executorService.scheduleAtFixedRate(() -> heartBeatCallback.callback(), 0, heartBeatTimeout, TimeUnit.MILLISECONDS);
        logger.info("heartbeat started with interval {}",heartBeatTimeout);
    }

    public void stopHeartBeat(){
        if(heartBeatTimer != null){
            boolean cancelled = heartBeatTimer.cancel(true);
            logger.info("heartbeat cancel attempt result: {}",cancelled);
        }
    }

    public void setElectionCallback(TimerCallback electionCallback) {
        this.electionCallback = electionCallback;
    }

    public void setHeartBeatCallback(TimerCallback heartBeatCallback) {
        this.heartBeatCallback = heartBeatCallback;
    }

    public void setLeaderCallback(TimerCallback leaderCallback) {
        this.leaderCallback = leaderCallback;
    }

    public void startLeaderLoop(){
        leaderThread = executorService.schedule(() -> leaderCallback.callback(),0,TimeUnit.MILLISECONDS);
    }

    public void stopLeaderLoop(){
        if(leaderThread != null){
            leaderThread.cancel(true);
        }
    }

    @FunctionalInterface
    interface TimerCallback {
        void callback();
    }
}
