/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.auth.service;

import cn.edu.sdu.qd.oj.auth.converter.PermissionConverter;
import cn.edu.sdu.qd.oj.auth.dao.PermissionDao;
import cn.edu.sdu.qd.oj.auth.dto.PermissionDTO;
import cn.edu.sdu.qd.oj.auth.entity.PermissionDO;
import cn.edu.sdu.qd.oj.common.converter.BaseConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName AuthService
 * @Description TODO
 * @Author zhangt2333
 * @Date 2020/2/27 14:18
 * @Version V1.0
 **/
@Service
@Slf4j
public class AuthService {
    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private PermissionConverter permissionConverter;

    public void syncNewPermissionUrl(List<PermissionDTO> permissionDTOList) {
        List<PermissionDO> permissionDOList = permissionConverter.from(permissionDTOList);
        List<String> urlList = permissionDOList.stream().map(PermissionDO::getUrl).collect(Collectors.toList());
        List<PermissionDO> dbUrlList = permissionDao.lambdaQuery().select(PermissionDO::getUrl).in(PermissionDO::getUrl, urlList).list();
        Set<String> dbUrlSet = dbUrlList.stream().map(PermissionDO::getUrl).collect(Collectors.toSet());
        permissionDOList = permissionDOList.stream().filter(permissionDO -> !dbUrlSet.contains(permissionDO.getUrl())).collect(Collectors.toList());
        permissionDOList.forEach(permissionDO -> permissionDO.setRoles(null));
        permissionDao.saveBatch(permissionDOList);
    }

    public List<PermissionDTO> listAll() {
        List<PermissionDO> permissionDOList = permissionDao.list();
        return permissionConverter.to(permissionDOList);
    }

    public List<String> urlToRoles(String url) {
        PermissionDO permissionDO = permissionDao.lambdaQuery().select(PermissionDO::getRoles).eq(PermissionDO::getUrl, url).one();
        return BaseConvertUtils.stringToList(permissionDO.getRoles());
    }
}