package com.flowiee.pms.model.dto;

import com.flowiee.pms.entity.product.MaterialTemp;
import com.flowiee.pms.entity.product.ProductVariantTemp;
import com.flowiee.pms.entity.sales.TicketImport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TicketImportDTO extends TicketImport implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer supplierId;
    private String supplierName;
    private Integer paymentMethodId;
    private String paymentMethodName;
    private Integer storageId;
    private String storageName;
    private List<ProductVariantTemp> listProductVariantTemp;
    private List<MaterialTemp> listMaterialTemp;

    public static TicketImportDTO fromTicketImport(TicketImport ticketImport) {
        TicketImportDTO dto = new TicketImportDTO();
        dto.setId(ticketImport.getId());
        dto.setTitle(ticketImport.getTitle());
        if (ticketImport.getSupplier() != null) {
            dto.setSupplierId(ticketImport.getSupplier().getId());
            dto.setSupplierName(ticketImport.getSupplier().getName());
        }
        if (ticketImport.getPaymentMethod() != null) {
            dto.setPaymentMethodId(ticketImport.getPaymentMethod().getId());
            dto.setPaymentMethodName(ticketImport.getPaymentMethod().getName());
        }
        dto.setImporter(ticketImport.getImporter());
        dto.setImportTime(ticketImport.getImportTime());
        dto.setNote(ticketImport.getNote());
        dto.setStatus(ticketImport.getStatus());
        //dto.setListProductDTO(ProductVariantDTO.fromProductVariants(ticketImport.getListProductDetails()));
        dto.setListProductVariantTemp(ticketImport.getListProductVariantTemps());
        //dto.setListMaterialDTO(MaterialDTO.fromMaterials(ticketImport.getListMaterials()));
        dto.setListMaterialTemp(ticketImport.getListMaterialTemps());
        return dto;
    }

    public static List<TicketImportDTO> fromTicketImports(List<TicketImport> ticketImports) {
        List<TicketImportDTO> list = new ArrayList<>();
        for (TicketImport t : ticketImports) {
            list.add(TicketImportDTO.fromTicketImport(t));
        }
        return list;
    }
}