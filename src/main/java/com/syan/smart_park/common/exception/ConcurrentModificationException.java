package com.syan.smart_park.common.exception;

/**
 * 并发修改异常
 * 当乐观锁版本冲突时抛出，触发 {@link org.springframework.retry.annotation.Retryable} 重试
 */
public class ConcurrentModificationException extends RuntimeException {

    public ConcurrentModificationException(String message) {
        super(message);
    }
}
