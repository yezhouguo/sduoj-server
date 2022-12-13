/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.problem.controller;

import cn.edu.sdu.qd.oj.common.annotation.UserSession;
import cn.edu.sdu.qd.oj.common.entity.ApiResponseBody;
import cn.edu.sdu.qd.oj.common.entity.PageResult;
import cn.edu.sdu.qd.oj.common.entity.UserSessionDTO;
import cn.edu.sdu.qd.oj.problem.dto.ProblemDTO;
import cn.edu.sdu.qd.oj.problem.dto.ProblemListDTO;
import cn.edu.sdu.qd.oj.problem.dto.ProblemListReqDTO;
import cn.edu.sdu.qd.oj.problem.service.ProblemService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/problem")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @GetMapping("/query")
    @ApiResponseBody
    public ProblemDTO queryByCode(@RequestParam("problemCode") String problemCode,
                                  @RequestParam("descriptionId") @Nullable Long descriptionId,
                                  @UserSession(nullable = true) UserSessionDTO userSessionDTO) {
        return this.problemService.queryByCode(problemCode, descriptionId, userSessionDTO);
    }

    @GetMapping("/queryById")
    @ApiResponseBody
    public ProblemDTO queryById(@RequestParam("problemId") String problemId,
                                  @RequestParam("descriptionId") @Nullable Long descriptionId,
                                  @UserSession(nullable = true) UserSessionDTO userSessionDTO) {
        return this.problemService.queryById(problemId, descriptionId, userSessionDTO);
    }

    @GetMapping("/list")
    @ApiResponseBody
    public PageResult<ProblemListDTO> queryList(@Valid ProblemListReqDTO problemListReqDTO,
                                                @UserSession(nullable = true) UserSessionDTO userSessionDTO) {
        return this.problemService.queryProblemByPage(problemListReqDTO, userSessionDTO);
    }

    //yzg
    @GetMapping("/getAllFromVscode")
    @ApiResponseBody
    public List<ProblemListDTO> queryAll(@UserSession(nullable = true) UserSessionDTO userSessionDTO) {
        return this.problemService.queryAllProblem(userSessionDTO);
    }
}