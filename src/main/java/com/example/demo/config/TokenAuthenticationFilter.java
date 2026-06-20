package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public TokenAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveAuthentication(request).ifPresent(authentication -> {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<UsernamePasswordAuthenticationToken> resolveAuthentication(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return java.util.Optional.empty();
        }

        String token = authorization.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        if (!token.startsWith("authenticated-")) {
            return java.util.Optional.empty();
        }

        Long userId;
        try {
            userId = Long.parseLong(token.substring("authenticated-".length()));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return java.util.Optional.empty();
        }

        String roleName = user.getRole().getName().toUpperCase(Locale.ROOT);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));
        return java.util.Optional.of(
                new UsernamePasswordAuthenticationToken(user.getId(), null, authorities)
        );
    }
}
