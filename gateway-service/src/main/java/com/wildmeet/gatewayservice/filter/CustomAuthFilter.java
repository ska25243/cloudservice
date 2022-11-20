package com.wildmeet.gatewayservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildmeet.gatewayservice.jwt.JwtUtil;
import com.wildmeet.gatewayservice.response.ResponseResult;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
public class CustomAuthFilter extends AbstractGatewayFilterFactory<CustomAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public CustomAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // application.yml 파일에서 지정한 filer의 Argument값을 받는 부분
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest req = exchange.getRequest();
            if (!req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Not found auth", HttpStatus.UNAUTHORIZED);
            }

            String authorization = Objects.requireNonNull(req.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            String token = authorization.replace("Bearer", "").trim();

            try {
                jwtUtil.validateToken(token);
            } catch (JwtException e) {
                return onError(exchange, e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        ResponseResult responseResult = new ResponseResult();
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        responseResult.setContentType(String.valueOf(MediaType.APPLICATION_JSON));
        responseResult.setStatus(status.value());
        responseResult.setMessage(message);
        DataBuffer db;
        try {
            db = new DefaultDataBufferFactory().wrap(objectMapper.writeValueAsBytes(responseResult));
        }catch (Exception e){
            db = null;
        }

        return exchange.getResponse().writeWith(Mono.just(db));
    }
}
