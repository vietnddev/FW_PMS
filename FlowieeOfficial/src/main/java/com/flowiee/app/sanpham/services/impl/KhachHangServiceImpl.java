package com.flowiee.app.sanpham.services.impl;

import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.common.action.KhachHangAction;
import com.flowiee.app.common.module.SystemModule;
import com.flowiee.app.common.utils.TagName;
import com.flowiee.app.hethong.service.AccountService;
import com.flowiee.app.hethong.service.SystemLogService;
import com.flowiee.app.sanpham.entity.KhachHang;
import com.flowiee.app.sanpham.repository.KhachHangRepository;
import com.flowiee.app.sanpham.services.KhachHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    private static final Logger logger = LoggerFactory.getLogger(KhachHangServiceImpl.class);
    private static final String module = SystemModule.SAN_PHAM.name();

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SystemLogService systemLogService;

    @Override
    public List<KhachHang> findAll() {
        return khachHangRepository.findAll();
    }

    @Override
    public KhachHang findById(Integer id) {
        return khachHangRepository.findById(id).orElse(null);
    }

    @Override
    public String save(KhachHang khachHang) {
        if (khachHang == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        khachHang.setCreatedBy(FlowieeUtil.ACCOUNT_ID + "");
        khachHangRepository.save(khachHang);
        systemLogService.writeLog(module, KhachHangAction.CREATE_KHACHHANG.name(), "Thêm mới khách hàng: " + khachHang.toString());
        logger.info(SanPhamServiceImpl.class.getName() + ": Thêm mới khách hàng " + khachHang.toString());
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String update(KhachHang khachHang, Integer id) {
        if (khachHang == null || id <= 0 || this.findById(id) == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        khachHang.setId(id);
        khachHangRepository.save(khachHang);
        systemLogService.writeLog(module, KhachHangAction.UPDATE_KHACHHANG.name(), "Cập nhật thông tin khách hàng: " + khachHang.toString());
        logger.info(SanPhamServiceImpl.class.getName() + ": Cập nhật khách hàng " + khachHang.toString());
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String delete(Integer id) {
        KhachHang khachHang = this.findById(id);
        if (id <= 0 || khachHang == null) {
            return TagName.SERVICE_RESPONSE_FAIL;
        }
        khachHangRepository.deleteById(id);
        systemLogService.writeLog(module, KhachHangAction.DELETE_KHACHHANG.name(), "Xóa khách hàng: " + khachHang.toString());
        logger.info(SanPhamServiceImpl.class.getName() + ": Xóa khách hàng " + khachHang.toString());
        return TagName.SERVICE_RESPONSE_SUCCESS;
    }
}