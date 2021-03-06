package raft.agh.edu.pl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

@SpringBootApplication
public class RaftApplication extends SpringBootServletInitializer{

    private static final Logger logger = LoggerFactory.getLogger(RaftApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RaftApplication.class);
    }


    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((th, tr) -> logger.error("thread exception", tr));
        SpringApplication.run(RaftApplication.class, args);
    }

	@Bean
    public ScheduledExecutorService timerExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean
    public CompletionService<TermStore> completionService() {
        return new ExecutorCompletionService<>(Executors.newFixedThreadPool(4));
    }

    @Bean
    public RestTemplate restTemplate(@Value("${requestTimeout}") int timeout) {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

}
