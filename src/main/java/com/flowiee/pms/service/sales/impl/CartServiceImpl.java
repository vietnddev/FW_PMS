package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.product.ProductDetail;
import com.flowiee.pms.entity.sales.Items;
import com.flowiee.pms.entity.sales.OrderCart;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.repository.sales.CartItemsRepository;
import com.flowiee.pms.repository.sales.OrderCartRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.CartItemsService;
import com.flowiee.pms.service.sales.CartService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartServiceImpl extends BaseService implements CartService {
    OrderCartRepository   cartRepository;
    CartItemsService      cartItemsService;
    CartItemsRepository   cartItemsRepository;
    ProductVariantService productVariantService;

    @Override
    public List<OrderCart> findCartByAccountId(Integer accountId) {
        List<OrderCart> listCart = cartRepository.findByAccountId(accountId);
        for (OrderCart cart : listCart) {
            for (Items item : cart.getListItems()) {
                Optional<ProductVariantDTO> productDetail = productVariantService.findById(item.getProductDetail().getId());
                if (productDetail.isPresent()) {
                    BigDecimal originalPrice = productDetail.get().getOriginalPrice();
                    BigDecimal discountPrice = productDetail.get().getDiscountPrice();
                    item.setPrice(discountPrice != null ? discountPrice : originalPrice);
                    item.getProductDetail().setAvailableSalesQty(productDetail.get().getStorageQty() - productDetail.get().getDefectiveQty());
                }
            }
        }
        return listCart;
    }

    @Override
    public List<OrderCart> findAll() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<OrderCart> findById(Integer id) {
        return cartRepository.findById(id);
    }

    @Override
    public OrderCart save(OrderCart orderCart) {
        if (orderCart == null) {
            throw new BadRequestException();
        }
        return cartRepository.save(orderCart);
    }

    @Override
    public OrderCart update(OrderCart cart, Integer cartId) {
        if (this.findById(cartId).isEmpty()) {
            throw new BadRequestException();
        }
        cart.setId(cartId);
        return cartRepository.save(cart);
    }

    @Override
    public String delete(Integer cartId) {
        if (this.findById(cartId).isEmpty()) {
            throw new BadRequestException();
        }
        cartRepository.deleteById(cartId);
        systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_CART_C, MasterObject.Cart, "Xóa/Reset giỏ hàng", "cartId = " + cartId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public Double calTotalAmountWithoutDiscount(int cartId) {
        return cartItemsRepository.calTotalAmountWithoutDiscount(cartId);
    }

    @Override
    public boolean isItemExistsInCart(Integer cartId, Integer productVariantId) {
        Items item = cartItemsRepository.findByCartAndProductVariant(cartId, productVariantId);
        return item != null;
    }

    @Transactional
    @Override
    public void resetCart(Integer cartId) {
        Optional<OrderCart> cartOptional = this.findById(cartId);
        cartOptional.ifPresent(orderCart -> cartItemsRepository.deleteAllItems(orderCart.getId()));
    }

    @Override
    public void addItemsToCart(Integer cartId, String[] productVariantIds) {
        List<String> listProductVariantId = Arrays.stream(productVariantIds).toList();
        for (String productVariantId : listProductVariantId) {
            Optional<ProductVariantDTO> productVariant = productVariantService.findById(Integer.parseInt(productVariantId));
            if (productVariant.isEmpty()) {
                continue;
            }
            if (this.isItemExistsInCart(cartId, productVariant.get().getId())) {
                Items items = cartItemsService.findItemByCartAndProductVariant(cartId, productVariant.get().getId());
                cartItemsService.increaseItemQtyInCart(items.getId(), items.getQuantity() + 1);
            } else {
                Items items = Items.builder()
                    .orderCart(new OrderCart(cartId))
                    .productDetail(new ProductDetail(Integer.parseInt(productVariantId)))
                    .priceType(PriceType.L.name())
                    .price(productVariant.get().getRetailPriceDiscount() != null ? productVariant.get().getRetailPriceDiscount() : productVariant.get().getRetailPrice())
                    .priceOriginal(productVariant.get().getRetailPrice())
                    .extraDiscount(BigDecimal.ZERO)
                    .quantity(1)
                    .note("")
                    .build();
                cartItemsService.save(items);
            }
        }
    }

    @Override
    public void updateItemsOfCart(Items itemToUpdate, Integer itemId) {
        Optional<Items> itemOptional = cartItemsService.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new BadRequestException("Item not found!");
        }
        Items item = itemOptional.get();
        if (itemToUpdate.getQuantity() <= 0) {
            cartItemsService.delete(item.getId());
        } else {
            ProductDetail productVariant = item.getProductDetail();
            if (itemToUpdate.getQuantity() > productVariant.getAvailableSalesQty()) {
                throw new BadRequestException("This item's quantity is over available quantity!");
            }
            item.setNote(itemToUpdate.getNote());
            item.setQuantity(itemToUpdate.getQuantity());
            if (itemToUpdate.getPriceType() != null && (!item.getPriceType().equals(itemToUpdate.getPriceType()))) {
                if (itemToUpdate.getPriceType().equals(PriceType.L.name())) {
                    item.setPrice(productVariant.getRetailPriceDiscount());
                    item.setPriceOriginal(productVariant.getRetailPrice());
                    item.setPriceType(PriceType.L.name());
                }
                if (itemToUpdate.getPriceType().equals(PriceType.S.name())) {
                    item.setPrice(productVariant.getWholesalePriceDiscount());
                    item.setPriceOriginal(productVariant.getWholesalePrice());
                    item.setPriceType(PriceType.S.name());
                }
            }
            if (itemToUpdate.getExtraDiscount() != null) {
                item.setExtraDiscount(itemToUpdate.getExtraDiscount());
            }
            cartItemsService.update(item, item.getId());
        }
    }
}