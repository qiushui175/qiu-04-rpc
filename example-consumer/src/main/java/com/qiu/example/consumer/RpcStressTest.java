package com.qiu.example.consumer;

import com.qiu.example.common.model.User;
import com.qiu.example.common.service.UserService;
import com.qiu.rpc.bootstrap.ConsumerBootstrap;
import com.qiu.rpc.proxy.ServiceProxyFactory;
import com.qiu.rpc.server.tcp.VertxTcpFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC 框架压力测试工具
 * 测试 UserService.getUser() 接口的吞吐量与稳定性
 */
public class RpcStressTest {

    // 可配置参数
    private static final int TOTAL_REQUESTS = 100_0000;      // 总请求数
    private static final int CONCURRENT_THREADS = 100;     // 并发线程数
    private static final int USER_ID_BASE = 1;             // 用户 ID 起始值（可随机或固定）

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 开始初始化 RPC 消费者...");
        ConsumerBootstrap.init();

        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        System.out.printf("📈 启动压力测试：总请求数=%d，并发线程数=%d\n", TOTAL_REQUESTS, CONCURRENT_THREADS);

        // 统计指标
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong failureCount = new AtomicLong(0);
        AtomicLong totalLatencyNanos = new AtomicLong(0); // 累计纳秒，避免浮点误差

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        long startTimeNanos = System.nanoTime();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int userId = USER_ID_BASE + (i % 1000); // 循环使用 1~1000 的用户ID，模拟热点+分散

            executor.submit(() -> {
                long start = System.nanoTime();
                try {
                    User user = userService.getUser(userId);
                    if (user != null && user.getName() != null & !user.getName().isBlank()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet(); // 业务返回 null 视为失败
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    // 可选：记录部分错误日志（避免刷屏）
                    // e.printStackTrace();
                } finally {
                    long latencyNanos = System.nanoTime() - start;
                    totalLatencyNanos.addAndGet(latencyNanos);
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成
        latch.await();
        long endTimeNanos = System.nanoTime();
        executor.shutdown();

        // 计算结果
        long totalTimeMillis = (endTimeNanos - startTimeNanos) / 1_000_000;
        long totalRequests = successCount.get() + failureCount.get();
        double qps = totalTimeMillis > 0 ? (totalRequests * 1000.0) / totalTimeMillis : 0;
        double avgLatencyMs = totalRequests > 0 ? (totalLatencyNanos.get() / 1_000_000.0) / totalRequests : 0;
        double errorRate = totalRequests > 0 ? (failureCount.get() * 100.0) / totalRequests : 0;

        // 打印最终报告
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ RPC 压力测试完成！");
        System.out.println("📊 总请求数: " + totalRequests);
        System.out.println("✅ 成功数:   " + successCount.get());
        System.out.println("❌ 失败数:   " + failureCount.get());
        System.out.printf("📉 错误率:   %.2f%%\n", errorRate);
        System.out.printf("⏱️  总耗时:   %d ms\n", totalTimeMillis);
        System.out.printf("⚡ QPS:      %.2f req/s\n", qps);
        System.out.printf("⏱️  平均延迟: %.2f ms\n", avgLatencyMs);
        System.out.println("=".repeat(60));

        // 优雅关闭
        VertxTcpFactory.shutdown();
        System.out.println("🔌 RPC 客户端已关闭。");
    }
}