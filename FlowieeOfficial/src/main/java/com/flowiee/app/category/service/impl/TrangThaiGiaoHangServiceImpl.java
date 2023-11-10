package com.flowiee.app.category.service.impl;

import com.flowiee.app.common.exception.NotFoundException;
import com.flowiee.app.common.utils.TagName;
import com.flowiee.app.service.product.OrderService;
import com.flowiee.app.category.entity.TrangThaiGiaoHang;
import com.flowiee.app.category.repository.TrangThaiGiaoHangRepository;
import com.flowiee.app.category.service.TrangThaiGiaoHangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TrangThaiGiaoHangServiceImpl implements TrangThaiGiaoHangService {
    @Autowired
    private TrangThaiGiaoHangRepository trangThaiGiaoHangRepository;
    @Autowired
    private OrderService orderService;

    @Override
    public List<TrangThaiGiaoHang> findAll() {
        return trangThaiGiaoHangRepository.findAll();
    }

    @Override
    public List<TrangThaiGiaoHang> findByDonHangId(int donHangId) {
        if (orderService.findById(donHangId) == null) {
            throw new NotFoundException();
        }
        return trangThaiGiaoHangRepository.findByDonHangId(orderService.findById(donHangId));
    }

    @Override
    public TrangThaiGiaoHang findById(int id) {
        return trangThaiGiaoHangRepository.findById(id).orElse(null);
    }

    @Override
    public String save(TrangThaiGiaoHang trangThaiGiaoHang) {
        try {
            trangThaiGiaoHangRepository.save(trangThaiGiaoHang);
            return TagName.SERVICE_RESPONSE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return TagName.SERVICE_RESPONSE_FAIL;
        }
    }

    @Override
    public String update(TrangThaiGiaoHang trangThaiGiaoHang, int id) {
        try {
            if (id <= 0 || this.findById(id) == null) {
                throw new NotFoundException();
            }
            trangThaiGiaoHangRepository.save(trangThaiGiaoHang);
            return TagName.SERVICE_RESPONSE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return TagName.SERVICE_RESPONSE_FAIL;
        }
    }

    @Override
    public String delete(int id) {
        try {
            if (id <= 0 || this.findById(id) == null) {
                throw new NotFoundException();
            }
            trangThaiGiaoHangRepository.deleteById(id);
            return TagName.SERVICE_RESPONSE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return TagName.SERVICE_RESPONSE_FAIL;
        }
    }

    @Override
    public String importData(MultipartFile fileImport) {
        return null;
    }

    @Override
    public byte[] exportData() {
        return new byte[0];
    }
}
