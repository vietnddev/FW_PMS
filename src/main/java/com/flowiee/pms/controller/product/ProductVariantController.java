package com.flowiee.pms.controller.product;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.product.ProductHistory;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.model.AppResponse;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.model.dto.ProductVariantTempDTO;
import com.flowiee.pms.service.product.ProductHistoryService;
import com.flowiee.pms.service.product.ProductPriceService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.utils.constants.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app.api.prefix}/product")
@Tag(name = "Product API", description = "Quản lý biến thể sản phẩm")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductVariantController extends BaseController {
    ProductVariantService productVariantService;
    ProductHistoryService productHistoryService;
    ProductPriceService   productPriceService;

    @Operation(summary = "Find all variants")
    @GetMapping("/variant/all")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<List<ProductVariantDTO>> findProductVariants() {
        return success(productVariantService.findAll());
    }

    @Operation(summary = "Find all variants of product")
    @GetMapping("/{productId}/variants")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<List<ProductVariantDTO>> findVariantsOfProduct(@PathVariable("productId") Integer productId) {
        return success(productVariantService.findAll(-1, -1, productId, null, null, null, null, null).getContent());
    }

    @Operation(summary = "Find detail product variant")
    @GetMapping("/variant/{id}")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<ProductVariantDTO> findDetailProductVariant(@PathVariable("id") Integer productVariantId) {
        Optional<ProductVariantDTO> productVariant = productVariantService.findById(productVariantId);
        if (productVariant.isEmpty()) {
            throw new ResourceNotFoundException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "product"));
        }
        return success(productVariant.get());
    }

    @Operation(summary = "Create product variant")
    @PostMapping("/variant/create")
    @PreAuthorize("@vldModuleProduct.insertProduct(true)")
    public AppResponse<ProductDetail> createProductVariant(@RequestBody ProductVariantDTO productVariantDTO) {
        try {
            return success(productVariantService.save(productVariantDTO));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR_OCCURRED.getDescription(), "productVariant"), ex);
        }
    }

    @Operation(summary = "Update product variant")
    @PutMapping("/variant/update/{id}")
    @PreAuthorize("@vldModuleProduct.updateProduct(true)")
    public AppResponse<ProductDetail> updateProductVariant(@RequestBody ProductVariantDTO productVariant, @PathVariable("id") Integer productVariantId) {
        if (productVariantService.findById(productVariantId).isEmpty()) {
            throw new ResourceNotFoundException("Product variant not found!");
        }
        return success(productVariantService.update(productVariant, productVariantId));
    }

    @Operation(summary = "Delete product variant")
    @DeleteMapping("/variant/delete/{id}")
    @PreAuthorize("@vldModuleProduct.deleteProduct(true)")
    public AppResponse<String> deleteProductVariant(@PathVariable("id") Integer productVariantId) {
        return success(productVariantService.delete(productVariantId));
    }

    @Operation(summary = "Get price history of product detail")
    @GetMapping(value = "/variant/price/history/{Id}")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<List<ProductHistory>> getHistoryPriceOfProductDetail(@PathVariable("Id") Integer productVariantId) {
        if (ObjectUtils.isEmpty(productVariantService.findById(productVariantId))) {
            throw new ResourceNotFoundException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "product history"));
        }
        return success(productHistoryService.findPriceChange(productVariantId));
    }

    @Operation(summary = "Update price")
    @PutMapping(value = "/variant/price/update/{productVariantId}")
    @PreAuthorize("@vldModuleProduct.priceManagement(true)")
    public AppResponse<String> updatePrice(@PathVariable("productVariantId") Integer productVariantId,
                                           @RequestParam(value = "originalPrice", required = false) BigDecimal originalPrice,
                                           @RequestParam(value = "discountPrice", required = false) BigDecimal discountPrice) {
        try {
            if (productVariantService.findById(productVariantId).isEmpty()) {
                throw new BadRequestException();
            }
            return success(productPriceService.updateProductPrice(productVariantId, originalPrice, discountPrice));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.UPDATE_ERROR_OCCURRED.getDescription(), "price"), ex);
        }
    }

    @Operation(summary = "Check product variant already exists")
    @GetMapping("/variant/exists")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<Boolean> checkProductVariantAlreadyExists(@RequestParam("productId") Integer productId,
                                                                 @RequestParam("colorId") Integer colorId,
                                                                 @RequestParam("sizeId") Integer sizeId,
                                                                 @RequestParam("fabricTypeId") Integer fabricTypeId) {
        try {
            return success(productVariantService.isProductVariantExists(productId, colorId, sizeId, fabricTypeId));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "product"), ex);
        }
    }

    @Operation(summary = "Get history import/export storage of product variant")
    @GetMapping("/variant/{productVariantId}/storage-history")
    @PreAuthorize("@vldModuleProduct.readProduct(true)")
    public AppResponse<List<ProductVariantTempDTO>> getStorageHistoryOfProduct(@PathVariable("productVariantId") Integer productVariantId) {
        try {
            return success(productVariantService.findStorageHistory(productVariantId));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "product"), ex);
        }
    }
}