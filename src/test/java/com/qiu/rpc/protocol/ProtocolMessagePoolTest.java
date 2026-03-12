package com.qiu.rpc.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProtocolMessagePool 对象池单元测试
 */
class ProtocolMessagePoolTest {

    @Test
    void acquireHeaderReturnsNonNull() {
        ProtocolMessage.Header header = ProtocolMessagePool.acquireHeader();
        assertNotNull(header);
    }

    @Test
    void releaseAndAcquireHeaderReusesObject() {
        ProtocolMessage.Header header = ProtocolMessagePool.acquireHeader();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setRequestId(12345L);

        ProtocolMessagePool.releaseHeader(header);

        ProtocolMessage.Header reused = ProtocolMessagePool.acquireHeader();
        // 复用后的对象字段应被重置
        assertEquals(0, reused.getMagic());
        assertEquals(0, reused.getVersion());
        assertEquals(0L, reused.getRequestId());
        assertEquals(0, reused.getBodyLength());
    }

    @Test
    void releaseNullHeaderIsIgnored() {
        // 不应抛出异常
        assertDoesNotThrow(() -> ProtocolMessagePool.releaseHeader(null));
    }
}
