package com.qiu.rpc.server.tcp;

import com.qiu.rpc.model.RpcResponse;
import com.qiu.rpc.protocol.ProtocolMessage;
import com.qiu.rpc.protocol.ProtocolMessageDecoder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class TcpResponseHandler {

    private static final int HEADER_SIZE = 17;

    public static void init(NetSocket socket) {
        socket.handler(new io.vertx.core.Handler<Buffer>() {
            private Buffer recordBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                recordBuffer.appendBuffer(buffer);

                while (true) {
                    if (recordBuffer.length() < HEADER_SIZE) {
                        break;
                    }

                    int bodyLength = recordBuffer.getInt(13);

                    if (bodyLength < 0) {
                        log.error("非法的 bodyLength: {}，丢弃所有数据并重置", bodyLength);
                        recordBuffer = Buffer.buffer();
                        break;
                    }

                    int totalLength = HEADER_SIZE + bodyLength;

                    if (recordBuffer.length() < totalLength) {
                        break;
                    }

                    // ====== 修复核心 ======
                    // 用 getBuffer() 拷贝出完整消息，而不是 slice（slice 是共享视图，不可扩容）
                    Buffer fullMessageBuffer = recordBuffer.getBuffer(0, totalLength);

                    // 用 getBuffer() 拷贝出剩余数据到新的独立 Buffer
                    // 这样新 Buffer 的底层 ByteBuf 是独立的，可以正常 append 扩容
                    if (recordBuffer.length() > totalLength) {
                        recordBuffer = recordBuffer.getBuffer(totalLength, recordBuffer.length());
                    } else {
                        // 刚好消费完，直接重置为空 buffer
                        recordBuffer = Buffer.buffer();
                    }
                    // =====================

                    try {
                        @SuppressWarnings("unchecked")
                        ProtocolMessage<RpcResponse> msg =
                                (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(fullMessageBuffer);

                        long requestId = msg.getHeader().getRequestId();
                        CompletableFuture<RpcResponse> future =
                                PendingRequestManager.PENDING_REQUESTS.remove(requestId);

                        if (future != null) {
                            future.complete(msg.getBody());
                        } else {
                            log.warn("收到响应但未找到对应 requestId: {} (可能已超时)", requestId);
                        }
                    } catch (Exception e) {
                        log.error("TCP 响应解码异常，丢弃该包", e);
                    }
                }
            }
        });
    }
}