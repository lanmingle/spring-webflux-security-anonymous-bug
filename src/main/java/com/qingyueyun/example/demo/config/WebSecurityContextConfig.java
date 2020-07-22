package com.qingyueyun.example.demo.config;

import com.qingyueyun.example.demo.authentication.PrintAuthenticationEntryPoint;
import com.qingyueyun.example.demo.authorization.PrintAccessDeniedHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.logout.LogoutWebFilter;
import org.springframework.security.web.server.authorization.AuthorizationWebFilter;
import org.springframework.security.web.server.authorization.ExceptionTranslationWebFilter;
import org.springframework.security.web.server.context.ReactorContextWebFilter;
import org.springframework.security.web.server.context.SecurityContextServerWebExchangeWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.header.HttpHeaderWriterWebFilter;
import org.springframework.security.web.server.savedrequest.ServerRequestCacheWebFilter;

import java.util.List;

/**
 * Copyright (C) 2020 青跃云
 *
 * @author 蓝明乐
 * @version 0.0.1
 * @since 1.8
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class WebSecurityContextConfig {

    private static final String ANONYMOUS_KEY = "key";

    private static final String ANONYMOUS_PRINCIPAL = "anonymous";

    private static final List<GrantedAuthority> ANONYMOUS_AUTHORITIES = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");

    @Autowired
    private PrintAuthenticationEntryPoint printAuthenticationEntryPoint;

    @Autowired
    private PrintAccessDeniedHandler printAccessDeniedHandler;

    /**
     * @see ServerSecurityContextRepository
     * @see HttpHeaderWriterWebFilter
     * @see CsrfWebFilter
     * @see ReactorContextWebFilter
     * @see AuthenticationWebFilter
     * @see AnonymousAuthenticationWebFilter
     * @see SecurityContextServerWebExchangeWebFilter
     * @see ServerRequestCacheWebFilter
     * @see LogoutWebFilter
     * @see ExceptionTranslationWebFilter
     * @see AuthorizationWebFilter
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        http
                // https://github.com/spring-projects/spring-security/issues/8849
                // https://github.com/spring-projects/spring-security/issues/6565
                // https://github.com/spring-projects/spring-security/pull/6590
                .anonymous(anonymousSpec -> {
                    anonymousSpec
                            .key(ANONYMOUS_KEY)
                            .principal(ANONYMOUS_PRINCIPAL)
                            .authorities(ANONYMOUS_AUTHORITIES)
                    ;
                })
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(this.printAuthenticationEntryPoint)
                        .accessDeniedHandler(this.printAccessDeniedHandler)
                )
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .anyExchange().access((authentication, object) -> authentication
                                .map(a -> new AuthorizationDecision(false))
                                .defaultIfEmpty(new AuthorizationDecision(false))
                        )
                )
        ;

        return http.build();
    }

}
