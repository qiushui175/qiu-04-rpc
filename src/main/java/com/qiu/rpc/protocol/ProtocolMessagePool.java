package com.qiu.rpc.protocol;

import com.qiu.rpc.pool.ObjectPool;

/**
 * 协议消息对象池，复用 {@link ProtocolMessage.Header} 对象，
 * 在高并发场景下减少频繁创建/销毁带来的 GC 压力。
 *
 * @author qiu
 */
public class ProtocolMessagePool {

    private static final int DEFAULT_POOL_SIZE = 256;

    private static final ObjectPool<ProtocolMessage.Header> HEADER_POOL = new ObjectPool<>(
            ProtocolMessage.Header::new,
            header -> {
                header.setMagic((byte) 0);
                header.setVersion((byte) 0);
                header.setSerializationType((byte) 0);
                header.setMessageType((byte) 0);
                header.setStatus((byte) 0);
                header.setRequestId(0L);
                header.setBodyLength(0);
            },
            DEFAULT_POOL_SIZE
    );

    /**
     * 从池中获取一个 Header 对象。
     *
     * @return 可复用的 Header 对象
     */
    public static ProtocolMessage.Header acquireHeader() {
        return HEADER_POOL.acquire();
    }

    /**
     * 将 Header 对象归还到池中。
     *
     * @param header 待归还的 Header 对象
     */
    public static void releaseHeader(ProtocolMessage.Header header) {
        HEADER_POOL.release(header);
    }

    /**
     * 获取 Header 池当前缓存的对象数量。
     *
     * @return 池中 Header 数
     */
    public static int headerPoolSize() {
        return HEADER_POOL.size();
    }
}
