package com.wimi.miro.util;

import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimestampConverter {

    public static Timestamp toFirestoreTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        // ZoneOffset.UTC를 사용하여 시스템 기본 시간대에 의존하지 않도록 수정
        long seconds = localDateTime.toEpochSecond(ZoneOffset.UTC);
        int nanos = localDateTime.getNano();
        return Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        // LocalDateTime.ofInstant 대신 다른 방식으로 변환
        return LocalDateTime.ofEpochSecond(
                timestamp.getSeconds(),
                timestamp.getNanos(),
                ZoneOffset.UTC
        );
    }

}