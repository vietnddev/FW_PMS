package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.ErrorCode;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.repository.product.ProductDetailRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductQuantityService;
import com.flowiee.pms.service.system.SystemLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductQuantityServiceImpl extends BaseService implements ProductQuantityService {
    private static final String mainObjectName = "ProductVariant";

    private final ProductDetailRepository productVariantRepo;
    private final SystemLogService        systemLogService;

    public ProductQuantityServiceImpl(ProductDetailRepository productVariantRepo, SystemLogService systemLogService) {
        this.productVariantRepo = productVariantRepo;
        this.systemLogService = systemLogService;
    }

    @Transactional
    @Override
    public void updateProductVariantQuantityIncrease(Integer pQuantity, Integer pProductVariantId) {
        this.updateProductVariantQuantity(pQuantity, pProductVariantId, "I");
    }

    @Transactional
    @Override
    public void updateProductVariantQuantityDecrease(Integer pQuantity, Integer pProductVariantId) {
        this.updateProductVariantQuantity(pQuantity, pProductVariantId, "D");
    }

    private void updateProductVariantQuantity(Integer quantity, Integer productVariantId, String type) {
        try {
            if ("I".equals(type)) {
                productVariantRepo.updateQuantityIncrease(quantity, productVariantId);
            } else if ("D".equals(type)) {
                productVariantRepo.updateQuantityDecrease(quantity, productVariantId);
            }
            systemLogService.writeLogUpdate(MODULE.PRODUCT.name(), ACTION.PRO_PRD_U.name(), mainObjectName, "Cập nhật số lượng sản phẩm", "productVariantId = " + productVariantId);
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.UPDATE_ERROR_OCCURRED.getDescription(), "product quantity"), ex);
        }
    }
}