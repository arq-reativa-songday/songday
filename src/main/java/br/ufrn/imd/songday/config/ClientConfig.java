package br.ufrn.imd.songday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import br.ufrn.imd.songday.client.SongsClient;
import br.ufrn.imd.songday.exception.ServicesCommunicationException;
import reactor.core.publisher.Mono;

@Configuration
public class ClientConfig {
    @Value("${gateaway.api.address}")
    private String baseUrl;

    @LoadBalanced
    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServicesCommunicationException(
                                "Erro durante a comunicação com Songs: " + response.toString())))
                .build();
    }

    @Bean
    SongsClient songsClient() {
        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient()))
                .build()
                .createClient(SongsClient.class);
    }
}
