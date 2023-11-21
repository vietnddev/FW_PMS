package com.flowiee.app.controller;

import com.flowiee.app.base.BaseController;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.exception.NotFoundException;
import com.flowiee.app.service.ProductService;
import com.flowiee.app.service.ProductVariantService;
import com.flowiee.app.service.FileStorageService;
import com.flowiee.app.service.AccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FileStorageController extends BaseController {
    @Autowired
    private FileStorageService fileService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductVariantService productVariantService;

    @PostMapping("/uploads/san-pham/{id}")
    public String uploadImageOfSanPham(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                       @PathVariable("id") Integer productId) throws Exception {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (productId <= 0 || productService.findById(productId) == null) {
            throw new NotFoundException("Product not found!");
        }
        if (file.isEmpty()) {
            throw new NotFoundException("File attach not found!");
        }
        fileService.saveImageSanPham(file, productId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/uploads/bien-the-san-pham/{id}")
    public String uploadImageOfSanPhamBienThe(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                                              @PathVariable("id") Integer productVariantId) throws Exception {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (productVariantId <= 0 || productVariantService.findById(productVariantId) == null) {
            throw new NotFoundException("Product variant not found!");
        }
        if (file.isEmpty()) {
            throw new NotFoundException("File attach not found!");
        }
        fileService.saveImageBienTheSanPham(file, productVariantId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/file/change-image-sanpham/{id}")
    public String changeFile(@RequestParam("file") MultipartFile file,
                             @PathVariable("id") Integer fileId,
                             HttpServletRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (fileId <= 0 || fileService.findById(fileId) == null) {
            throw new NotFoundException("Image not found");
        }
        if (file.isEmpty()) {
            throw new NotFoundException("File attach not found!");
        }
        fileService.changeImageSanPham(file, fileId);
        return "redirect:" + request.getHeader("referer");
    }

    @PostMapping("/file/delete/{id}")
    public String delete(HttpServletRequest request, @PathVariable("id") Integer fileId) {
        if (!accountService.isLogin()) {
            return PagesUtil.SYS_LOGIN;
        }
        if (fileId <= 0 || fileService.findById(fileId) == null) {
            throw new NotFoundException("Image not found!");
        }
        fileService.delete(fileId);
        return "redirect:" + request.getHeader("referer");
    }
}