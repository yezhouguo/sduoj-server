/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName SubmitApplication
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/3/6 15:34
 * @Version V1.0
 **/


@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan(basePackages = {"cn.edu.sdu.qd.oj.submit.mapper"})
public class SubmitApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubmitApplication.class, args);
    }
}