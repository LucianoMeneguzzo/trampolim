package com.trampolim.api.config;

import com.trampolim.api.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    // Lista separada por vírgula, ex: http://localhost:5173,https://trampolim.vercel.app
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa o bean corsConfigurationSource abaixo
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/desafios", "/api/desafios/*").permitAll()
                        // Só mentor (área /business do front)
                        .requestMatchers(HttpMethod.POST, "/api/desafios").hasRole("MENTOR")
                        .requestMatchers("/api/candidaturas/desafio/**").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/candidaturas/*/status").hasRole("MENTOR")
                        .requestMatchers(HttpMethod.POST, "/api/candidaturas/*/avaliacao").hasRole("MENTOR")
                        // Só aluno (área /student do front)
                        .requestMatchers(HttpMethod.POST, "/api/candidaturas").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.PUT, "/api/candidaturas/*/envio").hasRole("STUDENT")
                        // Qualquer usuário logado
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        // Sem token / token inválido ou expirado -> 401
                        .authenticationEntryPoint((request, response, e) ->
                                escreverErro(response, HttpServletResponse.SC_UNAUTHORIZED, "Faça login para continuar."))
                        // Logado, mas o perfil não tem permissão -> 403
                        .accessDeniedHandler((request, response, e) ->
                                escreverErro(response, HttpServletResponse.SC_FORBIDDEN, "Seu perfil não tem permissão para esta ação."))
                )
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Mesmo formato do GlobalExceptionHandler: { "erro": "..." }
    private static void escreverErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
