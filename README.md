# Spring Webflux Security Anonymous BUG Demo

See demo issues: https://github.com/spring-projects/spring-security/issues/8849

This code:

PrintAuthenticationEntryPoint:

``` java

@Slf4j
@Component
public class PrintAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        log.info("[PrintAuthenticationEntryPoint] commence , [AuthenticationException]: {}", e.getMessage());
        return Mono.empty();
    }

}

```

PrintAccessDeniedHandler:

``` java

@Slf4j
@Component
public class PrintAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException e) {
        log.info("[PrintAccessDeniedHandler] handle , [AuthenticationException]: {}", e.getMessage());
        return Mono.empty();
    }

}

```

WebSecurityContextConfig:

``` java

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

```

curl test

``` bash

curl localhost:8080

```

print log:

``` log

2020-07-22 10:59:39.907  INFO 4724 --- [  restartedMain] ngWebfluxSecurityAnonymousBugApplication : Starting SpringWebfluxSecurityAnonymousBugApplication on localhost with PID 4724 (/Users/lanmingle/Developer/src/github.com/lanmingle/spring-webflux-security-anonymous-bug/target/classes started by lanmingle in /Users/lanmingle/Developer/src/github.com/lanmingle/spring-webflux-security-anonymous-bug)
2020-07-22 10:59:39.915  INFO 4724 --- [  restartedMain] ngWebfluxSecurityAnonymousBugApplication : No active profile set, falling back to default profiles: default
2020-07-22 10:59:40.078  INFO 4724 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : Devtools property defaults active! Set 'spring.devtools.add-properties' to 'false' to disable
2020-07-22 10:59:40.078  INFO 4724 --- [  restartedMain] .e.DevToolsPropertyDefaultsPostProcessor : For additional web related logging consider setting the 'logging.level.web' property to 'DEBUG'
2020-07-22 10:59:42.120  INFO 4724 --- [  restartedMain] ctiveUserDetailsServiceAutoConfiguration : 

Using generated security password: 10e257c9-8586-457d-a491-d4c47e219bdc

2020-07-22 10:59:42.659  INFO 4724 --- [  restartedMain] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 2 endpoint(s) beneath base path '/actuator'
2020-07-22 10:59:42.949  INFO 4724 --- [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : LiveReload server is running on port 35729
2020-07-22 10:59:43.261  INFO 4724 --- [  restartedMain] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port(s): 8080
2020-07-22 10:59:43.394  INFO 4724 --- [  restartedMain] ngWebfluxSecurityAnonymousBugApplication : Started SpringWebfluxSecurityAnonymousBugApplication in 5.12 seconds (JVM running for 7.257)
2020-07-22 11:03:35.737  INFO 4724 --- [oundedElastic-1] c.q.e.d.a.PrintAuthenticationEntryPoint  : [PrintAuthenticationEntryPoint] commence , [AuthenticationException]: Not Authenticated
2020-07-22 11:03:35.739  INFO 4724 --- [oundedElastic-1] c.q.e.d.a.PrintAccessDeniedHandler       : [PrintAccessDeniedHandler] handle , [AuthenticationException]: Access Denied

```

analysis [AnonymousAuthenticationWebFilter] & [ExceptionTranslationWebFilter] :

``` java
    
     // AnonymousAuthenticationWebFilter
     // setAuthenticated(true);

      protected Authentication createAuthentication(ServerWebExchange exchange) {
		AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(key,
				principal, authorities);
		return auth;
     }
   
     // ExceptionTranslationWebFilter

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange)
			.onErrorResume(AccessDeniedException.class, denied -> exchange.getPrincipal()
                                 // executed
				.switchIfEmpty( commenceAuthentication(exchange, denied))
                                 // principal is [AnonymousAuthenticationToken] (executed)
				.flatMap( principal -> this.accessDeniedHandler
					.handle(exchange, denied))
			);
	}

```


This merge request may fix this problem #6590
