package com.qiu.rpc.registry;

import cn.hutool.json.JSONUtil;
import com.qiu.rpc.config.RegistryConfig;
import com.qiu.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
public class EtcdRegistry implements Registry {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Client client = Client.builder().endpoints("http://localhost:2379").build();
        KV kvClient = client.getKVClient();

        ByteSequence key = ByteSequence.from("sample_key".getBytes());
        ByteSequence value = ByteSequence.from("sample_value".getBytes());

        kvClient.put(key, value).get();

        CompletableFuture<GetResponse> getResponseCompletableFuture = kvClient.get(key);

        GetResponse getResponse = getResponseCompletableFuture.get();

        System.out.println(getResponse.getKvs().get(0).getValue());
    }


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
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 创建租约
        Lease leaseClient = client.getLeaseClient();
        long id = leaseClient.grant(30).get().getID();

        // 注册服务节点
        String registryPath = ETCD_REGISTRY_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryPath, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 绑定租约进行注册
        kvClient.put(key, value, io.etcd.jetcd.options.PutOption.newBuilder().withLeaseId(id).build()).get();
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(
                ByteSequence.from(ETCD_REGISTRY_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8)
        );
    }

    @Override
    public List<ServiceMetaInfo> serverDiscovery(String serviceKey) {
        String searchPath = String.format("%s%s/", ETCD_REGISTRY_ROOT_PATH, serviceKey);
        ByteSequence prefix = ByteSequence.from(searchPath, StandardCharsets.UTF_8);

        List<ServiceMetaInfo> serviceMetaInfos = new ArrayList<>();
        try {
            GetResponse getResponse = kvClient.get(prefix, GetOption.newBuilder().isPrefix(true).build()).get();
            getResponse.getKvs().forEach(kv -> {
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                serviceMetaInfos.add(serviceMetaInfo);
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return serviceMetaInfos;
    }

    @Override
    public void destroy() {
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
