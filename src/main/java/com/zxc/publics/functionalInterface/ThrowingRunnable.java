package com.zxc.publics.functionalInterface;

public interface ThrowingRunnable<E extends Exception> {

    public abstract void run() throws E;

}
