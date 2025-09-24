package io.goorm.board.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메소드 실행 로깅을 위한 어노테이션
 * 누가, 언제, 무엇을 실행했는지 자동으로 기록
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {

    /**
     * 로깅할 작업 유형
     */
    String operation() default "";

    /**
     * 로깅할 리소스 유형 (예: ORDER, PRODUCT, USER)
     */
    String resource() default "";

    /**
     * 로그 레벨 설정
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 실행 시간 측정 여부
     */
    boolean measureTime() default true;

    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}