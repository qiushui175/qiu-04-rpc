package com.qiu.rpc.pool;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ObjectPool 对象池单元测试
 */
class ObjectPoolTest {

    @Test
    void acquireFromEmptyPoolCreatesNewObject() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> sb.setLength(0),
                10
        );

        StringBuilder sb = pool.acquire();
        assertNotNull(sb);
        assertEquals(0, pool.size());
    }

    @Test
    void releaseAndAcquireReusesObject() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> sb.setLength(0),
                10
        );

        StringBuilder sb1 = pool.acquire();
        sb1.append("hello");
        pool.release(sb1);
        assertEquals(1, pool.size());

        StringBuilder sb2 = pool.acquire();
        // 复用的对象应该被 reset 过，内容为空
        assertEquals(0, sb2.length());
        // 应该是同一个对象实例
        assertSame(sb1, sb2);
        assertEquals(0, pool.size());
    }

    @Test
    void releaseNullIsIgnored() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> sb.setLength(0),
                10
        );

        pool.release(null);
        assertEquals(0, pool.size());
    }

    @Test
    void poolRespectsMaxSize() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> sb.setLength(0),
                2
        );

        StringBuilder sb1 = pool.acquire();
        StringBuilder sb2 = pool.acquire();
        StringBuilder sb3 = pool.acquire();

        pool.release(sb1);
        pool.release(sb2);
        // 池已满（maxSize=2），sb3 应该被丢弃
        pool.release(sb3);

        // 池大小不应超过 maxSize
        assertTrue(pool.size() <= 2);
    }

    @Test
    void concurrentAcquireAndRelease() throws InterruptedException {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> sb.setLength(0),
                64
        );

        int threadCount = 8;
        int opsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        StringBuilder sb = pool.acquire();
                        sb.append("data");
                        pool.release(sb);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        // 确保池大小不超过 maxSize
        assertTrue(pool.size() <= 64);
    }

    @Test
    void resetFunctionIsCalledOnRelease() {
        int[] resetCount = {0};
        ObjectPool<StringBuilder> pool = new ObjectPool<>(
                StringBuilder::new,
                sb -> {
                    sb.setLength(0);
                    resetCount[0]++;
                },
                10
        );

        StringBuilder sb = pool.acquire();
        sb.append("test");
        pool.release(sb);

        assertEquals(1, resetCount[0]);
        assertEquals(0, sb.length());
    }
}
