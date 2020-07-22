package com.qingyueyun.example.demo.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Copyright (C) 2020 青跃云
 *
 * @author 蓝明乐
 * @version 0.0.1
 * @since 1.8
 */
@Slf4j
@Component
public class PrintAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        log.info("[PrintAuthenticationEntryPoint] commence , [AuthenticationException]: {}", e.getMessage());
        return Mono.empty();
    }

}
