/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.problem.converter;

import cn.edu.sdu.qd.oj.common.converter.BaseConvertUtils;
import cn.edu.sdu.qd.oj.common.util.SpringContextUtils;
import cn.edu.sdu.qd.oj.judgetemplate.dto.JudgeTemplateListDTO;
import cn.edu.sdu.qd.oj.judgetemplate.service.JudgeTemplateService;
import cn.edu.sdu.qd.oj.problem.dto.ProblemCheckerConfigDTO;
import cn.edu.sdu.qd.oj.problem.dto.ProblemCheckpointDTO;
import cn.edu.sdu.qd.oj.problem.dto.ProblemFunctionTemplateDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description DTO-DO 特殊转换方法的统一收口处，解决单例耦合
 * @author zhangt2333
 **/
public class ProblemConverterUtils extends BaseConvertUtils{

    public static List<ProblemCheckpointDTO> checkpointsTo(byte[] bytes) {
        // TODO: 这种逻辑要是出现二次以上就可以内置到 ProblemCheckpointDTO 去写
        int size = bytes != null ? bytes.length : 0;
        if (size == 0 || size % ProblemCheckpointDTO.BYTE_SIZE != 0) {
            return Lists.newArrayList();
        }
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        List<ProblemCheckpointDTO> checkpoints = new ArrayList<>(size / ProblemCheckpointDTO.BYTE_SIZE);
        for (int i = 0; i < size; i += ProblemCheckpointDTO.BYTE_SIZE) {
            checkpoints.add(new ProblemCheckpointDTO(wrap.getLong(i), wrap.getInt(i + 8)));
        }
        return checkpoints;
    }

    public static byte[] checkpointsFrom(List<ProblemCheckpointDTO> checkpointList) {
        // 不能返回 new byte[0] 不然本来不更新 checkpoints 的，变成清空了
        if (checkpointList == null) {
            return null;
        }
        ByteBuf byteBuf = Unpooled.buffer(checkpointList.size() * ProblemCheckpointDTO.BYTE_SIZE);
        checkpointList.forEach(o -> {
            byteBuf.writeLong(o.getCheckpointId());
            byteBuf.writeInt(o.getCheckpointScore());
        });
        return byteBuf.array();
    }

    public static List<JudgeTemplateListDTO> judgeTemplatesTo(String judgeTemplates) {
        List<String> judgeTemplateIdStrList = BaseConvertUtils.stringToList(judgeTemplates);
        if (judgeTemplateIdStrList == null) {
            return new ArrayList<>();
        }
        List<Long> judgeTemplateIdList = judgeTemplateIdStrList.stream().map(Long::parseLong).collect(Collectors.toList());
        JudgeTemplateService judgeTemplateService = SpringContextUtils.getBean(JudgeTemplateService.class);
        return judgeTemplateService.listByIds(judgeTemplateIdList);
    }

    public static String judgeTemplatesFrom(List<JudgeTemplateListDTO> judgeTemplates) {
        if (CollectionUtils.isEmpty(judgeTemplates)) {
            return null;
        }
        return BaseConvertUtils.listToString(judgeTemplates.stream().map(JudgeTemplateListDTO::getId).map(String::valueOf).collect(Collectors.toList()));
    }

    public static List<Long> stringToLongList(String idListString) {
        List<String> stringList = BaseConvertUtils.stringToList(idListString);
        if (stringList == null) {
            return null;
        }
        return stringList.stream().map(Long::parseLong).collect(Collectors.toList());
    }

    public static String longListToString(List<Long> longList) {
        if (longList == null) {
            return null;
        }
        return BaseConvertUtils.listToString(longList.stream().map(Object::toString).collect(Collectors.toList()));
    }

    public static ProblemCheckerConfigDTO checkerConfigTo(String checkerConfig) {
        try {
            return JSON.parseObject(checkerConfig, ProblemCheckerConfigDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String checkerConfigFrom(ProblemCheckerConfigDTO checkerConfigDTO) {
        return JSON.toJSONString(checkerConfigDTO);
    }

    public static List<ProblemFunctionTemplateDTO> functionTemplatesTo(String functionTemplates) {
        try {
            return Optional.ofNullable(JSON.parseObject(functionTemplates, new TypeReference<List<ProblemFunctionTemplateDTO>>() {}))
                           .orElseGet(Lists::newArrayList);
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

    public static String functionTemplatesFrom(List<ProblemFunctionTemplateDTO> functionTemplates) {
        if (functionTemplates == null) {
            functionTemplates = Lists.newArrayList();
        }
        return JSON.toJSONString(functionTemplates);
    }
}
