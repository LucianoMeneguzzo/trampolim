package com.trampolim.api.config;

import com.trampolim.api.security.JwtAuthenticationFilter;
import com.trampolim.api.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    // Lista separada por vírgula, ex: http://localhost:5173,https://trampolim.vercel.app
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // O console do H2 só existe em desenvolvimento; no perfil de produção fica desligado
    @Value("${spring.h2.console.enabled:false}")
    private boolean consoleH2Ligado;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa o bean corsConfigurationSource abaixo
                .headers(headers -> {
                    // O Spring Security já manda X-Content-Type-Options, Cache-Control e, em HTTPS, o HSTS
                    headers.referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER));
                    headers.permissionsPolicyHeader(permissoes -> permissoes.policy("camera=(), microphone=(), geolocation=()"));
                    if (consoleH2Ligado) {
                        headers.frameOptions(frame -> frame.sameOrigin()); // o H2 Console usa frames da própria origem
                    } else {
                        // A API só devolve JSON: nenhuma página pode carregar scripts dela nem colocá-la num frame
                        headers.frameOptions(frame -> frame.deny());
                        headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"));
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Públicas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll() // a hospedagem confere se o back está de pé

                        // Desafios: ver é público; publicar, editar, fechar e apagar é do mentor
                        .requestMatchers(HttpMethod.GET, "/api/desafios/meus").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.GET, "/api/desafios", "/api/desafios/*").permitAll()
                        .requestMatchers("/api/desafios/**").hasRole("MENTOR")

                        // Candidaturas
                        .requestMatchers(HttpMethod.POST, "/api/candidaturas").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/api/candidaturas/*/envio").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/candidaturas/*/avaliacao").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/candidaturas/*/status").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.GET, "/api/candidaturas/desafio/*").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.GET, "/api/candidaturas/minhas").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/candidaturas/**").authenticated()

                        // Perfil público e métricas abrem sem login; /me é sempre do usuário logado
                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/metricas").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // Sem token / token inválido ou expirado -> 401
                        .authenticationEntryPoint((request, response, e) ->
                                escreverErro(response, HttpServletResponse.SC_UNAUTHORIZED, "Faça login para continuar."))
                        // Logado, mas o perfil não tem permissão -> 403
                        .accessDeniedHandler((request, response, e) ->
                                escreverErro(response, HttpServletResponse.SC_FORBIDDEN, "Seu perfil não tem permissão para esta ação."))
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Mesmo formato do GlobalExceptionHandler: { "erro": "..." }
    private static void escreverErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
