package com.qiu.rpc.protocol.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;

/**
 * 基于 Netty Direct Memory 的零拷贝 Buffer 分配器。
 * <p>
 * 使用堆外直接内存（Direct Memory），避免数据在 JVM 堆与内核态之间的额外拷贝，
 * 在高并发网络 I/O 场景下可显著降低 CPU 开销与 GC 压力。
 *
 * @author qiu
 */
public class DirectBufferAllocator {

    private DirectBufferAllocator() {
    }

    /**
     * 分配指定初始容量的 Direct Memory Buffer。
     * <p>
     * 底层使用 Netty 的 {@link Unpooled#directBuffer(int)} 创建堆外直接内存缓冲区，
     * 并包装为 Vert.x {@link Buffer} 以兼容现有编解码流程。
     *
     * @param initialCapacity 初始容量（字节数）
     * @return 基于 Direct Memory 的 Vert.x Buffer
     */
    public static Buffer allocate(int initialCapacity) {
        ByteBuf directBuf = Unpooled.directBuffer(initialCapacity);
        return BufferImpl.buffer(directBuf);
    }

    /**
     * 分配默认容量的 Direct Memory Buffer。
     *
     * @return 基于 Direct Memory 的 Vert.x Buffer
     */
    public static Buffer allocate() {
        return allocate(256);
    }
}
