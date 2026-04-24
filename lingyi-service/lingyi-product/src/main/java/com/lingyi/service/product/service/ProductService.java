package com.lingyi.service.product.service;

import com.lingyi.service.product.dto.CategorySaveRequest;
import com.lingyi.service.product.dto.ProductPageQuery;
import com.lingyi.service.product.dto.SpuSaveRequest;
import com.lingyi.service.product.dto.StockChangeRequest;
import com.lingyi.service.product.dto.StockSetRequest;
import com.lingyi.service.product.vo.CategoryVO;
import com.lingyi.service.product.vo.PageVO;
import com.lingyi.service.product.vo.ProductDetailVO;
import com.lingyi.service.product.vo.SkuVO;
import com.lingyi.service.product.vo.SpuVO;
import com.lingyi.service.product.vo.StockVO;
import java.util.List;

public interface ProductService {
    List<CategoryVO> listCategories();

    CategoryVO saveCategory(CategorySaveRequest request);

    PageVO<SpuVO> pageProducts(ProductPageQuery query, boolean onlyOnline);

    ProductDetailVO getProductDetail(Long spuId, boolean onlyOnline);

    ProductDetailVO saveSpu(SpuSaveRequest request);

    void updateSpuStatus(Long spuId, Integer status);

    SkuVO getSku(Long skuId, boolean onlyOnline);

    StockVO getStock(Long skuId);

    void setStock(StockSetRequest request);

    void lockStock(StockChangeRequest request);

    void releaseStock(StockChangeRequest request);

    void deductLockedStock(StockChangeRequest request);
}
