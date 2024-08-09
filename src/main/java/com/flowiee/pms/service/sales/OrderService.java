package com.flowiee.pms.service.sales;

import com.flowiee.pms.service.BaseCurdService;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.entity.sales.Order;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService extends BaseCurdService<OrderDTO> {
    Page<OrderDTO> findAll(int pageSize, int pageNum, String pTxtSearch, Integer pOrderId, Integer pPaymentMethodId,
                           Integer pOrderStatusId, Integer pSalesChannelId, Integer pSellerId, Integer pCustomerId,
                           Integer pBranchId, Integer pGroupCustomerId, LocalDateTime pOrderTimeFrom, LocalDateTime pOrderTimeTo, String pSortBy);

    Optional<OrderDTO> findById(Integer orderId);

    String doPay(Integer orderId, LocalDateTime paymentTime, Integer paymentMethod, Float paymentAmount, String paymentNote);

    List<Order> findOrdersToday();
}