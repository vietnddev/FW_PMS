package com.flowiee.pms.repository.sales;

import com.flowiee.pms.entity.sales.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("select distinct o from Order o " +
           "left join Customer c on c.id = o.customer.id " +
           "left join Category sc on sc.id = o.kenhBanHang.id and sc.type = 'SALES_CHANNEL' " +
           "left join Category os on os.id = o.trangThaiDonHang.id and os.type = 'ORDER_STATUS' " +
           "left join Category pm on pm.id = o.paymentMethod.id and pm.type = 'PAYMENT_METHOD' " +
           "left join FileStorage f on f.order.id = o.id " +
           "left join Account a on a.id = o.createdBy " +
           "where 1=1 " +
           "and (:txtSearch is null or (o.receiverName like %:txtSearch%)) " +
           "and (:orderId is null or o.id=:orderId) " +
           "and (:paymentMethodId is null or o.paymentMethod.id=:paymentMethodId) " +
           "and (:orderStatusId is null or o.trangThaiDonHang.id=:orderStatusId) " +
           "and (:salesChannelId is null or o.kenhBanHang.id=:salesChannelId) " +
           "and (:sellerId is null or o.nhanVienBanHang.id=:sellerId) " +
           "and (:customerId is null or o.customer.id=:customerId) " +
           "and (:branchId is null or a.branch.id = :branchId) " +
           "and (:groupCustomerId is null or 1=1) " +
           "and ((trunc(o.orderTime) >= trunc(:orderTimeFrom)) and (trunc(o.orderTime) <= trunc(:orderTimeTo)))")
    Page<Order> findAll(@Param("txtSearch") String txtSearch,
                        @Param("orderId") Integer orderId,
                        @Param("paymentMethodId") Integer paymentMethodId,
                        @Param("orderStatusId") Integer orderStatusId,
                        @Param("salesChannelId") Integer salesChannelId,
                        @Param("sellerId") Integer sellerId,
                        @Param("customerId") Integer customerId,
                        @Param("branchId") Integer branchId,
                        @Param("groupCustomerId") Integer groupCustomerId,
                        @Param("orderTimeFrom") LocalDateTime orderTimeFrom,
                        @Param("orderTimeTo") LocalDateTime orderTimeTo,
                        Pageable pageable);

    @Query(value = "select sum((select sum(d.price * d.quantity) from order_detail d where d.order_id = o.id) - o.amount_discount) from orders o where trunc(o.order_time) = trunc(current_date)", nativeQuery = true)
    Double findRevenueToday();

//    @Query("select nvl(sum(o.totalAmountDiscount), 0) from Order o where extract(month from o.thoiGianDatHang) = extract(month from current_date)")
//    Double findRevenueThisMonth();

    @Query("from Order o where trunc(o.orderTime) = trunc(current_date)")
    List<Order> findOrdersToday();

    @Modifying
    @Query("update Order set paymentTime=:paymentTime, paymentMethod.id=:paymentMethod, paymentAmount=:paymentAmount, paymentNote=:paymentNote, paymentStatus = true where id=:orderId")
    void updatePaymentStatus(@Param("orderId") Integer orderId, @Param("paymentTime") LocalDateTime paymentTime, @Param("paymentMethod") Integer paymentMethod, @Param("paymentAmount") Float paymentAmount, @Param("paymentNote") String paymentNote);

//    @Query("select " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 1 then o.totalAmountDiscount else 0 end) as JAN, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 2 then o.totalAmountDiscount else 0 end) as FEB, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 3 then o.totalAmountDiscount else 0 end) as MAR, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 4 then o.totalAmountDiscount else 0 end) as APRIL, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 5 then o.totalAmountDiscount else 0 end) as MAY, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 6 then o.totalAmountDiscount else 0 end) as JUN, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 7 then o.totalAmountDiscount else 0 end) as JUL, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 8 then o.totalAmountDiscount else 0 end) as AUG, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 9 then o.totalAmountDiscount else 0 end) as SEP, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 10 then o.totalAmountDiscount else 0 end) as OCT, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 11 then o.totalAmountDiscount else 0 end) as NOV, " +
//           "sum(case when extract(month from o.thoiGianDatHang) = 12 then o.totalAmountDiscount else 0 end) as DEC " +
//           "from Order o " +
//           "where extract(year from o.thoiGianDatHang) = extract(year from current_date)")
//    List<Object[]> findRevenueEachMonthOfYear();

    @Modifying
    @Query("update Order set ticketExport.id=:ticketExportId where id=:orderId")
    void setTicketExportInfo(@Param("orderId") Integer orderId, @Param("ticketExportId") Integer ticketExportId);

    @Query("from Order where ticketExport.id=:ticketExportId")
    Order findByTicketExport(@Param("ticketExportId") Integer ticketExportId);

    @Query("select " +
           "    extract(year from o.orderTime) AS year, " +
           "    extract(month from o.orderTime) AS month, " +
           "    count(o.id) AS order_count, " +
           "    nvl(sum(od.price - o.amountDiscount), 0) AS avgValue " +
           "from Order o " +
           "left join OrderDetail od on od.order.id = o.id " +
           "where 1=1 " +
           "    and o.customer.id=:customerId " +
           "    and extract(year from o.orderTime) = :year " +
           "    and (:month is null or (extract(month from o.orderTime) = :month)) " +
           "group by extract(year from o.orderTime), extract(month from o.orderTime) " +
           "order by extract(year from o.orderTime), extract(month from o.orderTime)")
    List<Object[]> findPurchaseHistory(@Param("customerId") Integer customerId, @Param("year") Integer year, @Param("month") Integer month);
}