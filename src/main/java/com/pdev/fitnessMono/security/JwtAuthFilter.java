package com.pdev.fitnessMono.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
     /* extends OncePerRequestFilter: fileter for filterChain ,
         that intercepts each request and check if the request has a valid JWT token,
         if it does then it will set the authentication in the security context */

    @Autowired
    private JwtUtils jwtUtils;

    /* doFilterInternal: This method is called for each incoming HTTP request. It tries to parse the JWT token
       from the request header, validate it, and if valid, it extracts the user ID and roles from the token.
       It then creates an authentication object and sets it in the security context, allowing Spring Security to
       recognize the user as authenticated for the duration of the request. Finally, it continues the filter
       chain to allow the request to reach its intended destination (e.g., controller). */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JwtAuthFilter: doFilterInternal called");

        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                System.out.println("TOKEN IS VALID: " + jwt);
                String userId = jwtUtils.getUserIdFromJwtToken(jwt);

                Claims claims = jwtUtils.getAllClaims(jwt);
                List<String> roles = claims.get("roles", List.class);
                System.out.println("ROLES:" + roles);
                List<GrantedAuthority> authorities = List.of();
                // If roles are present, convert them to GrantedAuthority objects for Spring Security to use for authorization decisions.
                if (roles!=null){
                    authorities = roles.stream()
                            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                            .toList();
                }

                /* Create an authentication token with the user ID and authorities, and set it in the security context.
                This allows Spring Security to recognize the user as authenticated for the duration of the request. */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /**
         * CRITICAL: Continue filter chain after setting SecurityContext.
         * Without this, Spring Security will not enforce authentication/authorization properly.
         **/
        // CRITICAL: Without this, the request never reaches next filters/controller (causes 200 issues)
        filterChain.doFilter(request, response);

    }

    private String parseJwt(HttpServletRequest request) {
        return jwtUtils.getJwtFromHeader(request);
    }

}
