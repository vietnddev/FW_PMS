package com.flowiee.pms.service.product;

import com.flowiee.pms.entity.system.FileStorage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductImageService {
    List<FileStorage> getImageOfProduct(Integer pProductId);

    List<FileStorage> getImageOfProductVariant(Integer pProductVariantId);

    FileStorage saveImageProduct(MultipartFile fileUpload, int pProductId) throws IOException;

    FileStorage saveImageProductVariant(MultipartFile fileUpload, int pProductId) throws IOException;

    FileStorage saveImageProductCombo(MultipartFile fileUpload, int productComboId) throws IOException;

    FileStorage saveImageTicketImport(MultipartFile fileUpload, int ticketImportId) throws IOException;

    FileStorage saveImageTicketExport(MultipartFile fileUpload, int ticketExportId) throws IOException;

    FileStorage setImageActiveOfProduct(Integer pProductId, Integer pImageId);

    FileStorage setImageActiveOfProductVariant(Integer pProductVariantId, Integer pImageId);

    FileStorage findImageActiveOfProduct(int pProductId);

    FileStorage findImageActiveOfProductVariant(int pProductVariantId);

    FileStorage changeImageProduct(MultipartFile fileToChange, int fileId);
}