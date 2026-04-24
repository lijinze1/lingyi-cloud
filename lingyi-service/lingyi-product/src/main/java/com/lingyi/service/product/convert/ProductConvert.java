package com.lingyi.service.product.convert;

import com.lingyi.service.product.entity.LyCategory;
import com.lingyi.service.product.entity.LySku;
import com.lingyi.service.product.entity.LySkuStock;
import com.lingyi.service.product.entity.LySpu;
import com.lingyi.service.product.vo.CategoryVO;
import com.lingyi.service.product.vo.ProductDetailVO;
import com.lingyi.service.product.vo.SkuVO;
import com.lingyi.service.product.vo.SpuVO;
import com.lingyi.service.product.vo.StockVO;
import java.util.Optional;

public final class ProductConvert {

    private ProductConvert() {
    }

    public static CategoryVO toCategoryVO(LyCategory entity) {
        CategoryVO vo = new CategoryVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setName(entity.getName());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    public static SpuVO toSpuVO(LySpu spu) {
        SpuVO vo = new SpuVO();
        vo.setId(spu.getId());
        vo.setCategoryId(spu.getCategoryId());
        vo.setName(spu.getName());
        vo.setSubTitle(spu.getSubTitle());
        vo.setMainImage(spu.getMainImage());
        vo.setStatus(spu.getStatus());
        return vo;
    }

    public static ProductDetailVO toDetailVO(LySpu spu) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(spu.getId());
        vo.setCategoryId(spu.getCategoryId());
        vo.setName(spu.getName());
        vo.setSubTitle(spu.getSubTitle());
        vo.setMainImage(spu.getMainImage());
        vo.setDetail(spu.getDetail());
        vo.setStatus(spu.getStatus());
        return vo;
    }

    public static SkuVO toSkuVO(LySku sku, LySkuStock stock) {
        SkuVO vo = new SkuVO();
        vo.setId(sku.getId());
        vo.setSpuId(sku.getSpuId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setTitle(sku.getTitle());
        vo.setAttrsJson(sku.getAttrsJson());
        vo.setPrice(sku.getPrice());
        vo.setOriginPrice(sku.getOriginPrice());
        vo.setStatus(sku.getStatus());
        vo.setStockTotal(Optional.ofNullable(stock).map(LySkuStock::getStockTotal).orElse(0));
        vo.setStockAvailable(Optional.ofNullable(stock).map(LySkuStock::getStockAvailable).orElse(0));
        vo.setStockLocked(Optional.ofNullable(stock).map(LySkuStock::getStockLocked).orElse(0));
        return vo;
    }

    public static StockVO toStockVO(LySkuStock stock) {
        StockVO vo = new StockVO();
        vo.setSkuId(stock.getSkuId());
        vo.setStockTotal(stock.getStockTotal());
        vo.setStockAvailable(stock.getStockAvailable());
        vo.setStockLocked(stock.getStockLocked());
        return vo;
    }
}
