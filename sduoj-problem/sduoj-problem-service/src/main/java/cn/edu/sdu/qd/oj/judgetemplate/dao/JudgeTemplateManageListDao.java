/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.judgetemplate.dao;

import cn.edu.sdu.qd.oj.judgetemplate.entity.JudgeTemplateManageListDO;
import cn.edu.sdu.qd.oj.judgetemplate.mapper.JudgeTemplateManageListDOMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;

@Repository
public class JudgeTemplateManageListDao extends ServiceImpl<JudgeTemplateManageListDOMapper, JudgeTemplateManageListDO> {
}