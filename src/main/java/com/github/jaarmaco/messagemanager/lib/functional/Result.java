package com.github.jaarmaco.messagemanager.lib.functional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.util.function.ThrowingSupplier;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class Result<T> {

    private T value = null;
    private Throwable exception = null;

    private Result() {}

    @Override
    public String toString() {

        if (value != null) {
            return String.format("Result.success(%s)", value);
        }

        if (exception != null) {
            return String.format("Result.exception(%s)",
                    exception.getClass().getSimpleName());
        }

        return "Result.empty()";
    }

    public void onSuccess(@NonNull Consumer<T> consumer) {
        if (isSuccess()) {
            consumer.accept(value);
        }
    }

    public void throwIfHasException() {
        if (exception instanceof RuntimeException rte) {
            throw rte;
        } else if (exception != null) {
            throw new RuntimeException(exception);
        }
    }

    public void ignoringErrors() {}

    public void requireNonEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("Empty result");
        }
    }

    public T requireSuccess() {
        return unwrapException().orElseThrow();
    }

    public Optional<T> unwrapException() {
        throwIfHasException();
        return Optional.ofNullable(value);
    }

    public T orElse(@NonNull T orElseValue) {
        return isSuccess() ? value : orElseValue;
    }

    public T orElseGet(@NonNull Supplier<T> orElseGetter) {
        return isSuccess() ? value : orElseGetter.get();
    }

    public <E extends RuntimeException> T orElseThrow(@NonNull Supplier<E> exceptionGetter) {
        if (isSuccess()) {
            return value;
        }
        throw exceptionGetter.get();
    } 

    public Result<T> require(@NonNull Predicate<T> condition) {
        
        if (!isSuccess()) {
            return this;
        }

        if (!condition.test(value)) {
            return empty();
        }

        return this;
    }

    public <R> Result<R> map(@NonNull Function<T, R> mapper) {
        
        if (isEmpty()) {
            return empty();
        }

        if (isException()) {
            return exception(exception);
        }

        try {
            return ofNullable(mapper.apply(value));
        } catch (Throwable e) {
            return exception(e);
        }
    }

    public <R> Result<R> andThenFromOptional(@NonNull Function<T, Optional<R>> mapper) {
        return andThen(it -> fromOptional(mapper.apply(it)));
    }

    public <R> Result<R> andThen(@NonNull Function<T, Result<R>> mapper) {
        
        if (isEmpty()) {
            return empty();
        }

        if (isException()) {
            return exception(exception);
        }

        Result<R> result;
        try {
            result = mapper.apply(value);
        } catch (Throwable e) {
            return exception(e);
        }

        if (result == null) {
            return empty();
        }

        if (result.isException()) {
            return exception(result.exception);
        }

        return ofNullable(result.value);
    }

    public boolean isSuccess() {
        return value != null && exception == null;
    }

    public boolean isFailure() {
        return value == null || exception != null;
    }

    public boolean isException() {
        return exception != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public static <T> Result<T> doTry(@NonNull ThrowingSupplier<T> action) { 
        try {
            return ofNullable(action.get());
        } catch (Throwable e) {
            return exception(e);
        }
    }

    public static <T> Result<T> of(@NonNull T value) {
        return ofNullable(value);
    }

    public static <T> Result<T> ofNullable(T value) { 
        Result<T> result = new Result<>();
        result.value = value;
        return result;
    }

    public static <T> Result<T> fromOptional(Optional<T> value) {
        return value.map(Result::of).orElseGet(Result::empty);
    }

    public static <T> Result<T> empty() {
        return exception(null);
    }

    public static <T, E extends Throwable> Result<T> exception(E exception) {
        Result<T> result = new Result<>();
        result.exception = exception;
        return result;
    }
}

