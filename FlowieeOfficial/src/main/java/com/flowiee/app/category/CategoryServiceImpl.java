package com.flowiee.app.category;

import com.flowiee.app.category.entity.DonViTinh;
import com.flowiee.app.category.service.impl.DonViTinhServiceImpl;
import com.flowiee.app.common.module.SystemModule;
import com.flowiee.app.common.utils.CategoryUtil;
import com.flowiee.app.common.utils.ExcelUtil;
import com.flowiee.app.common.utils.FileUtil;
import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.utils.NotificationUtil;
import com.flowiee.app.common.utils.TagName;
import com.flowiee.app.entity.Document;
import com.flowiee.app.entity.FileStorage;
import com.flowiee.app.entity.FlowieeImport;
import com.flowiee.app.entity.Notification;
import com.flowiee.app.repository.storage.FileStorageRepository;
import com.flowiee.app.repository.system.FlowieeImportRepository;
import com.flowiee.app.service.product.ProductService;
import com.flowiee.app.service.storage.DocumentService;
import com.flowiee.app.service.storage.FileStorageService;
import com.flowiee.app.service.system.FlowieeImportService;
import com.flowiee.app.service.system.NotificationService;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
	private static final String MODULE = SystemModule.DANH_MUC.name();
	
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FlowieeImportService flowieeImportService;
    @Autowired
    private FlowieeImportRepository flowieeImportRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private FileStorageRepository fileStorageRepository;
    
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(Integer entityId) {
        return categoryRepository.findById(entityId).orElse(null);
    }

    @Override
    public String save(Category entity) {
        if (entity == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        categoryRepository.save(entity);
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Transactional
    @Override
    public String update(Category entity, Integer entityId) {
        if (entity == null || entityId == null || entityId <= 0) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        entity.setId(entityId);
        categoryRepository.save(entity);
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Transactional
    @Override
    public String delete(Integer entityId) {
        if (entityId == null || entityId <= 0 || this.findById(entityId) == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        categoryRepository.deleteById(entityId);
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public List<Category> findRootCategory() {
        return categoryRepository.findRootCategory();
    }

    @Override
    public List<Category> findSubCategory(String categoryType) {
        return categoryRepository.findSubCategory(categoryType);
    }

    private Boolean categoryInUse(Integer categoryId) {
        Category category = this.findById(categoryId);
        if (category.getType().equals(CategoryUtil.UNIT)) {

        } else if (category.getType().equals(CategoryUtil.FABRICTYPE)) {

        } else if (category.getType().equals(CategoryUtil.PAYMETHOD)) {

        } else if (category.getType().equals(CategoryUtil.SALESCHANNEL)) {

        } else if (category.getType().equals(CategoryUtil.SIZE)) {

        } else if (category.getType().equals(CategoryUtil.COLOR)) {

        } else if (category.getType().equals(CategoryUtil.PRODUCTTYPE)) {

        } else if (category.getType().equals(CategoryUtil.DOCUMENTTYPE)) {
            if (!documentService.findByDoctype(categoryId).isEmpty()) return true;
        } else if (category.getType().equals(CategoryUtil.ORDERSTATUS)) {

        }
        return false;
    }

    @Transactional
	@Override
	public String importData(MultipartFile fileImport, String categoryType) {
		Date startTimeImport = new Date();
        String resultOfFlowieeImport = "";
        String detailOfFlowieeImport = "";
        int importSuccess = 0;
        int totalRecord = 0;
        boolean isImportSuccess = true;
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(fileImport.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 3; i < sheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row != null) {
                    String categoryCode = row.getCell(1).getStringCellValue();
                    String categoryName = row.getCell(2).getStringCellValue();
                    String categoryNote = row.getCell(3).getStringCellValue();
                    //Nếu name null -> không ínsert data null vào database
                    if (categoryName == null || categoryName.length() == 0) {
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont fontStyle = workbook.createFont();
                        row.getCell(1).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                        continue;
                    }

                    Category category = new Category();
                    category.setType(categoryType);
                    category.setCode(!categoryCode.isEmpty() ? categoryCode : FlowieeUtil.getMaDanhMuc(categoryName));
                    category.setName(categoryName);
                    category.setNote(categoryNote);

                    if (!"OK".equals(this.save(category))) {
                        isImportSuccess = false;
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont fontStyle = workbook.createFont();
                        row.getCell(1).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(ExcelUtil.highlightDataImportEror(cellStyle, fontStyle));
                    } else {
                        importSuccess++;
                    }
                    totalRecord++;
                }
            }
            workbook.close();

            if (isImportSuccess) {
                resultOfFlowieeImport = NotificationUtil.IMPORT_DM_DONVITINH_SUCCESS;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            } else {
                resultOfFlowieeImport = NotificationUtil.IMPORT_DM_DONVITINH_FAIL;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            }
            //Save file attach to storage
            FileStorage fileStorage = new FileStorage(fileImport, SystemModule.DANH_MUC.name());
            fileStorage.setGhiChu("IMPORT");
            fileStorage.setStatus(false);
            fileStorage.setActive(false);
            fileStorageService.saveFileOfImport(fileImport, fileStorage);

            //Save import
            FlowieeImport flowieeImport = new FlowieeImport();
            flowieeImport.setModule(MODULE);
            flowieeImport.setEntity(DonViTinhServiceImpl.class.getName());
            flowieeImport.setAccount(FlowieeUtil.ACCOUNT);
            flowieeImport.setStartTime(startTimeImport);
            flowieeImport.setEndTime(new Date());
            flowieeImport.setResult(resultOfFlowieeImport);
            flowieeImport.setDetail(detailOfFlowieeImport);
            flowieeImport.setSuccessRecord(importSuccess);
            flowieeImport.setTotalRecord(totalRecord);
            flowieeImport.setFileId(fileStorageRepository.findByCreatedTime(fileStorage.getCreatedAt()).getId());
            flowieeImportService.save(flowieeImport);

            Notification notification = new Notification();
            notification.setTitle(resultOfFlowieeImport);
            notification.setSend(FlowieeUtil.SYS_NOTI_ID);
            notification.setReceive(FlowieeUtil.ACCOUNT_ID);
            notification.setType(NotificationUtil.NOTI_TYPE_IMPORT);
            notification.setContent(resultOfFlowieeImport);
            notification.setReaded(false);
            notification.setImportId(flowieeImportRepository.findByStartTime(flowieeImport.getStartTime()).getId());
            notificationService.save(notification);

            return TagName.SERVICE_RESPONSE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TagName.SERVICE_RESPONSE_FAIL;
	}

	@Override
	public byte[] exportTemplate(String categoryType) {
		return FileUtil.exportTemplate(FileUtil.TEMPLATE_IE_DM_CATEGORY);
	}

	@Override
	public byte[] exportData(String categoryType) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String filePathOriginal = FlowieeUtil.PATH_TEMPLATE_EXCEL + "/" + FileUtil.TEMPLATE_IE_DM_CATEGORY + ".xlsx";
        String filePathTemp = FlowieeUtil.PATH_TEMPLATE_EXCEL + "/" + FileUtil.TEMPLATE_IE_DM_CATEGORY + "_" + Instant.now(Clock.systemUTC()).toEpochMilli() + ".xlsx";
        File fileDeleteAfterExport = new File(Path.of(filePathTemp).toUri());
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(Files.copy(Path.of(filePathOriginal),
								                     Path.of(filePathTemp),
								                     StandardCopyOption.REPLACE_EXISTING).toFile());
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<Category> listData = this.findAll();
            for (int i = 0; i < listData.size(); i++) {
                XSSFRow row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(listData.get(i).getCode());
                row.createCell(2).setCellValue(listData.get(i).getName());
                row.createCell(3).setCellValue(listData.get(i).getNote());
                for (int j = 0; j <= 3; j++) {
                    row.getCell(j).setCellStyle(ExcelUtil.setBorder(workbook.createCellStyle()));
                }
            }
            workbook.write(stream);
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileDeleteAfterExport.exists()) {
                fileDeleteAfterExport.delete();
            }
        }
        return stream.toByteArray();
	}
}