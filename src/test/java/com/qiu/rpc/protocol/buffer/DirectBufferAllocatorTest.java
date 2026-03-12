package com.qiu.rpc.protocol.buffer;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DirectBufferAllocator 直接内存分配器单元测试
 */
class DirectBufferAllocatorTest {

    @Test
    void allocateWithCapacityReturnsBuffer() {
        Buffer buffer = DirectBufferAllocator.allocate(64);
        assertNotNull(buffer);
        // 新 buffer 长度为 0，容量已预分配
        assertEquals(0, buffer.length());
    }

    @Test
    void allocateDefaultReturnsBuffer() {
        Buffer buffer = DirectBufferAllocator.allocate();
        assertNotNull(buffer);
        assertEquals(0, buffer.length());
    }

    @Test
    void directBufferSupportsReadWrite() {
        Buffer buffer = DirectBufferAllocator.allocate(128);

        buffer.appendByte((byte) 0xBC);
        buffer.appendByte((byte) 0x01);
        buffer.appendLong(12345L);
        buffer.appendInt(42);

        assertEquals((byte) 0xBC, buffer.getByte(0));
        assertEquals((byte) 0x01, buffer.getByte(1));
        assertEquals(12345L, buffer.getLong(2));
        assertEquals(42, buffer.getInt(10));
        // 1 + 1 + 8 + 4 = 14 bytes
        assertEquals(14, buffer.length());
    }

    @Test
    void directBufferSupportsAppendBytes() {
        Buffer buffer = DirectBufferAllocator.allocate(32);

        byte[] data = {1, 2, 3, 4, 5};
        buffer.appendBytes(data);

        assertEquals(5, buffer.length());
        byte[] result = buffer.getBytes(0, 5);
        assertArrayEquals(data, result);
    }

    @Test
    void directBufferGrowsBeyondInitialCapacity() {
        // 初始容量为 8，写入更多数据应自动扩展
        Buffer buffer = DirectBufferAllocator.allocate(8);

        byte[] data = new byte[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 128);
        }
        buffer.appendBytes(data);

        assertEquals(256, buffer.length());
        byte[] result = buffer.getBytes(0, 256);
        assertArrayEquals(data, result);
    }
}
