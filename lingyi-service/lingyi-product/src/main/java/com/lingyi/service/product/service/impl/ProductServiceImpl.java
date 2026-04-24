package com.lingyi.service.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.product.convert.ProductConvert;
import com.lingyi.service.product.dto.CategorySaveRequest;
import com.lingyi.service.product.dto.ProductPageQuery;
import com.lingyi.service.product.dto.SkuSaveRequest;
import com.lingyi.service.product.dto.SpuSaveRequest;
import com.lingyi.service.product.dto.StockChangeRequest;
import com.lingyi.service.product.dto.StockSetRequest;
import com.lingyi.service.product.entity.LyCategory;
import com.lingyi.service.product.entity.LySku;
import com.lingyi.service.product.entity.LySkuStock;
import com.lingyi.service.product.entity.LySpu;
import com.lingyi.service.product.mapper.CategoryMapper;
import com.lingyi.service.product.mapper.SkuMapper;
import com.lingyi.service.product.mapper.SkuStockMapper;
import com.lingyi.service.product.mapper.SpuMapper;
import com.lingyi.service.product.service.ProductService;
import com.lingyi.service.product.vo.CategoryVO;
import com.lingyi.service.product.vo.PageVO;
import com.lingyi.service.product.vo.ProductDetailVO;
import com.lingyi.service.product.vo.SkuVO;
import com.lingyi.service.product.vo.SpuVO;
import com.lingyi.service.product.vo.StockVO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int ONLINE = 1;

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final SkuStockMapper skuStockMapper;

    @Override
    public List<CategoryVO> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<LyCategory>()
                        .eq(LyCategory::getStatus, ONLINE)
                        .orderByAsc(LyCategory::getSortNo)
                        .orderByDesc(LyCategory::getCreatedAt))
                .stream()
                .map(ProductConvert::toCategoryVO)
                .toList();
    }

    @Override
    public CategoryVO saveCategory(CategorySaveRequest request) {
        LyCategory entity = Optional.ofNullable(request.getId())
                .map(categoryMapper::selectById)
                .orElseGet(LyCategory::new);
        entity.setParentId(Optional.ofNullable(request.getParentId()).orElse(0L));
        entity.setName(request.getName());
        entity.setSortNo(Optional.ofNullable(request.getSortNo()).orElse(0));
        entity.setStatus(Optional.ofNullable(request.getStatus()).orElse(ONLINE));
        if (entity.getId() == null) {
            categoryMapper.insert(entity);
        } else {
            categoryMapper.updateById(entity);
        }
        return ProductConvert.toCategoryVO(entity);
    }

    @Override
    public PageVO<SpuVO> pageProducts(ProductPageQuery query, boolean onlyOnline) {
        long pageNo = Math.max(query.getPageNo(), 1);
        long pageSize = Math.min(Math.max(query.getPageSize(), 1), 100);
        LambdaQueryWrapper<LySpu> wrapper = new LambdaQueryWrapper<LySpu>()
                .eq(query.getCategoryId() != null, LySpu::getCategoryId, query.getCategoryId())
                .eq(onlyOnline, LySpu::getStatus, ONLINE)
                .eq(query.getStatus() != null, LySpu::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(LySpu::getName, query.getKeyword())
                        .or()
                        .like(LySpu::getSubTitle, query.getKeyword()))
                .orderByDesc(LySpu::getCreatedAt);
        Page<LySpu> page = spuMapper.selectPage(Page.of(pageNo, pageSize), wrapper);
        List<LySpu> spus = page.getRecords();
        if (spus.isEmpty()) {
            return PageVO.of(pageNo, pageSize, page.getTotal(), List.of());
        }
        Map<Long, LyCategory> categoryMap = loadCategoryMap(spus.stream().map(LySpu::getCategoryId).toList());
        Map<Long, List<LySku>> skuMap = loadSkuMap(spus.stream().map(LySpu::getId).toList(), onlyOnline);
        Map<Long, LySkuStock> stockMap = loadStockMap(skuMap.values().stream().flatMap(List::stream).map(LySku::getId).toList());
        List<SpuVO> records = spus.stream().map(spu -> {
            SpuVO vo = ProductConvert.toSpuVO(spu);
            vo.setCategoryName(Optional.ofNullable(categoryMap.get(spu.getCategoryId())).map(LyCategory::getName).orElse(null));
            List<LySku> skus = skuMap.getOrDefault(spu.getId(), List.of());
            vo.setMinPrice(skus.stream().map(LySku::getPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            vo.setStockAvailable(skus.stream().map(sku -> stockMap.get(sku.getId())).filter(Objects::nonNull).mapToInt(LySkuStock::getStockAvailable).sum());
            return vo;
        }).toList();
        return PageVO.of(pageNo, pageSize, page.getTotal(), records);
    }

    @Override
    public ProductDetailVO getProductDetail(Long spuId, boolean onlyOnline) {
        LySpu spu = spuMapper.selectById(spuId);
        if (spu == null || (onlyOnline && !Objects.equals(spu.getStatus(), ONLINE))) {
            throw new BizException("P0404", "商品不存在或已下架");
        }
        ProductDetailVO vo = ProductConvert.toDetailVO(spu);
        LyCategory category = categoryMapper.selectById(spu.getCategoryId());
        vo.setCategoryName(Optional.ofNullable(category).map(LyCategory::getName).orElse(null));
        List<LySku> skus = skuMapper.selectList(new LambdaQueryWrapper<LySku>()
                .eq(LySku::getSpuId, spuId)
                .eq(onlyOnline, LySku::getStatus, ONLINE)
                .orderByAsc(LySku::getPrice));
        Map<Long, LySkuStock> stockMap = loadStockMap(skus.stream().map(LySku::getId).toList());
        vo.setSkus(skus.stream().map(sku -> ProductConvert.toSkuVO(sku, stockMap.get(sku.getId()))).toList());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductDetailVO saveSpu(SpuSaveRequest request) {
        if (categoryMapper.selectById(request.getCategoryId()) == null) {
            throw new BizException("P0404", "商品分类不存在");
        }
        LySpu spu = Optional.ofNullable(request.getId()).map(spuMapper::selectById).orElseGet(LySpu::new);
        spu.setCategoryId(request.getCategoryId());
        spu.setName(request.getName());
        spu.setSubTitle(request.getSubTitle());
        spu.setMainImage(request.getMainImage());
        spu.setDetail(request.getDetail());
        spu.setStatus(Optional.ofNullable(request.getStatus()).orElse(ONLINE));
        if (spu.getId() == null) {
            spuMapper.insert(spu);
        } else {
            spuMapper.updateById(spu);
        }
        for (SkuSaveRequest skuRequest : Optional.ofNullable(request.getSkus()).orElseGet(ArrayList::new)) {
            saveSku(spu.getId(), skuRequest);
        }
        return getProductDetail(spu.getId(), false);
    }

    @Override
    public void updateSpuStatus(Long spuId, Integer status) {
        LySpu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BizException("P0404", "商品不存在");
        }
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    @Override
    public SkuVO getSku(Long skuId, boolean onlyOnline) {
        LySku sku = skuMapper.selectById(skuId);
        if (sku == null || (onlyOnline && !Objects.equals(sku.getStatus(), ONLINE))) {
            throw new BizException("P0404", "SKU 不存在或已下架");
        }
        if (onlyOnline) {
            LySpu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null || !Objects.equals(spu.getStatus(), ONLINE)) {
                throw new BizException("P0404", "商品不存在或已下架");
            }
        }
        LySkuStock stock = selectStockBySkuId(skuId);
        return ProductConvert.toSkuVO(sku, stock);
    }

    @Override
    public StockVO getStock(Long skuId) {
        LySkuStock stock = selectStockBySkuId(skuId);
        if (stock == null) {
            throw new BizException("P0404", "库存记录不存在");
        }
        return ProductConvert.toStockVO(stock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStock(StockSetRequest request) {
        if (skuMapper.selectById(request.getSkuId()) == null) {
            throw new BizException("P0404", "SKU 不存在");
        }
        LySkuStock stock = Optional.ofNullable(selectStockBySkuId(request.getSkuId())).orElseGet(LySkuStock::new);
        stock.setSkuId(request.getSkuId());
        stock.setStockTotal(request.getStockTotal());
        stock.setStockAvailable(request.getStockAvailable());
        stock.setStockLocked(Math.max(0, request.getStockTotal() - request.getStockAvailable()));
        stock.setVersion(Optional.ofNullable(stock.getVersion()).orElse(0));
        if (stock.getId() == null) {
            skuStockMapper.insert(stock);
        } else {
            skuStockMapper.updateById(stock);
        }
    }

    @Override
    public void lockStock(StockChangeRequest request) {
        if (skuStockMapper.lockStock(request.getSkuId(), request.getQuantity()) == 0) {
            throw new BizException("P0405", "库存不足，无法锁定");
        }
    }

    @Override
    public void releaseStock(StockChangeRequest request) {
        if (skuStockMapper.releaseStock(request.getSkuId(), request.getQuantity()) == 0) {
            throw new BizException("P0406", "锁定库存不足，无法释放");
        }
    }

    @Override
    public void deductLockedStock(StockChangeRequest request) {
        if (skuStockMapper.deductLockedStock(request.getSkuId(), request.getQuantity()) == 0) {
            throw new BizException("P0407", "锁定库存不足，无法扣减");
        }
    }

    private void saveSku(Long spuId, SkuSaveRequest request) {
        LySku sku = Optional.ofNullable(request.getId()).map(skuMapper::selectById).orElseGet(LySku::new);
        sku.setSpuId(spuId);
        sku.setSkuCode(StringUtils.hasText(request.getSkuCode()) ? request.getSkuCode() : "SKU-" + spuId + "-" + System.nanoTime());
        sku.setTitle(request.getTitle());
        sku.setAttrsJson(request.getAttrsJson());
        sku.setPrice(request.getPrice());
        sku.setOriginPrice(request.getOriginPrice());
        sku.setStatus(Optional.ofNullable(request.getStatus()).orElse(ONLINE));
        if (sku.getId() == null) {
            skuMapper.insert(sku);
        } else {
            skuMapper.updateById(sku);
        }
        StockSetRequest stockRequest = new StockSetRequest();
        stockRequest.setSkuId(sku.getId());
        stockRequest.setStockTotal(Optional.ofNullable(request.getStockTotal()).orElse(0));
        stockRequest.setStockAvailable(Optional.ofNullable(request.getStockTotal()).orElse(0));
        setStock(stockRequest);
    }

    private Map<Long, LyCategory> loadCategoryMap(List<Long> ids) {
        List<Long> actualIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (actualIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(actualIds).stream().collect(Collectors.toMap(LyCategory::getId, Function.identity()));
    }

    private Map<Long, List<LySku>> loadSkuMap(List<Long> spuIds, boolean onlyOnline) {
        List<Long> actualIds = spuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (actualIds.isEmpty()) {
            return Map.of();
        }
        return skuMapper.selectList(new LambdaQueryWrapper<LySku>()
                        .in(LySku::getSpuId, actualIds)
                        .eq(onlyOnline, LySku::getStatus, ONLINE))
                .stream()
                .sorted(Comparator.comparing(LySku::getPrice))
                .collect(Collectors.groupingBy(LySku::getSpuId));
    }

    private Map<Long, LySkuStock> loadStockMap(List<Long> skuIds) {
        List<Long> actualIds = skuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (actualIds.isEmpty()) {
            return new HashMap<>();
        }
        return skuStockMapper.selectList(new LambdaQueryWrapper<LySkuStock>().in(LySkuStock::getSkuId, actualIds))
                .stream()
                .collect(Collectors.toMap(LySkuStock::getSkuId, Function.identity(), (left, right) -> left));
    }

    private LySkuStock selectStockBySkuId(Long skuId) {
        return skuStockMapper.selectOne(new LambdaQueryWrapper<LySkuStock>().eq(LySkuStock::getSkuId, skuId).last("LIMIT 1"));
    }
}
