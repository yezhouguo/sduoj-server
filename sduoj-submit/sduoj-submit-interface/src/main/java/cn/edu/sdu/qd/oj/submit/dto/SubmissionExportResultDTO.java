/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.submit.dto;

import cn.edu.sdu.qd.oj.common.entity.BaseDTO;
import lombok.*;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubmissionExportResultDTO extends BaseDTO {

    private Long submissionId;

    private Date gmtCreate;

    private Long userId;

    private Long problemId;

    private Long zipFileId;

    private Integer judgeTemplateId;

    private Integer judgeResult;

    private Integer judgeScore;

    private String code;
}