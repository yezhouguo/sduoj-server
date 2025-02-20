/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 */

package cn.edu.sdu.qd.oj.filesys.service.impl;

import cn.edu.sdu.qd.oj.common.enums.ApiExceptionEnum;
import cn.edu.sdu.qd.oj.common.exception.ApiException;
import cn.edu.sdu.qd.oj.common.util.AssertUtils;
import cn.edu.sdu.qd.oj.common.util.CodecUtils;
import cn.edu.sdu.qd.oj.common.util.CollectionUtils;
import cn.edu.sdu.qd.oj.common.util.SnowflakeIdWorker;
import cn.edu.sdu.qd.oj.filesys.dto.BinaryFileUploadReqDTO;
import cn.edu.sdu.qd.oj.filesys.dto.FileDTO;
import cn.edu.sdu.qd.oj.filesys.dto.FileDownloadReqDTO;
import cn.edu.sdu.qd.oj.filesys.dto.PlainFileDownloadDTO;
import cn.edu.sdu.qd.oj.filesys.config.FileSystemProperties;
import cn.edu.sdu.qd.oj.filesys.converter.FileConverter;
import cn.edu.sdu.qd.oj.filesys.dao.FileDao;
import cn.edu.sdu.qd.oj.filesys.entity.FileDO;
import cn.edu.sdu.qd.oj.filesys.service.FileService;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@EnableConfigurationProperties(FileSystemProperties.class)
public class LocalFileService implements FileService {

    @Autowired
    private FileSystemProperties fileSystemProperties;

    private SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker();

    @Autowired
    private FileDao fileDao;

    @Autowired
    private FileConverter fileConverter;

    @Transactional
    @Override
    public FileDTO upload(MultipartFile file, Long userId) {
        // 计算文件 md5
        String md5;
        try {
            md5 = CodecUtils.md5(file.getInputStream());
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.FILE_WRITE_ERROR);
        }
        // 查询是否已有相同 MD5
        FileDO fileDO = fileDao.lambdaQuery().eq(FileDO::getMd5, md5).one();
        if (fileDO != null) {
            return fileConverter.to(fileDO);
        }

        Long id = snowflakeIdWorker.nextId();
        fileDO = FileDO.builder()
                .id(id)
                .userId(userId)
                .name(file.getOriginalFilename())
                .extensionName(Files.getFileExtension(file.getOriginalFilename()))
                .md5(md5)
                .size(file.getSize())
                .build();
        AssertUtils.isTrue(fileDao.save(fileDO), ApiExceptionEnum.SERVER_BUSY);
        try {
            File writeFile = new File(Paths.get(fileSystemProperties.getBaseDir(), id.toString()).toString());
            byte[] bytes = file.getBytes();
            FileUtils.writeByteArrayToFile(writeFile, bytes);
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.FILE_WRITE_ERROR);
        }
        return fileConverter.to(fileDO);
    }

    @Override
    @Transactional
    public List<FileDTO> uploadFiles(MultipartFile[] files, Long userId) {
        List<BinaryFileUploadReqDTO> reqDTOList = new ArrayList<>(files.length);
        try {
            for (MultipartFile file : files) {
                byte[] bytes = file.getBytes();
                reqDTOList.add(
                    BinaryFileUploadReqDTO.builder()
                        .filename(file.getOriginalFilename())
                        .bytes(bytes)
                        .size((long) bytes.length)
                        .inputStream(file.getInputStream())
                        .build()
                );
            }
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.FILE_WRITE_ERROR);
        }
        return uploadBinaryFiles(reqDTOList, userId);
    }

    @Override
    public void downloadFilesInZip(List<FileDownloadReqDTO> fileDownloadReqDTOList, ZipOutputStream zipOut) {
        for (FileDownloadReqDTO fileDownloadReqDTO : fileDownloadReqDTOList) {
            FileSystemResource file = new FileSystemResource(Paths.get(fileSystemProperties.getBaseDir(), fileDownloadReqDTO.getId().toString()).toString());
            AssertUtils.isTrue(file.exists(), ApiExceptionEnum.FILE_NOT_EXISTS);
            fileDownloadReqDTO.setFileSystemResource(file);
        }
        try {
            for (FileDownloadReqDTO fileDownloadReqDTO : fileDownloadReqDTOList) {
                FileSystemResource file = fileDownloadReqDTO.getFileSystemResource();
                ZipEntry zipEntry = new ZipEntry(fileDownloadReqDTO.getDownloadFilename());
                zipEntry.setSize(file.contentLength());
                zipOut.putNextEntry(zipEntry);
                StreamUtils.copy(file.getInputStream(), zipOut);
                zipOut.closeEntry();
            }
            zipOut.finish();
            zipOut.close();
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.FILE_READ_ERROR);
        }
    }

    @Override
    public byte[] downloadFilesInZipBytes(List<FileDownloadReqDTO> fileDownloadReqDTOList) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        downloadFilesInZip(fileDownloadReqDTOList, zipOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    @Transactional
    public List<FileDTO> uploadBinaryFiles(List<BinaryFileUploadReqDTO> reqDTOList, Long userId) {
        String[] md5s = new String[reqDTOList.size()];
        // 计算文件 md5
        try {
            for (int i = 0, n = reqDTOList.size(); i < n; i++) {
                md5s[i] = CodecUtils.md5(reqDTOList.get(i).getBytes());
            }
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.FILE_WRITE_ERROR);
        }
        Map<String, FileDO> md5ToFileDOMap = fileDao.lambdaQuery()
                .in(FileDO::getMd5, md5s)
                .list()
                .stream()
                .collect(Collectors.toMap(FileDO::getMd5, Function.identity(), (k1, k2) -> k1));

        // 有新的文件需要上传
        if (reqDTOList.size() != Arrays.stream(md5s).map(md5ToFileDOMap::get).filter(Objects::nonNull).count()) {
            // 构造需要新传的文件
            List<FileDO> newFileDOList = new ArrayList<>();
            List<Integer> newFileDOIndexList = new ArrayList<>();
            for (int i = 0, n = reqDTOList.size(); i < n; i++) {
                FileDO fileDO = md5ToFileDOMap.get(md5s[i]);
                if (fileDO == null) {
                    fileDO = FileDO.builder()
                            .id(snowflakeIdWorker.nextId())
                            .name(reqDTOList.get(i).getFilename())
                            .extensionName(Files.getFileExtension(reqDTOList.get(i).getFilename()))
                            .md5(md5s[i])
                            .size(reqDTOList.get(i).getSize())
                            .userId(userId)
                            .build();
                    newFileDOList.add(fileDO);
                    newFileDOIndexList.add(i);
                    md5ToFileDOMap.put(fileDO.getMd5(), fileDO);
                }
            }
            if (CollectionUtils.isNotEmpty(newFileDOList)) {
                AssertUtils.isTrue(fileDao.saveBatch(newFileDOList), ApiExceptionEnum.SERVER_BUSY);
                int i = 0;
                try {
                    // 逐个写入文件系统
                    for (int n = newFileDOList.size(); i < n; i++) {
                        FileDO fileDO = newFileDOList.get(i);
                        File writeFile = new File(Paths.get(fileSystemProperties.getBaseDir(), fileDO.getId().toString()).toString());
                        byte[] bytes = reqDTOList.get(newFileDOIndexList.get(i)).getBytes();
                        FileUtils.writeByteArrayToFile(writeFile, bytes);
                    }
                } catch (Exception e) {
                    // 失败，尝试逐个回滚删除，随后抛异常使数据库回滚
                    for (int j = 0; j <= i; j++) {
                        FileDO fileDO = newFileDOList.get(i);
                        File file = new File(Paths.get(fileSystemProperties.getBaseDir(), fileDO.getId().toString()).toString());
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    throw new ApiException(ApiExceptionEnum.FILE_WRITE_ERROR);
                }
            }
        }

        // 构造返回值
        List<FileDO> fileDOList = new ArrayList<>();
        for (int i = 0; i < reqDTOList.size(); i++) {
            FileDO source = md5ToFileDOMap.get(md5s[i]);
            if (source == null) {
                continue;
            }
            FileDO fileDO = new FileDO();
            BeanUtils.copyProperties(source, fileDO);
            BinaryFileUploadReqDTO reqDTO = reqDTOList.get(i);
            AssertUtils.isTrue(reqDTO.getSize().equals(fileDO.getSize()), ApiExceptionEnum.FILE_NOT_MATCH);
            fileDO.setName(reqDTO.getFilename());
            fileDO.setExtensionName(Files.getFileExtension(reqDTO.getFilename()));
            fileDOList.add(fileDO);
        }
        return fileConverter.to(fileDOList);
    }

    @Override
    public List<PlainFileDownloadDTO> plainFileDownload(Long sizeLimit, List<PlainFileDownloadDTO> reqDTOList) {

        for (PlainFileDownloadDTO plainFileDownloadDTO : reqDTOList) {
            FileSystemResource file = new FileSystemResource(Paths.get(fileSystemProperties.getBaseDir(), plainFileDownloadDTO.getFileId().toString()).toString());
            AssertUtils.isTrue(file.exists(), ApiExceptionEnum.FILE_NOT_EXISTS);
            try {
                AssertUtils.isTrue(file.contentLength() <= sizeLimit, ApiExceptionEnum.FILE_TOO_LARGE);
                plainFileDownloadDTO.setBytes(StreamUtils.copyToByteArray(file.getInputStream()));
            } catch (IOException e) {
                throw new ApiException(ApiExceptionEnum.FILE_READ_ERROR);
            }
        }
        return reqDTOList;
    }

    @Override
    public FileDTO queryByMd5(String md5) {
        FileDO fileDO = fileDao.lambdaQuery().eq(FileDO::getMd5, md5).one();
        return fileConverter.to(fileDO);
    }

    @Override
    public byte[] downloadFile(long id) throws IOException {
        FileSystemResource file = new FileSystemResource(Paths.get(fileSystemProperties.getBaseDir(), String.valueOf(id)).toString());
        AssertUtils.isTrue(file.exists(), ApiExceptionEnum.FILE_NOT_EXISTS);
        return StreamUtils.copyToByteArray(file.getInputStream());
    }

    @Override
    public void downloadToStream(long fileId, OutputStream outputStream) throws IOException {
        FileSystemResource file = new FileSystemResource(Paths.get(fileSystemProperties.getBaseDir(), String.valueOf(fileId)).toString());
        StreamUtils.copy(file.getInputStream(), outputStream);
    }

    @Override
    public String fileIdToFilename(long fileId) {
        return Optional.ofNullable(fileDao.lambdaQuery().select(FileDO::getName).eq(FileDO::getId, fileId).one())
                       .map(FileDO::getName)
                       .orElse(null);
    }
}
