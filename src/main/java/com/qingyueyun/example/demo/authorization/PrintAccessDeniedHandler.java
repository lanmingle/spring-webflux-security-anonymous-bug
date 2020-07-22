package com.qingyueyun.example.demo.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
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
public class PrintAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException e) {
        log.info("[PrintAccessDeniedHandler] handle , [AuthenticationException]: {}", e.getMessage());
        return Mono.empty();
    }

}
