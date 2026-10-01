package com.learn.orderservice.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(LoadBalancerClient loadBalancerClient,
                                        ObjectProvider<ObservationRegistry> observationRegistry,
                                        @Value("${product-service.url}") String baseUrl) {

        ClientHttpRequestInterceptor loadBalancing = (request, body, execution) -> {
            URI original = request.getURI();
            ServiceInstance instance = loadBalancerClient.choose(original.getHost());
            if (instance == null) {
                throw new IOException("No instances available for " + original.getHost());
            }
            URI target = loadBalancerClient.reconstructURI(instance, original);
            HttpRequest rewritten = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                    return target;
                }
            };
            return execution.execute(rewritten, body);
        };

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);

        // The observation registry is what makes each outgoing call carry the trace id.
        return RestClient.builder()
                .observationRegistry(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP))
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .requestInterceptor(loadBalancing)
                .build();
    }
}
