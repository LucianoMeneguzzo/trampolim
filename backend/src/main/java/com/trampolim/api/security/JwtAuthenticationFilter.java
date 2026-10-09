package com.trampolim.api.security;

import com.trampolim.api.service.CustomUserDetailsService;
import com.trampolim.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Pega o cabeçalho Authorization da requisição
        final String authHeader = request.getHeader("Authorization");

        // 2. Verifica se o cabeçalho existe e começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove a parte "Bearer ")
        final String jwt = authHeader.substring(7);

        // 4. Extrai o email (subject) do token
        final String userEmail = jwtService.extractUsername(jwt);

        // 5. Se o email foi extraído e ainda não há ninguém autenticado no contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Carrega os detalhes do usuário no banco
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Valida o token com os dados do usuário
            if (jwtService.validateToken(jwt, userDetails.getUsername())) {

                // Cria o objeto de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Define a autenticação no contexto de segurança do Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}