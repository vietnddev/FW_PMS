package com.flowiee.pms.utils;

import com.flowiee.pms.exception.AuthenticationException;
import com.flowiee.pms.utils.constants.MODULE;
import com.flowiee.pms.model.ServerInfo;
import com.flowiee.pms.model.ShopInfo;
import com.flowiee.pms.model.UserPrincipal;
import com.flowiee.pms.utils.constants.CategoryType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.*;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

public class CommonUtils {
    public static LocalDateTime START_APP_TIME = null;
    public static Map<String, String> mvEndPointHeaderConfig = new HashMap<>();
    public static Map<String, String> mvEndPointSideBarConfig = new HashMap<>();
    public static ShopInfo mvShopInfo;
    public static ServerInfo mvServerInfo;

    public static String formatToVND(Object currency) {
        @SuppressWarnings("deprecation")
		NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setCurrency(Currency.getInstance("VND"));
        return currency != null ? currencyFormat.format(currency) : "0 VND";
    }

    public static String genCategoryCodeByName(String categoryName) {
        String normalized = Normalizer.normalize(categoryName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
        String strTemp = pattern.matcher(normalized).replaceAll("").replaceAll(" ", "").toUpperCase();

        StringBuilder result = new StringBuilder();
        for (char c : strTemp.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                result.append(c);
            }
        }

        return result.toString();
    }

    public static String now(String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime now = LocalDateTime.now();
        return dateTimeFormatter.format(now);
    }

    public static String getCategoryType(String key) {
        Map<String, String> map = new HashMap<>();
        for (CategoryType c : CategoryType.values()) {
            map.put(c.getKey(), c.getName());
        }
        return map.get(key);
    }

    public static String getPathDirectory(MODULE systemModule) {
        try {
            StringBuilder path = new StringBuilder(FileUtils.fileUploadPath);
            switch (systemModule) {
                case PRODUCT:
                    path.append("product");
                    break;
                case CATEGORY:
                    path.append("category");
                    break;
                case STORAGE:
                    path.append("storage");
                    break;
                default:
                	path.append("system");
                	break;
            }
            path.append("/" + LocalDateTime.now().getYear());
            path.append("/" + LocalDateTime.now().getMonth().getValue());
            path.append("/" + LocalDateTime.now().getDayOfMonth());
            File folder = new File(path.toString());
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    System.out.println("mkdirs OK");
                }
            }
            return path.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String getPathDirectory(String systemModule) {
        try {
            StringBuilder path = new StringBuilder(FileUtils.fileUploadPath);
            if (MODULE.PRODUCT.name().equals(systemModule)) {
                path.append("product");
            } else if (MODULE.CATEGORY.name().equals(systemModule)) {
                path.append("category");
            } else if (MODULE.STORAGE.name().equals(systemModule)) {
                path.append("storage");
            }
            path.append("/" + LocalDateTime.now().getYear());
            path.append("/" + LocalDateTime.now().getMonth().getValue());
            path.append("/" + LocalDateTime.now().getDayOfMonth());
            File folder = new File(path.toString());
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    System.out.println("mkdirs OK");
                }
            }
            return path.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static byte[] exportTemplate(String templateName) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String filePathOriginal = FileUtils.excelTemplatePath + "/" + templateName + ".xlsx";
        String filePathTemp = FileUtils.excelTemplatePath + "/" + templateName + "_" + Instant.now(Clock.systemUTC()).toEpochMilli() + ".xlsx";
        File fileDeleteAfterExport = null;
        try {
            fileDeleteAfterExport = new File(Path.of(filePathTemp).toUri());
            XSSFWorkbook workbook = new XSSFWorkbook(Files.copy(Path.of(filePathOriginal), Path.of(filePathTemp), StandardCopyOption.REPLACE_EXISTING).toFile());
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

    public static XSSFCellStyle highlightDataImportError(XSSFCellStyle cellStyle, XSSFFont fontStyle) {
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        fontStyle.setColor(IndexedColors.RED.getIndex());

        cellStyle.setFont(fontStyle);

        return cellStyle;
    }

    public static String encodePassword(String pwd) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.encode(pwd);
    }

    public static UserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new AuthenticationException();
    }

    public static String genProductCode() {
        return CommonUtils.now("yyyyMMddHHmmss");
    }

    public static String genOrderCode() {
        return CommonUtils.now("yyyyMMddHHmmss");
    }

    public static String trim(String str) {
        if (ObjectUtils.isEmpty(str)) {
            return "";
        }
        str = str.trim();
        if ("null".equals(str)) {
            str = null;
        }
        return str;
    }
}