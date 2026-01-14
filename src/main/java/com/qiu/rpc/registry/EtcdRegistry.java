package com.qiu.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author qiu
 * @version 1.0
 * @className EtcdRegistry
 * @packageName com.qiu.rpc.registry
 * @Description
 * @date 2026/1/13 21:10
 * @since 1.0
 */
@Slf4j
public class EtcdRegistry implements Registry {

    private final Set<String> localRegistryCache = new HashSet<>();

    private Client client;
    private KV kvClient;

    /**
     * /rpc_registry/[服务名]:[版本]:[分组]/[服务IP]:[端口]
     */
    private static final String ETCD_REGISTRY_ROOT_PATH = "/rpc_registry/";

    @Override
    public void init(RegistryConfig registryConfig) {
        this.client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        this.kvClient = client.getKVClient();
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo)
            throws ExecutionException, InterruptedException {

        // 创建租约
        Lease leaseClient = client.getLeaseClient();
        long id = leaseClient.grant(30).get().getID();

        // 注册服务节点
        String registryPath = ETCD_REGISTRY_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryPath, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(
                JSONUtil.toJsonStr(serviceMetaInfo),
                StandardCharsets.UTF_8
        );

        // 绑定租约进行注册（低频操作，保留 get）
        kvClient.put(
                key,
                value,
                io.etcd.jetcd.options.PutOption.newBuilder()
                        .withLeaseId(id)
                        .build()
        ).get();

        // 添加节点到本地缓存
        localRegistryCache.add(registryPath);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String serviceKey = ETCD_REGISTRY_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();

        // 低频操作，保留同步
        kvClient.delete(ByteSequence.from(serviceKey, StandardCharsets.UTF_8));

        localRegistryCache.remove(serviceKey);
    }

    /**
     * ===== 优化点 1：serverDiscovery 使用 join =====
     */
    @Override
    public List<ServiceMetaInfo> serverDiscovery(String serviceKey) {
        String searchPath = ETCD_REGISTRY_ROOT_PATH + serviceKey + "/";
        ByteSequence prefix = ByteSequence.from(searchPath, StandardCharsets.UTF_8);

        List<ServiceMetaInfo> serviceMetaInfos = new ArrayList<>();

        GetResponse response = kvClient
                .get(prefix, GetOption.newBuilder().isPrefix(true).build())
                .join(); // <-- 替换 get()

        response.getKvs().forEach(kv -> {
            String value = kv.getValue().toString(StandardCharsets.UTF_8);
            serviceMetaInfos.add(JSONUtil.toBean(value, ServiceMetaInfo.class));
        });

        return serviceMetaInfos;
    }

    /**
     * ===== 优化点 2：destroy 批量 delete + join =====
     */
    @Override
    public void destroy() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String key : localRegistryCache) {
            ByteSequence etcdKey = ByteSequence.from(key, StandardCharsets.UTF_8);
            futures.add(kvClient.delete(etcdKey));
        }

        // 统一等待
        futures.forEach(CompletableFuture::join);

        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    /**
     * ===== 优化点 3：heartBeat 中避免循环内串行 get =====
     */
    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {

                List<String> keys = new ArrayList<>(localRegistryCache);
                List<CompletableFuture<GetResponse>> futures = new ArrayList<>();

                // 并行发请求
                for (String key : keys) {
                    ByteSequence etcdKey = ByteSequence.from(key, StandardCharsets.UTF_8);
                    futures.add(kvClient.get(etcdKey));
                }

                // 统一 join
                for (int i = 0; i < futures.size(); i++) {
                    String key = keys.get(i);
                    GetResponse response = futures.get(i).join();

                    List<KeyValue> kvs = response.getKvs();
                    if (CollUtil.isEmpty(kvs)) {
                        log.info("Service node expired, need to re-register: {}", key);
                        continue;
                    }

                    KeyValue kv = kvs.get(0);
                    String value = kv.getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfo serviceMetaInfo =
                            JSONUtil.toBean(value, ServiceMetaInfo.class);

                    try {
                        register(serviceMetaInfo);
//                        log.info("Service node renewed: {}", key);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}
