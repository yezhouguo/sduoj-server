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
import lombok.*;

import java.util.Date;

/**
 * @Author zhangt2333
 * @Date 2020/3/14 18:59
 * @Version V1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubmissionJudgeDTO extends BaseDTO {

    private Long submissionId;

    private Long problemId;

    private Long userId;

    private Integer version;

    private Long judgeTemplateId;

    private Long zipFileId;

    private Date gmtCreate;

    private String code;

    private Integer codeLength;
}