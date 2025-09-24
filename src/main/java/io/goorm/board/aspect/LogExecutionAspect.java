package io.goorm.board.aspect;

import io.goorm.board.annotation.LogExecution;
import io.goorm.board.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 메소드 실행 로깅 AOP
 * @LogExecution 어노테이션이 있는 메소드의 실행을 자동으로 로깅
 */
@Aspect
@Component
@Slf4j
public class LogExecutionAspect {

    @Around("@annotation(logExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 현재 사용자 정보 가져오기
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // 실행 전 로깅
        logMethodStart(logExecution, username, className, methodName, args);

        Object result = null;
        Exception thrownException = null;

        try {
            // 실제 메소드 실행
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            thrownException = e;
            throw e;
        } finally {
            // 실행 후 로깅
            long executionTime = System.currentTimeMillis() - startTime;
            logMethodEnd(logExecution, username, className, methodName, result, thrownException, executionTime);
        }
    }

    private void logMethodStart(LogExecution logExecution, String username, String className, String methodName, Object[] args) {
        String operation = logExecution.operation().isEmpty() ? methodName : logExecution.operation();
        String resource = logExecution.resource().isEmpty() ? className : logExecution.resource();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[AUDIT] ")
                  .append("User: ").append(username)
                  .append(" | Operation: ").append(operation)
                  .append(" | Resource: ").append(resource)
                  .append(" | Method: ").append(className).append(".").append(methodName);

        // 파라미터 정보 추가 (민감한 정보 제외)
        if (args != null && args.length > 0) {
            logMessage.append(" | Params: ").append(getSafeParameterInfo(args));
        }

        logMessage.append(" | Status: STARTED");

        logByLevel(logExecution.level(), logMessage.toString());
    }

    private void logMethodEnd(LogExecution logExecution, String username, String className, String methodName,
                             Object result, Exception exception, long executionTime) {
        String operation = logExecution.operation().isEmpty() ? methodName : logExecution.operation();
        String resource = logExecution.resource().isEmpty() ? className : logExecution.resource();

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[AUDIT] ")
                  .append("User: ").append(username)
                  .append(" | Operation: ").append(operation)
                  .append(" | Resource: ").append(resource)
                  .append(" | Method: ").append(className).append(".").append(methodName);

        if (exception != null) {
            logMessage.append(" | Status: FAILED")
                     .append(" | Error: ").append(exception.getClass().getSimpleName())
                     .append(" | Message: ").append(exception.getMessage());
        } else {
            logMessage.append(" | Status: SUCCESS");
            if (result != null) {
                logMessage.append(" | Result: ").append(getSafeResultInfo(result));
            }
        }

        if (logExecution.measureTime()) {
            logMessage.append(" | ExecutionTime: ").append(executionTime).append("ms");
        }

        logByLevel(logExecution.level(), logMessage.toString());
    }

    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getEmail();
            }
            return principal.toString();
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String getSafeParameterInfo(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    if (arg instanceof String && arg.toString().length() > 100) {
                        return arg.toString().substring(0, 100) + "...";
                    }
                    if (arg instanceof User) {
                        return "User[" + ((User) arg).getEmail() + "]";
                    }
                    return arg.getClass().getSimpleName();
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }

    private String getSafeResultInfo(Object result) {
        if (result == null) return "null";
        if (result instanceof String && result.toString().length() > 100) {
            return result.toString().substring(0, 100) + "...";
        }
        return result.getClass().getSimpleName();
    }

    private void logByLevel(LogExecution.LogLevel level, String message) {
        switch (level) {
            case DEBUG -> log.debug(message);
            case INFO -> log.info(message);
            case WARN -> log.warn(message);
            case ERROR -> log.error(message);
        }
    }
}