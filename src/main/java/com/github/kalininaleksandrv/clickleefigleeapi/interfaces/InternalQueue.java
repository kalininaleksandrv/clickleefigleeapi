package com.github.kalininaleksandrv.clickleefigleeapi.interfaces;

public interface InternalQueue {
    boolean compareAndAdd(String newsId);
    void trimQueue(int numberOfRemovedElements);
}
