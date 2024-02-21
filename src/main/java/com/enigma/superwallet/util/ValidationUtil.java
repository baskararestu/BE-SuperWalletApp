package com.enigma.superwallet.util;

import com.enigma.superwallet.constant.ECurrencyCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final Validator validator;

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> result = validator.validate(object);
        if (!result.isEmpty()) {
            throw new ConstraintViolationException(result);
        }
    }

    public boolean isValidDate(String date) {
        if (StringUtils.isEmpty(date)) {
            return false;
        }
        try {
            validator.validate(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    public boolean isValidCode(String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) {
            return true;
        }
        for (ECurrencyCode code : ECurrencyCode.values()) {
            if (code.name().equals(currencyCode)) {
                return false;
            }
        }
        return true;
    }
    public String extractTokenFromHeader() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Remove "Bearer " prefix
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is missing or invalid");
        }
    }
}
