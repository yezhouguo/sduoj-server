/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.user.dto;

import cn.edu.sdu.qd.oj.common.entity.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * @ClassName User
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/2/26 11:29
 * @Version V1.0
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserManageUpdateReqDTO extends BaseDTO {

    private Long userId;

    private Map<String, String> features;

    @Pattern(regexp = "^[A-Za-z0-9_]{4,16}$", message = "用户名必须由英文、数字、'_'构成，且长度为4~16")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Length(max = 30, message = "昵称长度不合法，比如在30位之内")
    @Nullable
    private String nickname;

    @Length(min = 4, max = 32, message = "密码长度必须在4-32位之间")
    @Nullable
    private String password;

    @Email(message = "邮箱不合法")
    @NotBlank
    private String email;

    private Integer emailVerified;

    @Length(min = 11, max = 16, message = "手机号码长度不合法")
    @Nullable
    private String phone;

    @Range(min = 0, max = 2, message = "性别不合法, 0.女, 1.男, 2.问号")
    @Nullable
    private Integer gender;

    @Length(max = 20, message = "学号长度不合法")
    @Nullable
    private String studentId;

    private List<String> roles;
}