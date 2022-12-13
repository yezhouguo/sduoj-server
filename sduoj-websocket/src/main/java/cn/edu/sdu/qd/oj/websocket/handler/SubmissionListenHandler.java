/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.websocket.handler;

import cn.edu.sdu.qd.oj.common.util.RedisUtils;
import cn.edu.sdu.qd.oj.submit.dto.CheckpointResultMessageDTO;
import cn.edu.sdu.qd.oj.submit.enums.SubmissionJudgeResult;
import cn.edu.sdu.qd.oj.websocket.constant.SubmissionBizContant;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName SubmissionListener
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/4/15 12:28
 * @Version V1.0
 **/

@Slf4j
@Component
public class SubmissionListenHandler {

    @Autowired
    private RedisUtils redisUtils;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "sduoj.checkpoint.finish.ws", durable = "true"),
            exchange = @Exchange(value = "sduoj.checkpoint.finish", ignoreDeclarationExceptions = "true"))
    )
    public void pushSubmissionResult(CheckpointResultMessageDTO messageDTO) {
        log.info("rabbitMQ: {}", messageDTO);
        String submissionIdHex = Long.toHexString(messageDTO.getSubmissionId());
        messageDTO.set(CheckpointResultMessageDTO.INDEX_SUBMISSION_ID, submissionIdHex);

        String msg = JSONObject.toJSONString(messageDTO);

        // 如果是评测开始信息，则清空之前的记录，以免影响重测的场景。若不是，则直接存储该信息。现阶段问题是在短时间内多次重测，可能会消息重复，前端已容错
        try {
            if (SubmissionJudgeResult.COMPILING.code == (Integer) messageDTO.get(1)
                    || SubmissionJudgeResult.JUDGING.code == (Integer) messageDTO.get(1)) {
                redisUtils.del(SubmissionBizContant.getRedisSubmissionKey(submissionIdHex));
            } else {
                redisUtils.lSet(SubmissionBizContant.getRedisSubmissionKey(submissionIdHex), msg, SubmissionBizContant.REDIS_SUBMISSION_RESULT_EXPIRE);
            }
        } catch (Throwable ignored) {
        }

        // 在redis中广播这条消息
        redisUtils.publish(SubmissionBizContant.getRedisChannelKey(submissionIdHex), msg);
    }
}