package com.qiu.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qiu
 * @version 1.0
 * @className ServiceMetaInfo
 * @packageName com.qiu.rpc.model
 * @Description
 * @date 2026/1/13 21:26
 * @since 1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMetaInfo {

    private String serviceName;

    private String serviceVersion = "1.0";

    private String serviceHost;

    private int servicePort;

    private String serviceGroup = "default";

    public String getServiceKey() {
        return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
    }

    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }


}
