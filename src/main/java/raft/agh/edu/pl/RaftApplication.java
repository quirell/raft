package raft.agh.edu.pl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EnableAsync
@SpringBootApplication
public class RaftApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaftApplication.class, args);
	}

	@Bean
	private ScheduledExecutorService executorService(){
		return Executors.newScheduledThreadPool(2);
	}
}
