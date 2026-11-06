package com.example.internshiptracker.service;

public class RecordLimitException extends RuntimeException {
    public RecordLimitException(long limit) {
        super("The demo holds at most " + limit + " applications. Delete one, or wait for the next reset.");
    }
}
