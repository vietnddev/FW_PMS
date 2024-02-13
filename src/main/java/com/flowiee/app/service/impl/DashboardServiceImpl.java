package com.flowiee.app.service.impl;

import com.flowiee.app.entity.Customer;
import com.flowiee.app.entity.Order;
import com.flowiee.app.model.*;
import com.flowiee.app.service.CustomerService;
import com.flowiee.app.service.DashboardService;

import com.flowiee.app.service.OrderService;
import com.flowiee.app.service.ProductService;
import com.flowiee.app.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ProductService productService;

    @Override
    @SuppressWarnings("unchecked")
    public DashboardModel loadDashboard() {
        logger.info("Start loadDashboard(): " + CommonUtils.now("YYYY/MM/dd HH:mm:ss"));

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonth().getValue();

        //Revenue today
//      String revenueTodaySQL = "SELECT NVL(SUM(d.TOTAL_AMOUNT_AFTER_DISCOUNT), 0) FROM PRO_ORDER d WHERE TRUNC(d.THOI_GIAN_DAT_HANG) = TRUNC(SYSDATE)";
//      logger.info("[getRevenueToday() - SQL findData]: " + revenueTodaySQL);
//      Query revenueTodayQuery = entityManager.createNativeQuery(revenueTodaySQL);
//      String revenueToday = CommonUtil.formatToVND(Float.parseFloat(String.valueOf(revenueTodayQuery.getSingleResult())));
//      entityManager.close();

        //Revenue this month
//      String revenueThisMonthSQL = "SELECT NVL(SUM(d.TOTAL_AMOUNT_AFTER_DISCOUNT), 0) FROM PRO_ORDER d WHERE EXTRACT(MONTH FROM d.THOI_GIAN_DAT_HANG) = EXTRACT(MONTH FROM SYSDATE)";
//      logger.info("[getRevenueThisMonth() - SQL findData]: " + revenueThisMonthSQL);
//      Query revenueThisMonthSQLQuery = entityManager.createNativeQuery(revenueThisMonthSQL);
//      String revenueThisMonth = CommonUtil.formatToVND(Float.parseFloat(String.valueOf(revenueThisMonthSQLQuery.getSingleResult())));
//      entityManager.close();

        //Customers new
//      String customersNewSQL = "SELECT * FROM PRO_CUSTOMER c WHERE EXTRACT(MONTH FROM c.CREATED_AT) = EXTRACT(MONTH FROM SYSDATE)";
//      logger.info("[getCustomersNew() - SQL findData]: " + customersNewSQL);
//      Query customersNewQuery = entityManager.createNativeQuery(customersNewSQL);
//      List<Customer> customersNew = customersNewQuery.getResultList();
//      entityManager.close();

        //Orders today
//      String ordersTodaySQL = "SELECT * FROM PRO_ORDER D WHERE TRUNC(D.THOI_GIAN_DAT_HANG) = TRUNC(SYSDATE)";
//      logger.info("[getOrdersToday() - SQL findData]: " + ordersTodaySQL);
//      Query ordersTodayQuery = entityManager.createNativeQuery(ordersTodaySQL);
//      List<Order> ordersToday = ordersTodayQuery.getResultList();
//      entityManager.close();

        //Products top sell
        String productsTopSellSQL = "SELECT * FROM " +
                                    "(SELECT s.VARIANT_NAME, NVL(SUM(d.QUANTITY), 0) AS Total " +
                                    "FROM PRO_PRODUCT_VARIANT s " +
                                    "LEFT JOIN PRO_ORDER_DETAIL d ON s.id = d.PRODUCT_VARIANT_ID " +
                                    "GROUP BY s.ID, s.VARIANT_NAME " +
                                    "ORDER BY total DESC) " +
                                    "WHERE ROWNUM <= 10";
        logger.info("[getProductsTopSell() - SQL findData]: ");
        Query productsTopSellSQLQuery = entityManager.createNativeQuery(productsTopSellSQL);
        List<Object[]> productsTopSellResultList = productsTopSellSQLQuery.getResultList();
        LinkedHashMap<String, Integer> productsTopSell = new LinkedHashMap<>();
        for (Object[] data : productsTopSellResultList) {
            productsTopSell.put(String.valueOf(data[0]), Integer.parseInt(String.valueOf(data[1])));
        }
        entityManager.close();

        //Revenue month of year
        String revenueMonthOfYearSQL = "SELECT " +
                                       "    TO_CHAR(MONTHS.MONTH, 'MM') AS MONTH, " +
                                       "    NVL(SUM(d.PRICE * d.QUANTITY - NVL(o.AMOUNT_DISCOUNT, 0)), 0) AS REVENUE " +
                                       "FROM " +
                                       "    (SELECT TO_DATE('01-' || LEVEL || '-2024', 'DD-MM-YYYY') AS MONTH " +
                                       "     FROM DUAL " +
                                       "     CONNECT BY LEVEL <= 12) MONTHS " +
                                       "LEFT JOIN " +
                                       "    PRO_ORDER o ON TO_CHAR(o.ORDER_TIME, 'MM') = TO_CHAR(MONTHS.MONTH, 'MM') " +
                                       "LEFT JOIN " +
                                       "    PRO_ORDER_DETAIL d ON o.ID = d.ORDER_ID " +
                                       "WHERE " +
                                       "    EXTRACT(YEAR FROM MONTHS.MONTH) = ? " +
                                       "GROUP BY" +
                                       "    TO_CHAR(MONTHS.MONTH, 'MM') " +
                                       "ORDER BY " +
                                       "    TO_CHAR(MONTHS.MONTH, 'MM')";
        logger.info("[getRevenueMonthOfYearSQL() - SQL findData]: ");
        Query revenueMonthOfYearSQLQuery = entityManager.createNativeQuery(revenueMonthOfYearSQL);
        revenueMonthOfYearSQLQuery.setParameter(1, currentYear);
        List<Object[]> revenueMonthOfYearSQLResultList = revenueMonthOfYearSQLQuery.getResultList();
        LinkedHashMap<Integer, Float> revenueMonthOfYear = new LinkedHashMap<>();
        for (int i = 0; i < revenueMonthOfYearSQLResultList.size(); i++) {
            revenueMonthOfYear.put(Integer.parseInt(String.valueOf(revenueMonthOfYearSQLResultList.get(i)[0])), Float.parseFloat(String.valueOf(revenueMonthOfYearSQLResultList.get(i)[1] != null ? revenueMonthOfYearSQLResultList.get(i)[1] : 0)));
        }
        entityManager.close();

        //Revenue day of month
        String revenueDaysOfMonthSQL = "WITH MONTH_DAYS AS ( " +
                                       "    SELECT TO_DATE(TO_CHAR(TO_DATE('01-2024', 'MM-YYYY') + LEVEL - 1, 'DD-MM-YYYY'), 'DD-MM-YYYY') AS MONTH_DAY " +
                                       "    FROM DUAL " +
                                       "    CONNECT BY LEVEL <= EXTRACT(DAY FROM LAST_DAY(TO_DATE('2024-01-01', 'YYYY-MM-DD'))) " +
                                       ") " +
                                       "SELECT " +
                                       "    TO_CHAR(MD.MONTH_DAY, 'DD') AS DAY, " +
                                       "    NVL(SUM(d.PRICE * d.QUANTITY - o.AMOUNT_DISCOUNT), 0) AS REVENUE " +
                                       "FROM " +
                                       "    MONTH_DAYS MD " +
                                       "LEFT JOIN " +
                                       "    PRO_ORDER o ON TRUNC(o.ORDER_TIME) = MD.MONTH_DAY " +
                                       "LEFT JOIN " +
                                       "    PRO_ORDER_DETAIL d ON o.ID = d.ORDER_ID " +
                                       "WHERE " +
                                       "    EXTRACT(MONTH FROM MD.MONTH_DAY) = ? " + //-- Thay 1 bằng tháng bạn quan tâm
                                       "    AND EXTRACT(YEAR FROM MD.MONTH_DAY) = ? " + //-- Thay 2024 bằng năm bạn quan tâm
                                       "GROUP BY " +
                                       "  TO_CHAR(MD.MONTH_DAY, 'DD') " +
                                       "ORDER BY " +
                                       "  TO_CHAR(MD.MONTH_DAY, 'DD')";
        logger.info("[getRevenueDayOfMonth() - SQL findData]: ");
        Query revenueDayOfMonthSQLQuery = entityManager.createNativeQuery(revenueDaysOfMonthSQL);
        revenueDayOfMonthSQLQuery.setParameter(1, currentMonth);
        revenueDayOfMonthSQLQuery.setParameter(2, currentYear);
        List<Object[]> revenueDayOfMonthSQLResultList = revenueDayOfMonthSQLQuery.getResultList();
        LinkedHashMap<String, Float> revenueDayOfMonth = new LinkedHashMap<>();
        for (int i = 0; i < revenueDayOfMonthSQLResultList.size(); i++) {
            revenueDayOfMonth.put("Day " + (i + 1), Float.parseFloat(String.valueOf(revenueDayOfMonthSQLResultList.get(i)[1])));
        }
        entityManager.close();

        //Revenue by sales channel
        String revenueBySalesChannelSQL = "SELECT " +
                                          "c.NAME, " +
                                          "c.COLOR, " +
                                          "NVL(SUM(d.PRICE * d.QUANTITY - o.AMOUNT_DISCOUNT), 0) AS TOTAL " +
                                          "FROM (SELECT * FROM CATEGORY WHERE TYPE = 'SALES_CHANNEL') c " +
                                          "LEFT JOIN PRO_ORDER o ON c.ID = o.CHANNEL " +
                                          "LEFT JOIN PRO_ORDER_DETAIL d ON o.ID = d.ORDER_ID " +
                                          "GROUP BY c.NAME, c.COLOR";
        logger.info("[getRevenueBySalesChannel() - SQL findData]: ");
        Query revenueBySalesChannelQuery = entityManager.createNativeQuery(revenueBySalesChannelSQL);
        List<Object[]> revenueBySalesChannelResultList = revenueBySalesChannelQuery.getResultList();
        LinkedHashMap<String, Float> revenueSalesChannel = new LinkedHashMap<>();
        for (Object[] data : revenueBySalesChannelResultList) {
            revenueSalesChannel.put(String.valueOf(data[0]), Float.parseFloat(String.valueOf(data[2])));
        }
        entityManager.close();

        String revenueToday = CommonUtils.formatToVND(orderService.findRevenueToday());
        String revenueThisMonth = CommonUtils.formatToVND(orderService.findRevenueThisMonth());
        List<Customer> customersNew = customerService.findCustomerNewInMonth();
        List<Order> ordersToday = orderService.findOrdersToday();

        DashboardModel dashboard = new DashboardModel();
        dashboard.setTotalProducts(productService.totalProductsInStorage());
        dashboard.setRevenueToday(revenueToday);
        dashboard.setRevenueThisMonth(revenueThisMonth);
        dashboard.setOrdersTodayQty(ordersToday.size());
        dashboard.setListOrdersToday(ordersToday);
        dashboard.setCustomersNewInMonthQty(customersNew.size());
        dashboard.setListCustomersNewInMonth(customersNew);
        dashboard.setRevenueDayOfMonth(revenueDayOfMonth);
        dashboard.setRevenueMonthOfYear(revenueMonthOfYear);
        dashboard.setRevenueSalesChannel(revenueSalesChannel);
        dashboard.setProductsTopSell(productsTopSell);

        logger.info("Finished loadDashboard(): " + CommonUtils.now("YYYY/MM/dd HH:mm:ss"));
        return dashboard;
    }
}