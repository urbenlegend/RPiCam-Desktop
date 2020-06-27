package com.rpicam.util;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Supplier;

public class MemoryPool<T> {
    private ArrayList<T> pool;
    private ArrayBlockingQueue<T> freeList;
    
    public MemoryPool(int size, Supplier<T> supplier) {
        pool = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pool.add(supplier.get());
        }
        
        freeList = new ArrayBlockingQueue<>(size);
        freeList.addAll(pool);
    }
    
    public T get() throws InterruptedException {
        return freeList.take();
    }
    
    public void free(T item) throws InterruptedException {
        if (item == null) {
            return;
        }
        freeList.put(item);
    }
}
