package com.expensetracker.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("========== JWT FILTER ==========");
        System.out.println(
                "Request: "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );

        String header =
                request.getHeader("Authorization");

        System.out.println(
                "Authorization header present: "
                        + (header != null)
        );

        // No token
        if (header == null
                || !header.startsWith("Bearer ")) {

            System.out.println("NO JWT TOKEN");

            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        System.out.println(
                "JWT token length: "
                        + token.length()
        );

        // Invalid token
        if (!jwtService.isValid(token)) {

            System.out.println("JWT INVALID");

            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT VALID");

        try {

            String email =
                    jwtService.extractEmail(token);

            System.out.println(
                    "JWT EMAIL: " + email
            );

            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(email);

            System.out.println(
                    "USER LOADED: "
                            + userDetails.getUsername()
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println(
                    "AUTHENTICATED: "
                            + email
            );

        } catch (Exception e) {

            System.out.println(
                    "JWT USER LOAD ERROR: "
                            + e.getMessage()
            );

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(request, response);
    }
}