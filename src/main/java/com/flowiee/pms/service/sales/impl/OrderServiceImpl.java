package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.sales.*;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.repository.sales.CustomerRepository;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.constants.*;
import com.flowiee.pms.model.dto.OrderDetailDTO;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.exception.DataInUseException;
import com.flowiee.pms.model.dto.ProductVariantDTO;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.product.ProductVariantService;
import com.flowiee.pms.service.sales.*;
import com.flowiee.pms.utils.*;
import com.flowiee.pms.entity.category.Category;
import com.flowiee.pms.repository.sales.OrderRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderServiceImpl extends BaseService implements OrderService {
    CartService           cartService;
    OrderRepository       orderRepository;
    CartItemsService      cartItemsService;
    OrderItemsService     orderItemsService;
    OrderQRCodeService    orderQRCodeService;
    CustomerRepository    customerRepository;
    OrderHistoryService   orderHistoryService;
    VoucherTicketService  voucherTicketService;
    ProductVariantService productVariantService;

    @Override
    public List<OrderDTO> findAll() {
        return this.findAll(-1, -1, null, null, null, null, null, null, null, null, null, null, null, null).getContent();
    }

    @Override
    public Page<OrderDTO> findAll(int pPageSize, int pPageNum, String pTxtSearch, Integer pOrderId, Integer pPaymentMethodId,
                                  Integer pOrderStatusId, Integer pSalesChannelId, Integer pSellerId, Integer pCustomerId,
                                  Integer pBranchId, Integer pGroupCustomerId, LocalDateTime pOrderTimeFrom, LocalDateTime pOrderTimeTo, String pSortBy) {
        Pageable pageable = Pageable.unpaged();
        if (pPageSize >= 0 && pPageNum >= 0) {
            pageable = PageRequest.of(pPageNum, pPageSize, Sort.by(pSortBy != null ? pSortBy : "orderTime").descending());
        }
        if (pOrderTimeFrom == null) {
            pOrderTimeFrom = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
        }
        if (pOrderTimeTo == null) {
            pOrderTimeTo = LocalDateTime.of(2100, 12, 1, 0, 0, 0);
        }
        Page<Order> orders = orderRepository.findAll(pTxtSearch, pOrderId, pPaymentMethodId, pOrderStatusId, pSalesChannelId, pSellerId, pCustomerId, pBranchId, pGroupCustomerId, pOrderTimeFrom, pOrderTimeTo, pageable);
        return new PageImpl<>(OrderDTO.fromOrders(orders.getContent()), pageable, orders.getTotalElements());
    }

    @Override
    public Optional<OrderDTO> findById(Integer orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return Optional.empty();
        }
        OrderDTO order = OrderDTO.fromOrder(orderOptional.get());
        List<OrderDetail> listOrderDetail = orderItemsService.findByOrderId(orderId);
        int totalProduct = 0;
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderDetail d : listOrderDetail) {
            totalProduct += d.getQuantity();
            totalAmount = totalAmount.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
        }
        List<OrderDetailDTO> orderDetailDTOs = OrderDetailDTO.fromOrderDetails(listOrderDetail);;
        order.setListOrderDetailDTO(orderDetailDTOs);
        order.setTotalProduct(totalProduct);
        order.setTotalAmount(totalAmount);
        order.setTotalAmountDiscount(order.getTotalAmount().subtract(order.getAmountDiscount()));
        return Optional.of(order);
    }

    @Transactional
    @Override
    public OrderDTO save(OrderDTO request) {
        try {
            //Insert order
            Order order = Order.builder()
                .code(getNextOrderCode())
                .customer(new Customer(request.getCustomerId()))
                .kenhBanHang(new Category(request.getSalesChannelId(), null))
                .nhanVienBanHang(new Account(request.getCashierId()))
                .note(request.getNote())
                .orderTime(request.getOrderTime() != null ? request.getOrderTime() : LocalDateTime.now())
                .trangThaiDonHang(new Category(request.getOrderStatusId(), null))
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverEmail(request.getReceiverEmail())
                .receiverAddress(request.getReceiverAddress())
                .paymentMethod(request.getPayMethodId() != null ? new Category(request.getPayMethodId(), null) : null)
                .paymentStatus(false)
                .voucherUsedCode(ObjectUtils.isNotEmpty(request.getVoucherUsedCode()) ? request.getVoucherUsedCode() : null)
                .amountDiscount(request.getAmountDiscount() != null ? request.getAmountDiscount() : BigDecimal.ZERO)
                .packagingCost(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .giftWrapCost(BigDecimal.ZERO)
                .codFee(BigDecimal.ZERO)
                .isGiftWrapped(false)
                .build();
            Order orderSaved = orderRepository.save(order);

            //QRCode
            orderQRCodeService.saveQRCodeOfOrder(orderSaved.getId());

            //Insert items detail
            BigDecimal totalAmountOfOrder = BigDecimal.ZERO;
            Optional<OrderCart> cart = cartService.findById(request.getCartId());
            if (cart.isPresent()) {
                for (Items items : cart.get().getListItems()) {
                    Optional<ProductVariantDTO> productDetail = productVariantService.findById(items.getProductDetail().getId());
                    if (productDetail.isPresent()) {
                        OrderDetail orderDetail = OrderDetail.builder()
                            .order(orderSaved)
                            .productDetail(productDetail.get())
                            .quantity(cartItemsService.findQuantityOfItem(items.getOrderCart().getId() , productDetail.get().getId()))
                            .status(true)
                            .note(items.getNote())
                            .price(items.getPrice())
                            .priceOriginal(items.getPriceOriginal())
                            .extraDiscount(items.getExtraDiscount() != null ? items.getExtraDiscount() : BigDecimal.ZERO)
                            .priceType(items.getPriceType())
                            .build();
                        OrderDetail orderDetailSaved = orderItemsService.save(orderDetail);

                        totalAmountOfOrder = totalAmountOfOrder.add(orderDetailSaved.getPrice().multiply(BigDecimal.valueOf(orderDetailSaved.getQuantity())));
                    }
                }
            }

            //Update voucher ticket used status
            VoucherTicket voucherTicket = voucherTicketService.findTicketByCode((request.getVoucherUsedCode()));
            if (voucherTicket != null) {
//                String statusCode = voucherService.checkTicketToUse(request.getVoucherUsedCode());
//                if (AppConstants.VOUCHER_STATUS.ACTIVE.name().equals(statusCode)) {
//                    orderSaved.setVoucherUsedCode(request.getVoucherUsedCode());
//                    orderSaved.setAmountDiscount(request.getAmountDiscount());
//                }
                voucherTicket.setCustomer(orderSaved.getCustomer());
                voucherTicket.setActiveTime(new Date());
                voucherTicket.setUsed(true);
                voucherTicketService.update(voucherTicket, voucherTicket.getId());
            }
            //orderRepo.save(orderSaved);

            //Accumulate bonus points for customer
            if (request.getAccumulateBonusPoints() != null && request.getAccumulateBonusPoints()) {
                BigDecimal bonusPoints = totalAmountOfOrder.subtract(orderSaved.getAmountDiscount()).divide(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
                customerRepository.updateBonusPoint(orderSaved.getCustomer().getId(), bonusPoints.intValue());
            }

            //Sau khi đã lưu đơn hàng thì xóa all items
            cartItemsService.deleteAllItems();

            //Log
            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_ORD_C, MasterObject.Order, "Thêm mới đơn hàng", orderSaved.getCode());
            logger.info("Insert new order success! insertBy={}", CommonUtils.getUserPrincipal().getUsername());

            return OrderDTO.fromOrder(orderSaved);
        } catch (RuntimeException ex) {
            throw new AppException("Insert new order fail! order=" + request, ex);
        }
    }

    @Override
    public OrderDTO update(OrderDTO dto, Integer id) {
        Order orderToUpdate = orderRepository.findById(id).orElse(null);
        if (orderToUpdate == null) {
            throw new ResourceNotFoundException("Order not found!");
        }
        Order orderBefore = ObjectUtils.clone(orderToUpdate);
        orderToUpdate.setNote(dto.getNote());
        orderToUpdate.setTrangThaiDonHang(new Category(dto.getOrderStatusId(), null));
        Order orderUpdated = orderRepository.save(orderToUpdate);

        ChangeLog changeLog = new ChangeLog(orderBefore, orderUpdated);
        orderHistoryService.save(changeLog.getLogChanges(), "Cập nhật đơn hàng", id, null);
        systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_ORD_U, MasterObject.Order, "Cập nhật đơn hàng", changeLog);
        logger.info("Cập nhật đơn hàng {}", dto.toString());

        return OrderDTO.fromOrder(orderToUpdate);
    }

    @Override
    public String delete(Integer id) {
        Optional<OrderDTO> order = this.findById(id);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found!");
        }
        if (order.get().getPaymentStatus()) {
            throw new DataInUseException(ErrorCode.ERROR_DATA_LOCKED.getDescription());
        }
        orderRepository.deleteById(id);
        systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_ORD_D, MasterObject.Order, "Xóa đơn hàng", order.toString());
        logger.info("Xóa đơn hàng orderId={}", id);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Transactional
    @Override
    public String doPay(Integer orderId, LocalDateTime paymentTime, Integer paymentMethod, Float paymentAmount, String paymentNote) {
        Optional<OrderDTO> dto = this.findById(orderId);
        if (dto.isEmpty()) {
            throw new ResourceNotFoundException("Order not found!");
        }
        if (dto.get().getPaymentStatus()) {
            throw new BadRequestException("The order has been paid");
        }
        if ((paymentMethod == null || paymentMethod <= 0) || paymentAmount == null) {
            throw new BadRequestException("Thông tin thanh toán không hợp lệ!");
        }
        if (paymentTime == null) paymentTime = LocalDateTime.now();
        orderRepository.updatePaymentStatus(orderId, paymentTime, paymentMethod, paymentAmount, paymentNote);
        systemLogService.writeLogUpdate(MODULE.SALES, ACTION.PRO_ORD_U, MasterObject.Order, "Cập nhật trạng thái thanh toán đơn hàng", "Số tiền: " + CommonUtils.formatToVND(paymentAmount));
        return MessageCode.UPDATE_SUCCESS.getDescription();
    }

    @Override
    public List<Order> findOrdersToday() {
        return orderRepository.findOrdersToday();
    }

    private String getNextOrderCode() {
        int orderTodayQty = 0;
        List<Order> ordersToday = orderRepository.findOrdersToday();
        if (ordersToday != null) {
            orderTodayQty = ordersToday.size();
        }
        LocalDate currentDate = LocalDate.now();
        String year = String.valueOf(currentDate.getYear());
        String month = String.format("%02d", currentDate.getMonthValue());
        String day = String.format("%02d", currentDate.getDayOfMonth());
        return year + month + day + String.format("%03d", orderTodayQty + 1);
    }
}