package com.forrrest.authservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "Invalid Method"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "Server Error"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "Invalid Type Value"),

    // User
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "U001", "Email is Duplicated"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "User Not Found"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "Password is Invalid"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Invalid Token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Expired Token"),

    // Profile
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "Profile Not Found"),
    PROFILE_DUPLICATION(HttpStatus.CONFLICT, "P002", "Profile Name is Duplicated");

    private final HttpStatus status;
    private final String code;
    private final String message;
} 