package ru.otus.hw.services;

public interface StoreService {
    void startOrderGeneration();

    void stopOrderGeneration();

    boolean isRunning();
}