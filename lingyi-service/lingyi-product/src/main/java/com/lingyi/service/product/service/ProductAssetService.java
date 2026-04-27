package com.lingyi.service.product.service;

public interface ProductAssetService {

    String resolvePublicUrl(String assetKey);

    void ensureDemoAssetsUploaded();
}