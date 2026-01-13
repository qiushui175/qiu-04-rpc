package com.qiu.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.qiu.rpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiu
 * @version 1.0
 * @className SpiLoader
 * @packageName com.qiu.rpc.spi
 * @Description
 * @date 2026/1/13 19:40
 * @since 1.0
 */
@Slf4j
public class SpiLoader {

    /**
     * Cache for loaded SPI classes
     */
    private static Map<String, Map<String, Class<?>>> cachedClasses = new ConcurrentHashMap<>();

    private static Map<String, Object> cachedInstances = new ConcurrentHashMap<>();

    private static final String RPC_SPI_SYSTEM_DIR = "META-INF/rpc/system";
    private static final String RPC_SPI_CUSTOM_DIR = "META-INF/rpc/custom";

    private static final String[] SCAN_DIRS = new String[]{
            RPC_SPI_SYSTEM_DIR,
            RPC_SPI_CUSTOM_DIR
    };

    /**
     * List of classes that need to be loaded as SPI implementations
     */
    private static final List<Class<?>> LOADED_CLASS_LIST = Arrays.asList(Serializer.class);

    public static void loadALl() {
        for (Class<?> clazz : LOADED_CLASS_LIST) {
            load(clazz);
        }
    }

    public static Map<String, Class<?>> load(Class<?> clazz) {
        log.info("Loading SPI class: {}", clazz.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String dir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(dir  + "/" + clazz.getName());

            // Load each resource file
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStream = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStream);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.trim();
                        // Skip comments and empty lines
                        if (line.startsWith("#") || line.isEmpty()) {
                            continue;
                        }
                        String[] parts = line.split("=");
                        if (parts.length != 2) {
                            log.warn("Invalid SPI configuration line: {}", line);
                            continue;
                        }
                        String key = parts[0].trim();
                        String className = parts[1].trim();
                        try {
                            Class<?> implClass = Class.forName(className);
                            keyClassMap.put(key, implClass);
                            log.info("Loaded SPI implementation: key={}, class={}", key, className);
                        } catch (ClassNotFoundException e) {
                            log.error("SPI implementation class not found: {}", className, e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error loading SPI class from resource: {}", resource, e);
                }
            }
        }

        cachedClasses.put(clazz.getName(), keyClassMap);
        return keyClassMap;
    }


    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz, String key) {
        String instanceKey = clazz.getName() + "#" + key;
        if (cachedInstances.containsKey(instanceKey)) {
            return (T) cachedInstances.get(instanceKey);
        }

        Map<String, Class<?>> keyClassMap = cachedClasses.get(clazz.getName());
        if (keyClassMap == null) {
            keyClassMap = load(clazz);
        }

        Class<?> implClass = keyClassMap.get(key);
        if (implClass == null) {
            throw new RuntimeException("No SPI implementation found for key: " + key);
        }

        try {
            T instance = (T) implClass.getDeclaredConstructor().newInstance();
            cachedInstances.put(instanceKey, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate SPI implementation: " + implClass.getName(), e);
        }
    }
}
