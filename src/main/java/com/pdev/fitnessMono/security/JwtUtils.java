package com.pdev.fitnessMono.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtils {

    private String jwtSecret = "YS1zdHJpbmctc2VjcmV0LWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmc=";
    private int jwtExpirationMs = 172800000;

        /* getJwtFromHeader: extracts the JWT token from the Authorization header
        of the HTTP request. It checks if the header is present and starts
        with "Bearer ", and if so, it returns the token part of the header.
        If the header is not present or does not start with "Bearer ",
        it returns null. */
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if ( bearerToken!= null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /* generateToken: generates a JWT token for a given user ID and role. The token includes the user
    ID as the subject and the role as a claim. It also sets the issued at and expiration times,
    and signs the token with a secret key. */
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("roles", List.of(role))
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /* validateJwtToken: validates a JWT token by parsing it with the secret key.
    If the token is valid, it returns true. If the token is invalid, it catches
    the exception and logs an error message, and returns false. */
    public boolean validateJwtToken(String jwtToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build()
                    .parseSignedClaims(jwtToken);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: " + e.getMessage());
        }
        return true;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /* getUserIdFromJwtToken: extracts the user ID from a JWT token by parsing the token with the
    secret key and retrieving the subject from the token's claims. The subject is typically
    set to the user ID when the token is generated. */
    public String getUserIdFromJwtToken(String jwtToken) {
        return Jwts.parser().verifyWith((SecretKey) key()).build()
                .parseSignedClaims(jwtToken)
                .getPayload().getSubject();
    }

    /* getAllClaims: extracts all claims from a JWT token by parsing the token with the secret key and
    retrieving the payload, which contains all the claims. This method can be used to access any
    additional information stored in the token, such as roles or other custom claims. */
    public Claims getAllClaims(String jwtToken) {
        return Jwts.parser().verifyWith((SecretKey) key()).build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }
}
