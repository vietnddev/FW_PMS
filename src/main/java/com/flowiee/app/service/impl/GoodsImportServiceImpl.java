package com.flowiee.app.service.impl;

import com.flowiee.app.entity.Category;
import com.flowiee.app.model.request.TicketImportGoodsRequest;
import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.FlowieeUtil;
import com.flowiee.app.entity.Account;
import com.flowiee.app.entity.TicketImportGoods;
import com.flowiee.app.entity.Supplier;
import com.flowiee.app.repository.GoodsImportRepository;
import com.flowiee.app.service.GoodsImportService;

import com.flowiee.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GoodsImportServiceImpl implements GoodsImportService {
    @Autowired
    private GoodsImportRepository goodsImportRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EntityManager entityManager;

    private static String STATUS_DRAFT = "DRAFT";
//    private static String STATUS_APPROVING = "APPROVING";
//    private static String STATUS_APPROVED = "APPROVED";
//    private static String STATUS_REJECT = "REJECT";

    @Override
    public List<TicketImportGoods> findAll() {
        return goodsImportRepository.findAll();
    }

    @Override
    public TicketImportGoods findById(Integer entityId) {
        return goodsImportRepository.findById(entityId).get();
    }

    @Override
    public String save(TicketImportGoods entity) {
        if (entity == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        goodsImportRepository.save(entity);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String update(TicketImportGoods entity, Integer entityId) {
        if (entity == null || entityId == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        if (entityId <= 0 || this.findById(entityId) == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        entity.setId(entityId);
        goodsImportRepository.save(entity);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public String delete(Integer entityId) {
        if (entityId == null || entityId <= 0) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        TicketImportGoods ticketImportGoods = this.findById(entityId);
        if (ticketImportGoods == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        goodsImportRepository.deleteById(entityId);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }
;

    @Override
    public String saveDraft(TicketImportGoodsRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        TicketImportGoods ticketImportGoods = this.findById(request.getId());
        if (ticketImportGoods == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        ticketImportGoods.setId(request.getId());
        ticketImportGoods.setTitle(request.getTitle());
        if (request.getSupplierId() != null && request.getSupplierId() > 0) {
            ticketImportGoods.setSupplier(new Supplier(request.getSupplierId(), null));
        }
        if (request.getPaymentMethodId() != null && request.getPaymentMethodId() > 0) {
            ticketImportGoods.setPaymentMethod(new Category(request.getPaymentMethodId(), null));
        }
        if (request.getReceivedBy() != null && request.getReceivedBy() > 0) {
            ticketImportGoods.setReceivedBy(new Account(request.getReceivedBy()));
        }
        ticketImportGoods.setDiscount(request.getDiscount());
        ticketImportGoods.setPaidAmount(request.getPaidAmount());
        ticketImportGoods.setPaidStatus(request.getPaidStatus());
        if (request.getOrderTime() != null) {
            ticketImportGoods.setOrderTime(request.getOrderTime());
        }
        if (request.getReceivedTime() != null) {
            ticketImportGoods.setReceivedTime(request.getReceivedTime());
        }
        ticketImportGoods.setNote(request.getNote());
        ticketImportGoods.setStatus(STATUS_DRAFT);
        goodsImportRepository.save(ticketImportGoods);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public List<TicketImportGoods> search(String text, Integer supplierId, Integer paymentMethod, String payStatus, String importStatus) {
        List<TicketImportGoods> listData = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.ID, i.TITLE, s.NAME as SUPPLIER_NAME, pm.TEN_LOAI as PAYMENT_METHOD_NAME, i.PAY_STATUS, a.HO_TEN as RECEIVED_NAME, i.STATUS, ");
        sql.append("       s.ID as SUPPLIER_ID, pm.ID as PAYMENT_METHOD_ID, a.ID as RECEIVED_ID, ");
        sql.append("       i.ORDER_TIME, i.RECEIVED_TIME ");
        sql.append("FROM goods_import i ");
        sql.append("LEFT JOIN supplier s ON s.ID = i.SUPPLIER_ID ");
        sql.append("LEFT JOIN dm_hinh_thuc_thanh_toan pm ON pm.ID = i.PAYMENT_METHOD ");
        sql.append("LEFT JOIN account a ON a.ID = i.RECEIVED_BY ");
        sql.append("WHERE i.TITLE LIKE '%").append(text != null ? text : "").append("%' ");
        if (supplierId != null) {
            sql.append("AND s.ID = ? ");
        }
        if (paymentMethod != null) {
            sql.append("AND pm.ID = ? ");
        }
        if (payStatus != null) {
            sql.append("i.PAY_STATUS = ? ");
        }
        if (importStatus != null) {
            sql.append("i.STATUS = ?");
        }
        System.out.println(sql.toString());
        Query query = entityManager.createNativeQuery(sql.toString());
        int i = 1;
        if (supplierId != null) {
            query.setParameter(i++, supplierId);
        }
        if (paymentMethod != null) {
            query.setParameter(i++, paymentMethod);
        }
        if (payStatus != null) {
            query.setParameter(i++, payStatus);
        }
        if (importStatus != null) {
            query.setParameter(i++, importStatus);
        }
        @SuppressWarnings("unchecked")
		List<Object[]> data = query.getResultList();
        for (Object[] o : data) {
            TicketImportGoods ticketImportGoods = new TicketImportGoods();
            ticketImportGoods.setId(Integer.parseInt(o[0].toString()));
            ticketImportGoods.setTitle(String.valueOf(o[1]));
            ticketImportGoods.setSupplier(new Supplier(Integer.parseInt(String.valueOf(o[7])), String.valueOf(o[2])));
            ticketImportGoods.setPaymentMethod(new Category(Integer.parseInt(String.valueOf(o[8])), String.valueOf(o[3])));
            ticketImportGoods.setPaidStatus(String.valueOf(o[4]));
            ticketImportGoods.setReceivedBy(new Account(Integer.parseInt(String.valueOf(o[9])), null, String.valueOf(o[5])));
            ticketImportGoods.setStatus(String.valueOf(o[6]));
            ticketImportGoods.setOrderTime(FlowieeUtil.convertStringToDate(String.valueOf(o[10])));
            ticketImportGoods.setReceivedTime(FlowieeUtil.convertStringToDate(String.valueOf(o[11])));
            listData.add(ticketImportGoods);
        }
        entityManager.close();
        return listData;
    }

    @Override
    public List<TicketImportGoods> findByMaterialId(Integer materialId) {
        List<TicketImportGoods> listData = new ArrayList<>();
        if (materialId != null && materialId >= 0) {
            //listData = goodsImportRepository.findByMaterialId(materialId);
        }
        return listData;
    }

    @Override
    public List<TicketImportGoods> findBySupplierId(Integer supplierId) {
        List<TicketImportGoods> listData = new ArrayList<>();
        if (supplierId != null && supplierId >= 0) {
            listData = goodsImportRepository.findBySupplierId(supplierId);
        }
        return listData;
    }

    @Override
    public List<TicketImportGoods> findByPaymentMethod(String paymentMethod) {
        List<TicketImportGoods> listData = new ArrayList<>();
        if (paymentMethod != null) {
            listData = goodsImportRepository.findByPaymentMethod(paymentMethod);
        }
        return listData;
    }

    @Override
    public List<TicketImportGoods> findByPaidStatus(String paidStatus) {
        List<TicketImportGoods> listData = new ArrayList<>();
        if (paidStatus != null && FlowieeUtil.getPaymentStatusCategory().containsKey(paidStatus)) {
            listData = goodsImportRepository.findByPaidStatus(paidStatus);
        }
        return listData;
    }

    @Override
    public List<TicketImportGoods> findByAccountId(Integer accountId) {
        List<TicketImportGoods> listData = new ArrayList<>();
        if (accountId != null && accountId > 0) {
            listData = goodsImportRepository.findByReceiveBy(accountId);
        }
        return listData;
    }

    @Override
    public TicketImportGoods findDraftImportPresent(Integer createdBy) {
        return goodsImportRepository.findDraftGoodsImportPresent(STATUS_DRAFT, createdBy);
    }

    @Override
    public TicketImportGoods createDraftImport() {
        TicketImportGoods ticketImportGoods = new TicketImportGoods();
        ticketImportGoods.setTitle("Title");
        ticketImportGoods.setStatus(STATUS_DRAFT);
        ticketImportGoods.setCreatedBy(accountService.findCurrentAccountId());
        ticketImportGoods.setOrderTime(new Date());
        ticketImportGoods.setReceivedTime(new Date());
        ticketImportGoods = goodsImportRepository.save(ticketImportGoods);
        return ticketImportGoods;
    }

    @Override
    public String updateStatus(Integer entityId, String status) {
        if (entityId == null || entityId <= 0) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        TicketImportGoods ticketImportGoods = this.findById(entityId);
        if (ticketImportGoods == null) {
            return AppConstants.SERVICE_RESPONSE_FAIL;
        }
        ticketImportGoods.setStatus(status);
        goodsImportRepository.save(ticketImportGoods);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }
}