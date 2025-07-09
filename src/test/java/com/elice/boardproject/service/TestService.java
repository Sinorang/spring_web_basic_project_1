package com.elice.boardproject.service;

import org.springframework.stereotype.Service;

@Service
public class TestService {
    public String echo(String input) {
        return input;
    }
    public void throwException() {
        throw new IllegalStateException("테스트 예외");
    }
} 