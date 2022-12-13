/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.submit.client;

import cn.edu.sdu.qd.oj.problem.api.ProblemApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @InterfaceName ProblemClient
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/4/9 17:11
 * @Version V1.0
 **/

@FeignClient(ProblemApi.SERVICE_NAME)
public interface ProblemClient extends ProblemApi {
}