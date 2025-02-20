/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.common.entity;

import cn.edu.sdu.qd.oj.common.entity.BaseDTO;
import lombok.*;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSessionDTO extends BaseDTO {

    public static final String HEADER_KEY = "SDUOJUserInfo";
    public static final String HEADER_VALUE_LOGOUT = "logout";
    public static final String HEADER_KEY_USERID = "SDUOJUserInfo-UserId";

    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String studentId;
    private List<String> roles;

    private String ipv4;
    private String userAgent;

    public boolean userIdEquals(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean userIdNotEquals(Long userId) {
        return !userIdEquals(userId);
    }
}