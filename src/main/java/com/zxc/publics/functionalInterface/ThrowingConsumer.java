package com.zxc.publics.functionalInterface;

import java.util.Objects;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

    void accept(T t) throws E;

    default ThrowingConsumer<T, E> andThen(ThrowingConsumer<? super T, ? super E> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            try {
                accept(t);
                after.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
