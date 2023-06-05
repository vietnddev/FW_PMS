package com.flowiee.app.sanpham.controller;

import com.flowiee.app.common.authorization.KiemTraQuyenModuleSanPham;
import com.flowiee.app.common.utils.PagesUtil;
import com.flowiee.app.danhmuc.entity.HinhThucThanhToan;
import com.flowiee.app.danhmuc.repository.KenhBanHangRepository;
import com.flowiee.app.danhmuc.service.HinhThucThanhToanService;
import com.flowiee.app.danhmuc.service.KenhBanHangService;
import com.flowiee.app.danhmuc.service.TrangThaiDonHangService;
import com.flowiee.app.hethong.service.AccountService;
import com.flowiee.app.sanpham.entity.DonHang;
import com.flowiee.app.sanpham.entity.DonHangChiTiet;
import com.flowiee.app.sanpham.model.DonHangChiTietResponse;
import com.flowiee.app.sanpham.model.DonHangRequest;
import com.flowiee.app.sanpham.services.BienTheSanPhamService;
import com.flowiee.app.sanpham.services.ChiTietDonHangService;
import com.flowiee.app.sanpham.services.DonHangService;
import com.flowiee.app.sanpham.services.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/don-hang")
public class DonHangController {
    @Autowired
    private DonHangService donHangService;
    @Autowired
    private ChiTietDonHangService donHangChiTietService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private BienTheSanPhamService bienTheSanPhamService;
    @Autowired
    private KenhBanHangService kenhBanHangService;
    @Autowired
    private HinhThucThanhToanService hinhThucThanhToanService;
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private TrangThaiDonHangService trangThaiDonHangService;
    @Autowired
    private KiemTraQuyenModuleSanPham kiemTraQuyenModuleSanPham;

    @GetMapping
    public String findAllDonHang(ModelMap modelMap) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenXem()) {
            modelMap.addAttribute("listDonHang", donHangService.findAll());
            modelMap.addAttribute("listBienTheSanPham", bienTheSanPhamService.findAll());
            modelMap.addAttribute("listKenhBanHang", kenhBanHangService.findAll());
            modelMap.addAttribute("listHinhThucThanhToan", hinhThucThanhToanService.findAll());
            modelMap.addAttribute("listKhachHang", khachHangService.findAll());
            modelMap.addAttribute("listNhanVienBanHang", accountService.findAll());
            modelMap.addAttribute("listTrangThaiDonHang", trangThaiDonHangService.findAll());
            modelMap.addAttribute("donHangRequest", new DonHangRequest());
            modelMap.addAttribute("donHang", new DonHang());
            return PagesUtil.PAGE_DONHANG;
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @PostMapping
    public String FilterListDonHang(ModelMap modelMap, @ModelAttribute("donHangRequest") DonHangRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenXem()) {
            modelMap.addAttribute("listDonHang", donHangService.findAll(request.getSearchTxt(),
                                                                                   request.getThoiGianDatHangSearch(),
                                                                                   request.getKenhBanHang(),
                                                                                   request.getHinhThucThanhToan(),
                                                                                   request.getTrangThaiDonHang()));
            modelMap.addAttribute("listBienTheSanPham", bienTheSanPhamService.findAll());
            modelMap.addAttribute("listKenhBanHang", kenhBanHangService.findAll());
            modelMap.addAttribute("listHinhThucThanhToan", hinhThucThanhToanService.findAll());
            modelMap.addAttribute("listKhachHang", khachHangService.findAll());
            modelMap.addAttribute("listNhanVienBanHang", accountService.findAll());
            modelMap.addAttribute("listTrangThaiDonHang", trangThaiDonHangService.findAll());
            modelMap.addAttribute("donHangRequest", new DonHangRequest());
            modelMap.addAttribute("donHang", new DonHang());
            return PagesUtil.PAGE_DONHANG;
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @GetMapping("/{id}")
    public String findDonHangDetail(@PathVariable("id") int id, HttpServletRequest request, ModelMap modelMap) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenXem()) {
            List<DonHangChiTiet> listDonHangDetail = donHangChiTietService.findByDonHangId(id);
            modelMap.addAttribute("donHangDetail", donHangService.findById(id));
            modelMap.addAttribute("listDonHangDetail", donHangChiTietService.convertToDonHangChiTietResponse(listDonHangDetail));
            modelMap.addAttribute("donHang", new DonHang());
            return PagesUtil.PAGE_DONHANG_CHITIET;
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @Transactional
    @PostMapping("/insert")
    public String insert(@ModelAttribute("donHangRequest") DonHangRequest request) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenThemMoiDonHang()) {
            donHangService.save(request);
            return "redirect:/don-hang";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @PostMapping("/update/{id}")
    public String update(@ModelAttribute("donHang") DonHang donHang, @PathVariable("id") int id) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenCapNhatDonHang()) {
            donHangService.update(donHang, id);
            return "redirect:/don-hang";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        if (!accountService.isLogin()) {
            return PagesUtil.PAGE_LOGIN;
        }
        if (kiemTraQuyenModuleSanPham.kiemTraQuyenCapNhatDonHang()) {
            donHangService.delete(id);
            return "redirect:/don-hang";
        } else {
            return PagesUtil.PAGE_UNAUTHORIZED;
        }
    }
}