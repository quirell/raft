package raft.agh.edu.pl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAsync
@SpringBootApplication
public class RaftApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaftApplication.class, args);
	}

	@Bean
	private ScheduledExecutorService executorService(){
		return Executors.newScheduledThreadPool(3);
	}

    @Bean
    private CompletionService<TermStore> completionService(){
        return new ExecutorCompletionService<>(Executors.newFixedThreadPool(4));
    }

    @Bean
    private RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return restTemplate;
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
