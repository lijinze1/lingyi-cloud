package com.lingyi.service.product.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.service.product.entity.LyCategory;
import com.lingyi.service.product.entity.LySku;
import com.lingyi.service.product.entity.LySkuStock;
import com.lingyi.service.product.entity.LySpu;
import com.lingyi.service.product.mapper.CategoryMapper;
import com.lingyi.service.product.mapper.SkuMapper;
import com.lingyi.service.product.mapper.SkuStockMapper;
import com.lingyi.service.product.mapper.SpuMapper;
import com.lingyi.service.product.service.ProductAssetService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductBootstrapRunner implements ApplicationRunner {

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final SkuStockMapper skuStockMapper;
    private final ProductAssetService productAssetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        productAssetService.ensureDemoAssetsUploaded();
        Long count = spuMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            return;
        }
        seedCategories();
        seedProducts();
    }

    private void seedCategories() {
        seedCategory(2001L, "感冒退热", 10);
        seedCategory(2002L, "止咳化痰", 20);
        seedCategory(2003L, "肠胃用药", 30);
        seedCategory(2004L, "皮肤护理", 40);
        seedCategory(2005L, "维矿补益", 50);
        seedCategory(2006L, "过敏常备", 60);
    }

    private void seedProducts() {
        seedProduct(3001L, 2001L, 4001L, 5001L, "感冒灵颗粒", "家庭常备感冒退热", "demo-cold.svg", "适合常见感冒发热、头痛、鼻塞等轻中度不适。", new BigDecimal("26.00"), new BigDecimal("39.00"), 320);
        seedProduct(3002L, 2001L, 4002L, 5002L, "布洛芬缓释胶囊", "发热与疼痛双场景常备", "demo-fever.svg", "适合缓解发热、头痛、牙痛与轻度肌肉酸痛。", new BigDecimal("32.00"), new BigDecimal("46.00"), 280);
        seedProduct(3003L, 2002L, 4003L, 5003L, "川贝枇杷糖浆", "换季咳嗽护理常备", "demo-cough.svg", "适合咳嗽、咽喉刺激与痰多等日常护理场景。", new BigDecimal("24.00"), new BigDecimal("36.00"), 240);
        seedProduct(3004L, 2002L, 4004L, 5004L, "复方鲜竹沥液", "止咳化痰常备用药", "demo-cough.svg", "适合咳痰偏多、咽部黏腻不适等场景。", new BigDecimal("21.00"), new BigDecimal("33.00"), 220);
        seedProduct(3005L, 2003L, 4005L, 5005L, "蒙脱石散", "肠胃敏感家庭常备", "demo-stomach.svg", "适合轻度腹泻、肠胃敏感时的基础护理。", new BigDecimal("19.00"), new BigDecimal("27.00"), 260);
        seedProduct(3006L, 2003L, 4006L, 5006L, "健胃消食片", "饭后胀满常备", "demo-stomach.svg", "适合饭后积食、轻度腹胀等消化护理场景。", new BigDecimal("17.00"), new BigDecimal("25.00"), 260);
        seedProduct(3007L, 2004L, 4007L, 5007L, "炉甘石洗剂", "皮肤瘙痒护理", "demo-skin.svg", "适合皮肤瘙痒、蚊虫叮咬等日常护理场景。", new BigDecimal("16.00"), new BigDecimal("23.00"), 180);
        seedProduct(3008L, 2004L, 4008L, 5008L, "莫匹罗星软膏", "创面护理常备", "demo-skin.svg", "适合轻度破损、局部护理需求较高的家庭场景。", new BigDecimal("29.00"), new BigDecimal("39.00"), 140);
        seedProduct(3009L, 2005L, 4009L, 5009L, "维生素C咀嚼片", "日常维C补充", "demo-vitamin.svg", "适合换季时期的维生素补充与家庭常备。", new BigDecimal("18.00"), new BigDecimal("26.00"), 300);
        seedProduct(3010L, 2005L, 4010L, 5010L, "钙维D咀嚼片", "骨骼营养补充", "demo-vitamin.svg", "适合成人与老人日常钙和维D补充。", new BigDecimal("34.00"), new BigDecimal("48.00"), 220);
        seedProduct(3011L, 2006L, 4011L, 5011L, "氯雷他定片", "过敏季节常备", "demo-fever.svg", "适合季节性鼻痒、流涕与轻度过敏场景。", new BigDecimal("27.00"), new BigDecimal("38.00"), 190);
        seedProduct(3012L, 2001L, 4012L, 5012L, "板蓝根颗粒", "咽喉不适常备", "demo-cold.svg", "适合换季时期咽喉不适与居家常备。", new BigDecimal("18.00"), new BigDecimal("28.00"), 260);
    }

    private void seedCategory(Long id, String name, Integer sortNo) {
        LyCategory category = new LyCategory();
        category.setId(id);
        category.setParentId(0L);
        category.setName(name);
        category.setSortNo(sortNo);
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    private void seedProduct(Long spuId,
                             Long categoryId,
                             Long skuId,
                             Long stockId,
                             String name,
                             String subTitle,
                             String mainImage,
                             String detail,
                             BigDecimal price,
                             BigDecimal originPrice,
                             Integer stockTotal) {
        LySpu spu = new LySpu();
        spu.setId(spuId);
        spu.setCategoryId(categoryId);
        spu.setName(name);
        spu.setSubTitle(subTitle);
        spu.setMainImage(mainImage);
        spu.setDetail(detail);
        spu.setStatus(1);
        spuMapper.insert(spu);

        LySku sku = new LySku();
        sku.setId(skuId);
        sku.setSpuId(spuId);
        sku.setSkuCode("SKU-" + skuId);
        sku.setTitle(name + " 标准装");
        sku.setAttrsJson("{\"规格\":\"标准装\"}");
        sku.setPrice(price);
        sku.setOriginPrice(originPrice);
        sku.setStatus(1);
        skuMapper.insert(sku);

        LySkuStock stock = new LySkuStock();
        stock.setId(stockId);
        stock.setSkuId(skuId);
        stock.setStockTotal(stockTotal);
        stock.setStockAvailable(stockTotal);
        stock.setStockLocked(0);
        stock.setVersion(0);
        skuStockMapper.insert(stock);
    }
}
