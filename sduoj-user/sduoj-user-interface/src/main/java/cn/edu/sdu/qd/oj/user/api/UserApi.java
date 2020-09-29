/**
 * The GNU General Public License
 * Copyright (c) 2020-2020 zhangt2333@gmail.com
 **/

package cn.edu.sdu.qd.oj.user.api;

import cn.edu.sdu.qd.oj.common.exception.InternalApiException;
import cn.edu.sdu.qd.oj.user.dto.UserDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @InterfaceName UserApi
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/2/27 14:56
 * @Version V1.0
 **/

@RequestMapping("/internal/user")
public interface UserApi {
    String SERVICE_NAME = "user-service";

    /**
     * 根据用户名和密码查询用户
     * @param map {"username": "", "password": ""}
     */
    @PostMapping(value = "/verify", consumes = "application/json")
    UserDTO verify(@RequestBody Map<String, String> map) throws InternalApiException;

    /**
     * 根据用户id查询用户
     * @param userId
     */
    @GetMapping("/queryById")
    UserDTO query(@RequestParam("userId") Long userId) throws InternalApiException;

    /**
     * 根据用户名查询用户id
     * @param username
     */
    @GetMapping("/queryIdByUsername")
    Long queryUserId(@RequestParam("username") String username) throws InternalApiException;

    /**
     * 查询 userId->username 的全量 map
     */
    @GetMapping("/queryIdToUsernameMap")
    Map<Long, String> queryIdToNameMap() throws InternalApiException;

    /**
    * @Description 查询具体用户权限
    **/
    @GetMapping("/queryRolesById")
    List<String> queryRolesById(@RequestParam("userId") Long userId);
}