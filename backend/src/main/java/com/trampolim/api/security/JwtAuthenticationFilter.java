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

        final String authHeader = request.getHeader("Authorization");
        System.out.println("=== DEBUG FILTRO JWT ===");
        System.out.println("1. Cabeçalho Authorization recebido: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("2. Sem token ou formato inválido. Seguindo sem autenticar.");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("3. Token JWT extraído (início): " + jwt.substring(0, Math.min(30, jwt.length())) + "...");

        final String userEmail = jwtService.extractUsername(jwt);
        System.out.println("4. E-mail extraído do token: " + userEmail);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("5. UserDetails carregado do banco para: " + userDetails.getUsername());

                boolean isValid = jwtService.validateToken(jwt, userDetails.getUsername());
                System.out.println("6. O token é válido para este usuário? " + isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("7. SUCESSO: Autenticação definida no SecurityContextHolder!");
                } else {
                    System.out.println("7. FALHA: A validação do token retornou false.");
                }
            } catch (Exception e) {
                System.out.println("7. ERRO: Exceção ao carregar usuário ou validar: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("======================");

        filterChain.doFilter(request, response);
    }
}