package com.github.kalininaleksandrv.clickleefigleeapi.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class InternalQueueImplementationTest {

    @Spy
    InternalQueueImplementation internalQueueImplementation;

    @Test
    void compareAndAddDifferentNewsCase() {
        internalQueueImplementation.compareAndAdd("news1");
        assertEquals(internalQueueImplementation.getQueueSize(), 1);
    }

    @Test
    void compareAndAddSameNewsCase() {
        internalQueueImplementation.compareAndAdd("news1");
        assertFalse(internalQueueImplementation.compareAndAdd("news1"));
    }

    @Test
    void compareAndAddTrimmedQueue() {
        //проверяем, что при очереди более 300 элементов передача количества элементов в метод trimOueue
        // уменьшает размер очереди на количество добавленных элементов с начала очереди
    }
}