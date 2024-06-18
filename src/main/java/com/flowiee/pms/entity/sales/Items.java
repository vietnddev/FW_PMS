package com.flowiee.pms.entity.sales;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.entity.BaseEntity;
import com.flowiee.pms.entity.product.ProductDetail;
import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "order_cart_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Items extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private OrderCart orderCart;

    @Column(name = "price_type", nullable = false)
    private String priceType;//S or L

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "price_original", nullable = false)
    private BigDecimal priceOriginal;

    @Column(name = "extra_discount")
    private BigDecimal extraDiscount;

	@Override
	public String toString() {
		return "Items [id=" + super.id + ", quantity=" + quantity + ", orderCart=" + orderCart + "]";
	}
}