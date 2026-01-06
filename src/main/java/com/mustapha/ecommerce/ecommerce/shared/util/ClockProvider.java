package com.mustapha.ecommerce.ecommerce.shared.util;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Clock Provider
 * Responsibility: Provide current time (testable)
 */
@Component
public class ClockProvider {

    private final Clock clock;

    public ClockProvider() {
        this.clock = Clock.systemDefaultZone();
    }

    public ClockProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
