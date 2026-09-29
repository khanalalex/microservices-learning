package com.learn.orderservice.config;

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
                                        @Value("${product-service.url}") String baseUrl) {

        // Replaces the service name in the URL (http://product-service/...)
        // with the address of a real instance chosen from Eureka's registry.
        ClientHttpRequestInterceptor loadBalancing = (request, body, execution) -> {
            URI original = request.getURI();
            ServiceInstance instance = loadBalancerClient.choose(original.getHost());
            if (instance == null) {
                // IOException becomes ResourceAccessException, which our handler turns into 503
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
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .requestInterceptor(loadBalancing)
                .build();
    }
}
