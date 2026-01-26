package com.badmintonshop.scheduler;

import com.badmintonshop.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs every day at midnight to remove expired refresh tokens.
     * Cron expression: "0 0 0 * * *" (At 00:00:00am every day)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(now);
        
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired refresh tokens at {}", deletedCount, now);
        }
    }
}