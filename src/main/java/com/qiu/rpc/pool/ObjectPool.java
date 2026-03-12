package com.qiu.rpc.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 通用线程安全对象池，用于高并发场景下复用对象，减少 GC 频率与系统抖动。
 * <p>
 * 基于 {@link ConcurrentLinkedQueue} 实现无锁并发访问，
 * 通过 {@link AtomicInteger} 控制池大小上限，防止内存无限增长。
 *
 * @param <T> 池化对象类型
 * @author qiu
 */
public class ObjectPool<T> {

    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private final int maxSize;
    private final AtomicInteger currentSize;

    /**
     * @param factory 对象工厂，用于在池为空时创建新对象
     * @param reset   对象重置函数，在归还对象前清理状态
     * @param maxSize 池中最大缓存对象数
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> reset, int maxSize) {
        this.pool = new ConcurrentLinkedQueue<>();
        this.factory = factory;
        this.reset = reset;
        this.maxSize = maxSize;
        this.currentSize = new AtomicInteger(0);
    }

    /**
     * 从池中获取一个对象，如果池为空则通过工厂创建新对象。
     *
     * @return 可复用对象
     */
    public T acquire() {
        T obj = pool.poll();
        if (obj != null) {
            currentSize.decrementAndGet();
            return obj;
        }
        return factory.get();
    }

    /**
     * 将对象归还到池中。归还前会调用 reset 函数清理对象状态。
     * 如果池已满，则直接丢弃该对象，由 GC 回收。
     *
     * @param obj 待归还的对象
     */
    public void release(T obj) {
        if (obj == null) {
            return;
        }
        if (currentSize.get() < maxSize) {
            reset.accept(obj);
            pool.offer(obj);
            currentSize.incrementAndGet();
        }
    }

    /**
     * 获取当前池中缓存的对象数量。
     *
     * @return 池中对象数
     */
    public int size() {
        return currentSize.get();
    }
}
