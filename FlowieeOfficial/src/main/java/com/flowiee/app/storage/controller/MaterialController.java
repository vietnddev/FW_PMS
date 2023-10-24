package com.flowiee.app.storage.controller;

import com.flowiee.app.common.authorization.KiemTraQuyenModuleKhoTaiLieu;
import com.flowiee.app.common.exception.BadRequestException;
import com.flowiee.app.common.utils.EndPointUtil;
import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.danhmuc.service.DonViTinhService;
import com.flowiee.app.hethong.service.AccountService;
import com.flowiee.app.hethong.service.NotificationService;
import com.flowiee.app.storage.entity.Material;
import com.flowiee.app.sanpham.services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@Controller
@RequestMapping("/storage/material")
public class MaterialController {
    @Autowired
    private MaterialService materialService;
    @Autowired
    private DonViTinhService donViTinhService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private KiemTraQuyenModuleKhoTaiLieu kiemTraQuyenModuleKho;

    @GetMapping("")
    public ModelAndView loadPage() {
        if (!accountService.isLogin()) {
            return new ModelAndView(PagesUtil.PAGE_LOGIN);
        }
        if (kiemTraQuyenModuleKho.kiemTraQuyenQuanLyMaterial()) {
            ModelAndView modelAndView = new ModelAndView(PagesUtil.PAGE_STORAGE_MATERIAL);
            modelAndView.addObject("material", new Material());
            modelAndView.addObject("listMaterial", materialService.findAll());
            modelAndView.addObject("listDonViTinh", donViTinhService.findAll());
            modelAndView.addObject("listNotification", notificationService.findAllByReceiveId(FlowieeUtil.ACCOUNT_ID));
            modelAndView.addObject("templateImportName", "Name");
            modelAndView.addObject("url_template", EndPointUtil.STORAGE_MATERIAL_TEMPLATE);
            modelAndView.addObject("url_import", EndPointUtil.STORAGE_MATERIAL_IMPORT);
            modelAndView.addObject("url_export", EndPointUtil.STORAGE_MATERIAL_EXPORT);
            return modelAndView;
        } else {
            return new ModelAndView(PagesUtil.PAGE_UNAUTHORIZED);
        }
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute("material") Material material) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        material.setStatus(true);
        materialService.save(material);
        return "redirect:";
    }

    @PostMapping("/update/{id}")
    public String update(@ModelAttribute("material") Material material,
                         @PathVariable("id") Integer materialId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (materialId <= 0) {
            throw new BadRequestException();
        }
        materialService.update(material, materialId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer materialId, HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        materialService.delete(materialId);
        return "redirect:" + request.getHeader("referer");
    }

    @GetMapping("/template")
    public ResponseEntity<?> exportTemplate() {
        return null;
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile file) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
//        if (kiemTraQuyenModule.kiemTraQuyenExport()) {
//            donViTinhService.importData(file);
//            return "redirect:" + EndPointUtil.DANHMUC_DONVITINH_VIEW;
//        } else {
//            return PagesUtil.PAGE_UNAUTHORIZED;
//        }
        return null;
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportData() {
        if (!accountService.isLogin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_LOGIN);
        }
//        if (kiemTraQuyenModule.kiemTraQuyenExport()) {
//            byte[] dataExport = donViTinhService.exportData();
//            HttpHeaders header = new HttpHeaders();
//            header.setContentType(new MediaType("application", "force-download"));
//            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FileUtil.TEMPLATE_IE_DM_LOAIDONVITINH + ".xlsx");
//            return new ResponseEntity<>(new ByteArrayResource(dataExport), header, HttpStatus.CREATED);
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PagesUtil.PAGE_UNAUTHORIZED);
//        }
        return null;
    }
}