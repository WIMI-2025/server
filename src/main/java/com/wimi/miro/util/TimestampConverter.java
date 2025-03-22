package com.wimi.miro.util;

import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimestampConverter {

    public static Timestamp toFirestoreTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        long seconds = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        int nanos = localDateTime.getNano();
        return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochSecond(
                        timestamp.getSeconds(),
                        timestamp.getNanos()
                ),
                ZoneId.systemDefault()
        );
    }

    public static Timestamp now() {
        return Timestamp.now();
    }
}