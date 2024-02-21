package com.enigma.superwallet.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.enigma.superwallet.entity.Admin;
import com.enigma.superwallet.entity.AppUser;
import com.enigma.superwallet.entity.Customer;
import com.enigma.superwallet.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtUtil {

    @Value("${app.super_wallet.jwt.jwt-secret}")
    private String jwtSecret;
    @Value("${app.super_wallet.jwt.app-name}")
    private String appName;
    @Value("${app.super_wallet.jwt.jwtExpirationInSecond}")
    private long jwtExpirationInSecond;
    private final CustomerService customerService;

    public JwtUtil(CustomerService customerService) {
        this.customerService = customerService;
    }

    public String generateToken(AppUser appUser) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Optional<Customer> customer = customerService.getCustomerByUserCredentialId(appUser.getId());
            String customerId = customer.map(Customer::getId).orElse("");
            return JWT.create()
                    .withIssuer(appName)
                    .withSubject(appUser.getId())
                    .withClaim("customerId", customerId)
                    .withExpiresAt(Instant.now().plusSeconds(jwtExpirationInSecond))
                    .withIssuedAt(Instant.now())
                    .withClaim("app", appUser.getRole().name())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException();
        }
    }
    public boolean verifyJwtToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getIssuer().equals(appName);
        } catch (JWTVerificationException e) {
            throw new RuntimeException();
        }
    }
    public Map<String,String> getUserInfoByToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            Map<String,String> userInfo = new HashMap<>();
            userInfo.put("userId",decodedJWT.getSubject());
            userInfo.put("role",decodedJWT.getClaim("role").asString());
            userInfo.put("customerId",decodedJWT.getClaim("customerId").asString());
            return userInfo;
        }catch (JWTVerificationException e) {
            throw new RuntimeException();
        }
    }
}

