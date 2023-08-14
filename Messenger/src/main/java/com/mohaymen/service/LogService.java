package com.mohaymen.service;

import com.mohaymen.model.entity.Log;
import com.mohaymen.repository.LogRepository;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class LogService {

    private enum Level{INFO, DEBUG, ERROR}
    private final LogRepository logRepository;

    @Setter
    private String logger;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void info(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.INFO.name(), message);
        logRepository.save(log);
    }

    public void error(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.ERROR.name(), message);
        logRepository.save(log);
    }

    public void debug(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.DEBUG.name(), message);
        logRepository.save(log);
    }


}
