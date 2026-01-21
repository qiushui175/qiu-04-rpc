package com.qiu.example.provider;

import com.qiu.example.common.service.UserService;
import com.qiu.example.provider.service.impl.UserServiceImpl;
import com.qiu.rpc.bootstrap.ProviderBootstrap;
import com.qiu.rpc.model.ServiceRegisterInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author qiu
 * @version 1.0
 * @className ProviderExample
 * @packageName com.qiu.example.provider
 * @Description
 * @date 2026/1/9 15:41
 * @since 1.0
 */
public class ProviderExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 获取端口参数
        int port = 18080; // 默认端口
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]); // 从命令行参数获取端口
            } catch (NumberFormatException e) {
                System.out.println("无效的端口号，使用默认端口: " + port);
            }
        }
        System.out.println("使用的端口号: " + port);

        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        serviceRegisterInfoList.add(new ServiceRegisterInfo<UserService>(UserService.class.getName(), UserServiceImpl.class));
        ProviderBootstrap.init(serviceRegisterInfoList);
    }

}
