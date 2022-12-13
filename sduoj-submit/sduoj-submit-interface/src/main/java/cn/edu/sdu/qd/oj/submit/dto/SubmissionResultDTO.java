/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.submit.dto;

import cn.edu.sdu.qd.oj.common.entity.BaseDTO;
import cn.edu.sdu.qd.oj.common.util.HexStringToLongDeserializer;
import cn.edu.sdu.qd.oj.common.util.LongToHexStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubmissionResultDTO extends BaseDTO {

    @JsonSerialize(using = LongToHexStringSerializer.class)
    @JsonDeserialize(using = HexStringToLongDeserializer.class)
    private Long submissionId;

    private Long contestId;

    private Date gmtCreate;

    private Long problemId;

    private Long userId;

    private Integer judgeScore;

    private Integer judgeResult;

    // -------------------------------------------

    private String problemCode;
}