package com.flowiee.pms.controller.sales;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.exception.ResourceNotFoundException;
import com.flowiee.pms.model.AppResponse;
import com.flowiee.pms.model.EximModel;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.service.ExportService;
import com.flowiee.pms.service.sales.OrderService;
import com.flowiee.pms.utils.constants.ErrorCode;
import com.flowiee.pms.utils.constants.TemplateExport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app.api.prefix}/order")
@Tag(name = "Order API", description = "Quản lý đơn hàng")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController extends BaseController {
     OrderService orderService;
     ExportService exportService;

    public OrderController(OrderService orderService, @Qualifier("orderExportServiceImpl") ExportService exportService) {
        this.orderService = orderService;
        this.exportService = exportService;
    }

    @Operation(summary = "Find all orders")
    @GetMapping("/all")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public AppResponse<List<OrderDTO>> findAllOrders(@RequestParam(value = "orderId", required = false) Integer pOrderId,
                                                     @RequestParam(value = "paymentMethodId", required = false) Integer pPaymentMethodId,
                                                     @RequestParam(value = "orderStatusId", required = false) Integer pOrderStatusId,
                                                     @RequestParam(value = "salesChannelId", required = false) Integer pSalesChannelId,
                                                     @RequestParam(value = "groupCustomerId", required = false) Integer pGroupCustomerId,
                                                     @RequestParam(value = "sellerId", required = false) Integer pSellerId,
                                                     @RequestParam(value = "customerId", required = false) Integer pCustomerId,
                                                     @RequestParam(value = "branchId", required = false) Integer pBranchId,
                                                     @RequestParam(value = "txtSearch", required = false) String pTxtSearch,
                                                     @RequestParam("pageSize") int pageSize,
                                                     @RequestParam("pageNum") int pageNum) {
        try {
            Page<OrderDTO> orderPage = orderService.findAll(pageSize, pageNum - 1, pTxtSearch, pOrderId, pPaymentMethodId, pOrderStatusId, pSalesChannelId, pSellerId, pCustomerId, pBranchId, pGroupCustomerId, null, null, null);
            return success(orderPage.getContent(), pageNum, pageSize, orderPage.getTotalPages(), orderPage.getTotalElements());
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "order"), ex);
        }
    }

    @Operation(summary = "Find detail order")
    @GetMapping("/{orderId}")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public AppResponse<OrderDTO> findOrderDetail(@PathVariable("orderId") Integer orderId) {
        Optional<OrderDTO> orderDTO = orderService.findById(orderId);
        if (orderDTO.isEmpty()) {
            throw new ResourceNotFoundException("Order not found!");
        }
        return success(orderDTO.get());
    }

    @Operation(summary = "Create new order")
    @PostMapping("/insert")
    public AppResponse<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            return success(orderService.save(orderDTO));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.CREATE_ERROR_OCCURRED.getDescription(), "order"), ex);
        }
    }

    @Operation(summary = "Update order")
    @PutMapping("/update/{orderId}")
    @PreAuthorize("@vldModuleSales.updateOrder(true)")
    public AppResponse<OrderDTO> update(@RequestBody OrderDTO order, @PathVariable("orderId") Integer orderId) {
        return success(orderService.update(order, orderId));
    }

    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("@vldModuleSales.deleteOrder(true)")
    public AppResponse<String> deleteOrder(@PathVariable("orderId") Integer orderId) {
        //Check them trang thai
        return success(orderService.delete(orderId));
    }

    @PutMapping("/do-pay/{orderId}")
    @PreAuthorize("@vldModuleSales.updateOrder(true)")
    public AppResponse<String> doPayOrder(@PathVariable("orderId") Integer orderId,
                                          @RequestParam(value = "paymentTime", required = false) String paymentTime,
                                          @RequestParam("paymentMethod") Integer paymentMethod,
                                          @RequestParam("paymentAmount") Float paymentAmount,
                                          @RequestParam(value = "paymentNote", required = false) String paymentNote) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
        return success(orderService.doPay(orderId, LocalDateTime.parse(paymentTime, formatter), paymentMethod, paymentAmount, paymentNote));
    }

    @GetMapping("/export")
    @PreAuthorize("@vldModuleSales.readOrder(true)")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        EximModel model = exportService.exportToExcel(TemplateExport.EX_LIST_OF_ORDERS, null, false);
        return ResponseEntity.ok().headers(model.getHttpHeaders()).body(model.getContent());
    }

    @GetMapping("/scan/QR-Code/{code}")
    public ModelAndView findOrderInfoByQRCode(@PathVariable("code") String code) {
        try {
            //Xử lý code thành id
            return new ModelAndView().addObject("orderInfo", orderService.findById(null));
        } catch (RuntimeException ex) {
            throw new AppException(String.format(ErrorCode.SEARCH_ERROR_OCCURRED.getDescription(), "scan order"), ex);
        }
    }
}