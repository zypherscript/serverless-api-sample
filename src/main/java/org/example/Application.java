package org.example;

import org.example.post.JsonPlaceholderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  JsonPlaceholderService jsonPlaceholderService() {
    var client = WebClient.builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .build();
    var factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();
    return factory.createClient(JsonPlaceholderService.class);
  }
}