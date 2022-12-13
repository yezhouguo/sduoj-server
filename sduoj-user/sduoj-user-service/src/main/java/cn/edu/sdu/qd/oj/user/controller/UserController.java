/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.user.controller;

import cn.edu.sdu.qd.oj.common.annotation.RealIp;
import cn.edu.sdu.qd.oj.common.annotation.UserSession;
import cn.edu.sdu.qd.oj.common.entity.ApiResponseBody;
import cn.edu.sdu.qd.oj.common.entity.ResponseResult;
import cn.edu.sdu.qd.oj.common.enums.ApiExceptionEnum;
import cn.edu.sdu.qd.oj.common.exception.ApiException;
import cn.edu.sdu.qd.oj.common.util.AssertUtils;
import cn.edu.sdu.qd.oj.common.util.CaptchaUtils;
import cn.edu.sdu.qd.oj.common.util.RedisConstants;
import cn.edu.sdu.qd.oj.common.util.RedisUtils;
import cn.edu.sdu.qd.oj.user.dto.UserDTO;
import cn.edu.sdu.qd.oj.common.entity.UserSessionDTO;
import cn.edu.sdu.qd.oj.user.dto.UserUpdateReqDTO;
import cn.edu.sdu.qd.oj.user.service.UserExtensionService;
import cn.edu.sdu.qd.oj.user.service.UserService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.*;

import static cn.edu.sdu.qd.oj.common.enums.ApiExceptionEnum.PASSWORD_NOT_MATCHING;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/2/26 11:29
 * @Version V1.0
 **/

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserExtensionService userExtensionService;

    @Autowired
    private RedisUtils redisUtils;

    @PostMapping("/register")
    @ApiResponseBody
    public Void register(@Valid @RequestBody UserDTO userDTO) throws Exception {
        verifyCaptcha(userDTO.getCaptchaId(), userDTO.getCaptcha());
        userDTO.setRoles(null);
        this.userService.register(userDTO);
        return null;
    }

    @GetMapping("/verifyEmail")
    @ApiResponseBody
    public Void emailVerify(@RequestParam("token") String token) {
        this.userService.emailVerify(token);
        return null;
    }

    @PostMapping("/sendVerificationEmail")
    @ApiResponseBody
    public String verificationEmailSend(@RequestBody Map<String, String> json) throws MessagingException {
        // TODO: 前端重新发验证邮件需要输验证码
//        verifyCaptcha(json.get("captchaId"), json.get("captcha"));

        String username = json.get("username");
        return this.userService.sendVerificationEmail(username);
    }

    @PostMapping("/forgetPassword")
    @ApiResponseBody
    public String forgetPassword(@RequestBody Map<String, String> json) throws Exception {
        verifyCaptcha(json.get("captchaId"), json.get("captcha"));

        String username = null, email = null;
        try {
            username = json.get("username");
            email = json.get("email");
        }catch (Exception ignore) {
        }
        return this.userService.forgetPassword(username, email);
    }

    @PostMapping("/resetPassword")
    @ApiResponseBody
    public Void resetPassword(@RequestBody Map<String, String> json) {
        String token = json.get("token");
        String password = json.get("password");
        AssertUtils.notNull(token, ApiExceptionEnum.PARAMETER_ERROR);
        AssertUtils.notNull(password, ApiExceptionEnum.PARAMETER_ERROR);
        this.userService.resetPassword(token, password);
        return null;
    }

    @GetMapping("/getProfile")
    @ApiResponseBody
    public UserDTO getProfile(@UserSession UserSessionDTO userSessionDTO) {
        return this.userService.queryByUserId(userSessionDTO.getUserId());
    }

    @PostMapping("/updateProfile")
    @ApiResponseBody
    public Void updateProfile(@RequestBody UserUpdateReqDTO reqDTO,
                              @UserSession UserSessionDTO userSessionDTO) throws MessagingException {
        // 新密码校验
        if (StringUtils.isNotBlank(reqDTO.getNewPassword())) {
            validatePassword(reqDTO.getNewPassword());
        }

        // 新邮箱校验
        if (StringUtils.isNotBlank(reqDTO.getNewEmail())) {
            validateEmail(reqDTO.getNewEmail());
        }

        reqDTO.setUserId(userSessionDTO.getUserId());
        this.userService.updateProfile(reqDTO);
        return null;
    }

    private void validateEmail(@Email(message = "邮箱不合法") String email) {
    }

    private void validatePassword(@Length(min = 4, max = 32, message = "密码长度必须在4-32位之间") String password) {

    }

    @GetMapping("/getCaptcha")
    @ApiResponseBody
    public Map<String, String> getCaptcha() {
        String uuid = UUID.randomUUID().toString();
        CaptchaUtils.CaptchaEntity captcha = CaptchaUtils.getRandomBase64Captcha();
        AssertUtils.isTrue(redisUtils.set(RedisConstants.getCaptchaKey(uuid), captcha.getRandomStr(), RedisConstants.CAPTCHA_EXPIRE), ApiExceptionEnum.UNKNOWN_ERROR);
        Map<String, String> map = new HashMap<>();
        map.put("captcha", captcha.getBase64());
        map.put("captchaId", uuid);
        return map;
    }

    @GetMapping("/isExist")
    @ApiResponseBody
    public Boolean isExist(@RequestParam("username") @Nullable String username,
                           @RequestParam("email") @Nullable String email) {
        if (StringUtils.isNotBlank(username)) {
            return this.userService.isExistUsername(username);
        }
        if (StringUtils.isNotBlank(email)) {
            return this.userService.isExistEmail(email);
        }
        return false;
    }

    //yzg
    @PostMapping("/loginFromVscode")
    @ResponseBody
    public ResponseResult<UserSessionDTO> login(HttpServletResponse response,@RequestBody @NotNull Map<String, String> json) throws ApiException {
        String username = null, password = null;
        try {
            username = json.get("username");
            password = json.get("password");
        } catch (Exception ignore) {
        }
        log.info("{} login from {} by {}", username, " ", " ");
        UserSessionDTO userSessionDTO =null;
        try {
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                // 登录校验
                userSessionDTO = this.userService.login(username, password, " ", " ");
                response.setHeader(UserSessionDTO.HEADER_KEY, JSON.toJSONString(userSessionDTO));
            }
        }catch (Exception loginError) {
            return ResponseResult.error(PASSWORD_NOT_MATCHING);
        }
        return ResponseResult.ok(userSessionDTO);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseResult<UserSessionDTO> login(HttpServletResponse response,
                                                @RequestBody @NotNull Map<String, String> json,
                                                @RealIp String ipv4,
                                                @RequestHeader("user-agent") String userAgent) throws ApiException {
        String username = null, password = null;
        try {
            username = json.get("username");
            password = json.get("password");
        } catch (Exception ignore) {
        }

        log.info("{} login from {} by {}", username, ipv4, userAgent);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            // 登录校验
            UserSessionDTO userSessionDTO = this.userService.login(username, password, ipv4, userAgent);
            response.setHeader(UserSessionDTO.HEADER_KEY, JSON.toJSONString(userSessionDTO));
            return ResponseResult.ok(userSessionDTO);
        }
        return ResponseResult.error();
    }

    @GetMapping("/logout")
    @ResponseBody
    public ResponseResult<Void> logout(HttpServletResponse response) {
        response.setHeader(UserSessionDTO.HEADER_KEY, UserSessionDTO.HEADER_VALUE_LOGOUT);
        return ResponseResult.ok(null);
    }


    /**
    * @Description 进行验证码验证
    * @exception  ApiException CAPTCHA_NOT_MATCHING
    * @exception  ApiException CAPTCHA_NOT_FOUND
    **/
    private void verifyCaptcha(String captchaId, String inputCaptcha) {
        AssertUtils.notNull(captchaId, ApiExceptionEnum.CAPTCHA_NOT_FOUND);
        AssertUtils.notNull(inputCaptcha, ApiExceptionEnum.CAPTCHA_NOT_FOUND);

        String captcha = Optional.ofNullable(redisUtils.get(RedisConstants.getCaptchaKey(captchaId))).map(o -> (String) o).orElse(null);

        AssertUtils.notNull(captcha, ApiExceptionEnum.CAPTCHA_NOT_FOUND);
        AssertUtils.isTrue(captcha.equalsIgnoreCase(inputCaptcha), ApiExceptionEnum.CAPTCHA_NOT_MATCHING);
    }

    /**
     * @Description 查询用户参加过的比赛
     **/
    @GetMapping("/queryParticipateContest")
    @ApiResponseBody
    public List<Long> queryParticipateContest(@UserSession UserSessionDTO userSessionDTO) {
        return userExtensionService.queryParticipateContest(userSessionDTO.getUserId());
    }

}