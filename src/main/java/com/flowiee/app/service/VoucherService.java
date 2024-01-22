package com.flowiee.app.service;

import com.flowiee.app.entity.VoucherInfo;
import com.flowiee.app.dto.VoucherInfoDTO;

import java.util.Date;
import java.util.List;

public interface VoucherService {
    List<VoucherInfoDTO> findAll(String status, Date startTime, Date endTime, String title);

    VoucherInfoDTO findById(Integer voucherId);

    List<VoucherInfoDTO> findByIds(List<Integer> voucherIds, String status);

    VoucherInfo save(VoucherInfo voucherInfo, List<Integer> listSanPhamApDung);

    VoucherInfo update(VoucherInfo voucherInfo, Integer voucherId);

    String detele(Integer voucherId);

    VoucherInfoDTO isAvailable(String voucherTicketCode);
}