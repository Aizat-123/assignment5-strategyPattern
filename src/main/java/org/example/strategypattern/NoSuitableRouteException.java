package org.example.strategypattern;

public class NoSuitableRouteException extends RuntimeException {
    public NoSuitableRouteException(String message) {
        super(message);
    }
}