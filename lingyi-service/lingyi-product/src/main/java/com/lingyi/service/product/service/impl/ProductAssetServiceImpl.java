package com.lingyi.service.product.service.impl;

import com.lingyi.service.product.config.MinioProperties;
import com.lingyi.service.product.service.ProductAssetService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAssetServiceImpl implements ProductAssetService {

    private final MinioProperties minioProperties;

    @Override
    public String resolvePublicUrl(String assetKey) {
        if (!StringUtils.hasText(assetKey)) {
            return assetKey;
        }
        if (assetKey.startsWith("http://") || assetKey.startsWith("https://") || assetKey.startsWith("/api/")) {
            return assetKey;
        }
        if (minioProperties.isEnabled()) {
            return minioProperties.getPublicBaseUrl().replaceAll("/$", "") + "/" + assetKey;
        }
        return "/api/product/assets/" + assetKey;
    }

    @Override
    public void ensureDemoAssetsUploaded() {
        if (!minioProperties.isEnabled()) {
            return;
        }
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
            }
            for (Map.Entry<String, String> entry : demoAssets().entrySet()) {
                ClassPathResource resource = new ClassPathResource("seed-assets/" + entry.getValue());
                try (InputStream inputStream = resource.getInputStream()) {
                    client.putObject(PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(entry.getKey())
                            .stream(inputStream, resource.contentLength(), -1)
                            .contentType("image/svg+xml")
                            .build());
                }
            }
        } catch (Exception ex) {
            log.warn("MinIO demo asset upload skipped: {}", ex.getMessage());
        }
    }

    private Map<String, String> demoAssets() {
        Map<String, String> assets = new LinkedHashMap<>();
        assets.put("demo-cold.svg", "demo-cold.svg");
        assets.put("demo-fever.svg", "demo-fever.svg");
        assets.put("demo-cough.svg", "demo-cough.svg");
        assets.put("demo-stomach.svg", "demo-stomach.svg");
        assets.put("demo-skin.svg", "demo-skin.svg");
        assets.put("demo-vitamin.svg", "demo-vitamin.svg");
        return assets;
    }
}