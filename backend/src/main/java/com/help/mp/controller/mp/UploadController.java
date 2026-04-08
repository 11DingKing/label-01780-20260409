package com.help.mp.controller.mp;

import com.help.mp.common.BizException;
import com.help.mp.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/mp/upload")
public class UploadController {

    @Value("${app.upload.base-url:http://localhost:8080/upload}")
    private String baseUrl;
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    private static final String UPLOAD_DIR = "upload";

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BizException(400, "请选择图片");
        String ext = getExt(file.getOriginalFilename());
        if (ext == null || (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("gif")))
            throw new BizException(400, "仅支持 jpg/png/gif");
        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String name = UUID.randomUUID().toString() + "." + ext;
            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target);
            String url = baseUrl + "/" + name;
            log.info("Uploaded image: {}", url);
            return Result.ok(url);
        } catch (IOException e) {
            log.error("Upload failed", e);
            throw new BizException(500, "上传失败");
        }
    }

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
