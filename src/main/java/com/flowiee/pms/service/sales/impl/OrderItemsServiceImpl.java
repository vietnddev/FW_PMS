package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.sales.OrderDetail;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.utils.ChangeLog;
import com.flowiee.pms.utils.constants.ACTION;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.repository.sales.OrderDetailRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.OrderHistoryService;
import com.flowiee.pms.service.sales.OrderItemsService;
import com.flowiee.pms.service.system.SystemLogService;
import com.flowiee.pms.utils.constants.MasterObject;
import com.flowiee.pms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderItemsServiceImpl extends BaseService implements OrderItemsService {
    OrderDetailRepository orderDetailRepo;
    SystemLogService      systemLogService;
    OrderHistoryService   orderHistoryService;

    @Override
    public List<OrderDetail> findAll() {
        return orderDetailRepo.findAll();
    }

    @Override
    public Optional<OrderDetail> findById(Integer orderDetailId) {
        return orderDetailRepo.findById(orderDetailId);
    }

    @Override
    public List<OrderDetail> findByOrderId(Integer orderId) {
        return orderDetailRepo.findByOrderId(orderId);
    }

    @Override
    public OrderDetail save(OrderDetail orderDetail) {
        try {
            if (orderDetail.getExtraDiscount() == null) {
                orderDetail.setExtraDiscount(BigDecimal.ZERO);
            }
            OrderDetail orderDetailSaved = orderDetailRepo.save(orderDetail);
            systemLogService.writeLogCreate(MODULE.PRODUCT, ACTION.PRO_ORD_C, MasterObject.OrderDetail, "Thêm mới item vào đơn hàng", orderDetail.toString());
            logger.info("{}: Thêm mới item vào đơn hàng {}", OrderServiceImpl.class.getName(), orderDetail.toString());
            return orderDetailSaved;
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }

    @Override
    public OrderDetail update(OrderDetail orderDetail, Integer orderDetailId) {
        try {
            Optional<OrderDetail> orderDetailOptional = this.findById(orderDetailId);
            if (orderDetailOptional.isEmpty()) {
                throw new BadRequestException();
            }
            OrderDetail orderItemBefore = ObjectUtils.clone(orderDetailOptional.get());

            orderDetail.setId(orderDetailId);
            OrderDetail orderItemUpdated = orderDetailRepo.save(orderDetail);

            String logTitle = "Cập nhật đơn hàng " + orderItemUpdated.getOrder().getCode();
            ChangeLog changeLog = new ChangeLog(orderItemBefore, orderItemUpdated);
            orderHistoryService.save(changeLog.getLogChanges(), logTitle, orderItemBefore.getOrder().getId(), orderItemBefore.getId());
            systemLogService.writeLogUpdate(MODULE.PRODUCT, ACTION.PRO_ORD_U, MasterObject.OrderDetail, logTitle, changeLog);
            logger.info("{}: Cập nhật item of đơn hàng {}", OrderServiceImpl.class.getName(), orderItemUpdated.toString());

            return orderItemUpdated;
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }

    @Override
    public String delete(Integer orderDetailId) {
        Optional<OrderDetail> orderDetail = this.findById(orderDetailId);
        if (orderDetail.isEmpty()) {
            throw new BadRequestException();
        }
        try {
            orderDetailRepo.deleteById(orderDetailId);
            systemLogService.writeLogDelete(MODULE.PRODUCT, ACTION.PRO_ORD_D, MasterObject.OrderDetail, "Xóa item of đơn hàng", orderDetail.toString());
            logger.info("{}: Xóa item of đơn hàng {}", OrderServiceImpl.class.getName(), orderDetail.toString());
            return MessageCode.DELETE_SUCCESS.getDescription();
        } catch (RuntimeException ex) {
            throw new AppException(ex);
        }
    }
}