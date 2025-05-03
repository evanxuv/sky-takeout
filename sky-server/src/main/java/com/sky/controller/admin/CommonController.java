package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.UUID;

/**
 * @author evan
 * @version 1.0
 */
@Slf4j
@RequestMapping("/admin/common")
@RestController
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    // 使用阿里云oss做文件存储
    @RequestMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        log.info("文件上传,原始文件名:{}", originalFilename);
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));// 获取文件后缀
        String url = null;
        try {
            // 1. 调用AliOssUtil工具类的upload上传方法
            String ObjectName = UUID.randomUUID().toString() + suffix;
            url = aliOssUtil.upload(file.getBytes(), ObjectName);
        } catch (IOException e) {
            log.info("文件上传失败:{}",e.getMessage());
            return Result.error("文件上传失败");
        }
        // 2. 返回图片路径结果
        return Result.success(url);
    }
}
