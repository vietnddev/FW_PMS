package com.flowiee.sms.service.impl;

import com.flowiee.sms.entity.*;
import com.flowiee.sms.core.exception.AppException;
import com.flowiee.sms.core.exception.BadRequestException;
import com.flowiee.sms.core.exception.DataInUseException;
import com.flowiee.sms.model.MODULE;
import com.flowiee.sms.repository.*;
import com.flowiee.sms.service.*;
import com.flowiee.sms.utils.*;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	private static final String mvModule = MODULE.CATEGORY.name();
	
    private final CategoryRepository categoryRepo;
    private final CategoryHistoryRepository categoryHistoryRepo;
    private final ProductService productService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final ImportService importService;
    private final AppImportRepository appImportRepo;
    private final FileStorageService fileStorageService;
    private final FileStorageRepository fileStorageRepo;
    private final AccountService accountService;
    private final MaterialService materialService;
    private final ProductDetailRepository productDetailRepo;


    @Autowired
    public CategoryServiceImpl(ImportService importService, CategoryRepository categoryRepo, ProductService productService,
                               MaterialService materialService, OrderService orderService, NotificationService notificationService,
                               AppImportRepository appImportRepo, FileStorageService fileStorageService, FileStorageRepository fileStorageRepo,
                               AccountService accountService, CategoryHistoryRepository categoryHistoryRepo, ProductDetailRepository productDetailRepo) {
        this.importService = importService;
        this.categoryRepo = categoryRepo;
        this.productService = productService;
        this.materialService = materialService;
        this.orderService = orderService;
        this.notificationService = notificationService;
        this.appImportRepo = appImportRepo;
        this.fileStorageService = fileStorageService;
        this.fileStorageRepo = fileStorageRepo;
        this.accountService = accountService;
        this.categoryHistoryRepo = categoryHistoryRepo;
        this.productDetailRepo = productDetailRepo;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    @Override
    public Category findById(Integer entityId) {
        return categoryRepo.findById(entityId).orElse(null);
    }

    @Override
    public Category save(Category entity) {
        if (entity == null) {
            throw new BadRequestException();
        }
        return categoryRepo.save(entity);
    }

    @Transactional
    @Override
    public Category update(Category entity, Integer entityId) {
        Category categoryBefore = this.findById(entityId);
        if (categoryBefore == null) {
            throw new BadRequestException();
        }
        categoryBefore.compareTo(entity).forEach((key, value) -> {
            CategoryHistory categoryHistory = new CategoryHistory();
            categoryHistory.setTitle("Cập nhật danh mục " + categoryBefore.getType());
            categoryHistory.setCategory(new Category(categoryBefore.getId(), null));
            categoryHistory.setField(key);
            categoryHistory.setOldValue(value.substring(0, value.indexOf("#")));
            categoryHistory.setNewValue(value.substring(value.indexOf("#") + 1));
            categoryHistoryRepo.save(categoryHistory);
        });
        entity.setId(entityId);
        return categoryRepo.save(entity);
    }

    @Transactional
    @Override
    public String delete(Integer entityId) {
        if (entityId == null || entityId <= 0 || this.findById(entityId) == null) {
            throw new BadRequestException();
        }
        if (categoryInUse(entityId)) {
            throw new DataInUseException(MessageUtils.ERROR_DATA_LOCKED);
        }
        categoryHistoryRepo.deleteAllByCategory(entityId);
        categoryRepo.deleteById(entityId);
        return MessageUtils.DELETE_SUCCESS;
    }

    @Override
    public List<Category> findRootCategory() {
        List<Category> roots = categoryRepo.findRootCategory();
        List<Object[]> recordsOfEachType = categoryRepo.totalRecordsOfEachType();
        for (Category c : roots) {
            for (Object[] o : recordsOfEachType) {
                if (c.getType().equals(o[0])) {
                    c.setTotalSubRecords(Integer.parseInt(String.valueOf(o[1])));
                    break;
                }
            }
        }
        return roots;
    }

    @Override
    public List<Category> findSubCategory(String categoryType, Integer parentId) {
        return categoryRepo.findSubCategory(categoryType, parentId, Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Category> findSubCategory(String categoryType, Integer parentId, int pageSize, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        return categoryRepo.findSubCategory(categoryType, parentId, pageable);
    }

    @Override
    public Boolean categoryInUse(Integer categoryId) {
        Category category = this.findById(categoryId);
        switch (category.getType()) {
            case "UNIT":
                if (!productService.findProductsByType(categoryId).isEmpty() || !materialService.findByUnit(categoryId).isEmpty()) {
                    return true;
                }
                break;
            case "FABRIC_TYPE":
                if (!productDetailRepo.findAll(null, null, null, null, categoryId).isEmpty()) {
                    return true;
                }
                break;
            case "PAYMENT_METHOD":
                if (!orderService.findOrdersByPaymentMethodId(categoryId).isEmpty()) {
                    return true;
                }
                break;
            case "SALES_CHANNEL":
                if (!orderService.findOrdersBySalesChannelId(categoryId).isEmpty()) {
                    return true;
                }
                break;
            case "SIZE":
                if (!productDetailRepo.findAll(null, null, null, categoryId, null).isEmpty()) {
                    return true;
                }
                break;
            case "COLOR":
                if (!productDetailRepo.findAll(null, null, categoryId, null, null).isEmpty()) {
                    return true;
                }
                break;
            case "PRODUCT_TYPE":
                if (!productService.findProductsByType(categoryId).isEmpty()) {
                    return true;
                }
                break;
            case "ORDER_STATUS":
                if (!orderService.findOrdersByStatus(categoryId).isEmpty()) {
                    return true;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + category.getType());
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
                        row.getCell(1).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                        continue;
                    }

                    Category category = new Category();
                    category.setType(categoryType);
                    category.setCode(!categoryCode.isEmpty() ? categoryCode : CommonUtils.genCategoryCodeByName(categoryName));
                    category.setName(categoryName);
                    category.setNote(categoryNote);

                    if (!"OK".equals(this.save(category))) {
                        isImportSuccess = false;
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        XSSFFont fontStyle = workbook.createFont();
                        row.getCell(1).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(2).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                        row.getCell(3).setCellStyle(CommonUtils.highlightDataImportEror(cellStyle, fontStyle));
                    } else {
                        importSuccess++;
                    }
                    totalRecord++;
                }
            }
            workbook.close();

            if (isImportSuccess) {
                //resultOfFlowieeImport = MessagesUtil.IMPORT_DM_DONVITINH_SUCCESS;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            } else {
                //resultOfFlowieeImport = MessagesUtil.IMPORT_DM_DONVITINH_FAIL;
                detailOfFlowieeImport = importSuccess + " / " + totalRecord;
            }
            //Save file attach to storage
            FileStorage fileStorage = new FileStorage(fileImport, MODULE.CATEGORY.name());
            fileStorage.setNote("IMPORT");
            fileStorage.setStatus(false);
            fileStorage.setActive(false);
            fileStorage.setAccount(accountService.findCurrentAccount());
            fileStorageService.saveFileOfImport(fileImport, fileStorage);

            //Save import
            FlowieeImport flowieeImport = new FlowieeImport();
            flowieeImport.setModule(mvModule);
            flowieeImport.setEntity(Category.class.getName());
            flowieeImport.setAccount(accountService.findCurrentAccount());
            flowieeImport.setStartTime(startTimeImport);
            flowieeImport.setEndTime(new Date());
            flowieeImport.setResult(resultOfFlowieeImport);
            flowieeImport.setDetail(detailOfFlowieeImport);
            flowieeImport.setSuccessRecord(importSuccess);
            flowieeImport.setTotalRecord(totalRecord);
            flowieeImport.setFileId(fileStorageRepo.findByCreatedTime(fileStorage.getCreatedAt()).getId());
            importService.save(flowieeImport);

            Notification notification = new Notification();
            notification.setTitle(resultOfFlowieeImport);
            notification.setSend(0);
            notification.setReceive(CommonUtils.getUserPrincipal().getId());
            //notification.setType(MessagesUtil.NOTI_TYPE_IMPORT);
            notification.setContent(resultOfFlowieeImport);
            notification.setReaded(false);
            notification.setImportId(appImportRepo.findByStartTime(flowieeImport.getStartTime()).getId());
            notificationService.save(notification);

            return MessageUtils.CREATE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException();
        }
	}

	@Override
	public byte[] exportTemplate(String categoryType) {
		return CommonUtils.exportTemplate(AppConstants.TEMPLATE_IE_DM_CATEGORY);
	}

	@Override
	public byte[] exportData(String categoryType) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String filePathOriginal = CommonUtils.excelTemplatePath + "/" + AppConstants.TEMPLATE_IE_DM_CATEGORY + ".xlsx";
        String filePathTemp = CommonUtils.excelTemplatePath + "/" + AppConstants.TEMPLATE_IE_DM_CATEGORY + "_" + Instant.now(Clock.systemUTC()).toEpochMilli() + ".xlsx";
        File fileDeleteAfterExport = new File(Path.of(filePathTemp).toUri());
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(Files.copy(Path.of(filePathOriginal), Path.of(filePathTemp), StandardCopyOption.REPLACE_EXISTING).toFile());
            XSSFSheet sheet = workbook.getSheetAt(0);
            List<Category> listData = this.findAll();
            for (int i = 0; i < listData.size(); i++) {
                XSSFRow row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(listData.get(i).getCode());
                row.createCell(2).setCellValue(listData.get(i).getName());
                row.createCell(3).setCellValue(listData.get(i).getNote());
                for (int j = 0; j <= 3; j++) {
                    row.getCell(j).setCellStyle(CommonUtils.setBorder(workbook.createCellStyle()));
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