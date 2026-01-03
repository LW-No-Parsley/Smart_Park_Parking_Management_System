package com.syan.smart_park.controller;

import com.syan.smart_park.common.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源控制器
 * 提供分类资源访问功能
 */
@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private static final String RESOURCE_BASE_PATH = "src/main/resources/public/";

    /**
     * 获取静态资源
     *
     * @param category 资源分类（如：images, documents等）
     * @param filename 文件名
     * @return 资源文件
     */
    @GetMapping("/{category}/{filename:.+}")
    public ResponseEntity<Resource> getResource(
            @PathVariable String category,
            @PathVariable String filename) {
        
        try {
            // 处理category映射：将"images"映射到"img"目录以保持向后兼容性
            String actualCategory = category;
            if ("images".equals(category)) {
                actualCategory = "img";
                log.debug("映射category: images -> img");
            }
            
            // 构建文件路径
            Path filePath = Paths.get(RESOURCE_BASE_PATH, actualCategory, filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 检查资源是否存在且可读
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("资源不存在或不可读: {}/{}", category, filename);
                return ResponseEntity.notFound().build();
            }

            // 根据文件扩展名确定Content-Type
            String contentType = determineContentType(filename);

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            
            // 处理文件名编码（支持中文等非ASCII字符）
            String encodedFilename = encodeFilename(filename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "inline; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("资源URL格式错误: {}/{}", category, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("获取资源失败: {}/{}", category, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据文件名确定Content-Type
     */
    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (lowerFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else if (lowerFilename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        } else if (lowerFilename.endsWith(".txt")) {
            return "text/plain; charset=UTF-8";
        } else if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) {
            return MediaType.TEXT_HTML_VALUE;
        } else if (lowerFilename.endsWith(".css")) {
            return "text/css";
        } else if (lowerFilename.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerFilename.endsWith(".json")) {
            return MediaType.APPLICATION_JSON_VALUE;
        } else if (lowerFilename.endsWith(".xml")) {
            return MediaType.APPLICATION_XML_VALUE;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    /**
     * 编码文件名（支持非ASCII字符）
     * 使用RFC 5987规范
     */
    private String encodeFilename(String filename) {
        try {
            // 对非ASCII字符进行URL编码
            return java.net.URLEncoder.encode(filename, "UTF-8")
                    .replace("+", "%20");
        } catch (Exception e) {
            log.warn("文件名编码失败: {}", filename, e);
            return filename;
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public R<String> healthCheck() {
        return R.success("Resource service is healthy");
    }
}
