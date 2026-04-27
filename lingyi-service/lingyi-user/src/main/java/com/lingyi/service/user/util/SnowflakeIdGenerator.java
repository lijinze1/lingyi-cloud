package com.lingyi.service.user.util;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {

    private static final long CUSTOM_EPOCH = 1704067200000L;
    private static final long WORKER_ID = 1L;
    private static final long DATACENTER_ID = 1L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile long lastTimestamp = -1L;

    public synchronized long nextId() {
        long now = currentTime();
        if (now < lastTimestamp) {
            now = lastTimestamp;
        }
        if (now == lastTimestamp) {
            int seq = (sequence.incrementAndGet()) & (int) MAX_SEQUENCE;
            if (seq == 0) {
                now = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }
        lastTimestamp = now;
        return ((now - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
                | (DATACENTER_ID << DATACENTER_SHIFT)
                | (WORKER_ID << WORKER_SHIFT)
                | (sequence.get() & MAX_SEQUENCE);
    }

    private long waitNextMillis(long timestamp) {
        long now = currentTime();
        while (now <= timestamp) {
            now = currentTime();
        }
        return now;
    }

    private long currentTime() {
        return Instant.now().toEpochMilli();
    }
}
