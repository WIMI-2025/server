package com.wimi.miro.controller.advice;

import com.wimi.miro.util.DiscordNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier discordNotifier;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "잘못된 요청입니다");
        errorResponse.put("details", ex.getMessage());

        // 400 에러도 디스코드에 알림을 보내고 싶다면 주석 해제
        // discordNotifier.sendErrorNotification("IllegalArgumentException: " + ex.getMessage(), ex).subscribe();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Map<String, String>> handleExecutionException(ExecutionException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "데이터베이스 처리 중 오류가 발생했습니다");
        errorResponse.put("details", ex.getMessage());

        // 서버 에러 발생 시 디스코드로 알림
        logAndNotifyError("ExecutionException", ex);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "파일 크기가 너무 큽니다.");
        errorResponse.put("details", ex.getMessage());

        discordNotifier.sendErrorNotification("MaxUploadSizeExceededException: " + ex.getMessage(), ex).subscribe();

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "서버 오류가 발생했습니다");
        errorResponse.put("details", ex.getMessage());

        // 서버 에러 발생 시 디스코드로 알림
        logAndNotifyError("UnhandledException", ex);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<Map<String, String>> handleInterruptedException(InterruptedException ex) {
        // 인터럽트 상태 복원
        Thread.currentThread().interrupt();

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "요청 처리가 중단되었습니다");
        errorResponse.put("details", ex.getMessage());

        // 서버 에러 발생 시 디스코드로 알림
        logAndNotifyError("InterruptedException", ex);

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * 에러를 로깅하고 디스코드로 알림을 보내는 헬퍼 메서드
     */
    private void logAndNotifyError(String errorType, Exception ex) {
        // 로그에 에러 기록
        log.error("[{}] 서버 에러 발생: {}", errorType, ex.getMessage(), ex);

        // 디스코드로 알림 발송
        discordNotifier.sendErrorNotification(errorType + ": " + ex.getMessage(), ex)
                .subscribe(
                        response -> log.debug("Discord Notify Success: {}", response),
                        error -> log.error("Discord Notify Fail", error)
                );
    }
}