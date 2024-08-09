package com.flowiee.pms.controller.sales;

import com.flowiee.pms.controller.BaseController;
import com.flowiee.pms.model.AppResponse;
import com.flowiee.pms.model.dto.OrderDTO;
import com.flowiee.pms.model.dto.TicketExportDTO;
import com.flowiee.pms.entity.sales.TicketExport;
import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.service.sales.TicketExportService;
import com.flowiee.pms.utils.constants.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app.api.prefix}/stg/ticket-export")
@Tag(name = "Ticket export API", description = "Quản lý xuất hàng")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TicketExportController extends BaseController {
    TicketExportService ticketExportService;

    @Operation(summary = "Find all tickets")
    @GetMapping("/all")
    @PreAuthorize("@vldModuleSales.exportGoods(true)")
    public AppResponse<List<TicketExport>> findAll(@RequestParam("pageSize") int pageSize,
                                                   @RequestParam("pageNum") int pageNum,
                                                   @RequestParam(value = "storageId", required = false) Integer storageId) {
        Page<TicketExport> ticketExports = ticketExportService.findAll(pageSize, pageNum - 1, storageId);
        return success(ticketExports.getContent(), pageNum, pageSize, ticketExports.getTotalPages(), ticketExports.getTotalElements());
    }

    @Operation(summary = "Find detail")
    @GetMapping("/{id}")
    @PreAuthorize("@vldModuleSales.exportGoods(true)")
    public AppResponse<TicketExportDTO> findDetail(@PathVariable("id") Integer ticketExportId) {
        Optional<TicketExport> ticketExport = ticketExportService.findById(ticketExportId);
        if (ticketExport.isEmpty()) {
            throw new BadRequestException();
        }
        return success(TicketExportDTO.fromTicketExport(ticketExport.get()));
    }

    @Operation(summary = "Create new ticket")
    @PostMapping("/create-draft")
    @PreAuthorize("@vldModuleSales.exportGoods(true)")
    public AppResponse<TicketExport> createDraftTicket(@RequestBody(required = false) OrderDTO order) {
        return success(ticketExportService.save(order));
    }

    @Operation(summary = "Update ticket")
    @PutMapping("/update/{id}")
    @PreAuthorize("@vldModuleSales.exportGoods(true)")
    public AppResponse<TicketExportDTO> updateTicketExport(@RequestBody TicketExport ticketExport, @PathVariable("id") Integer ticketExportId) {
        if (ObjectUtils.isEmpty(ticketExportService.findById(ticketExportId))) {
            throw new BadRequestException();
        }
        return success(TicketExportDTO.fromTicketExport(ticketExportService.update(ticketExport, ticketExportId)));
    }

    @Operation(summary = "Delete ticket")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@vldModuleSales.exportGoods(true)")
    public AppResponse<String> deleteTicketExport(@PathVariable("id") Integer ticketExportId) {
        return success(ticketExportService.delete(ticketExportId));
    }
}