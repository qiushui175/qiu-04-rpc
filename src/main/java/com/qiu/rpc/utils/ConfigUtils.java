package com.qiu.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author qiu
 * @version 1.0
 * @className ConfigUtils
 * @packageName com.qiu.rpc.utils
 * @Description
 * @date 2026/1/12 17:35
 * @since 1.0
 */
public class ConfigUtils {

    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    private static <T> T loadConfig(Class<T> tClass, String prefix, String env) {
        StringBuilder configBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(env)) {
            configBuilder.append("-").append(env);
        }
        configBuilder.append(".properties");
        Props props = new Props(configBuilder.toString());
        return props.toBean(tClass, prefix);
    }

}
