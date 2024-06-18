package com.flowiee.pms.entity.sales;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.pms.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "customer_contact")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerContact extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "value", length = 500, nullable = false)
    private String value;

    @Column(name = "note")
    private String note;

    @Column(name = "is_default")
    private String isDefault;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "is_used")
    private boolean isUsed;

	@Override
	public String toString() {
		return "CustomerContact [id=" + super.id + ", customer=" + customer + ", code=" + code + ", value=" + value + ", note=" + note + ", isDefault=" + isDefault + ", status=" + status + "]";
	}    
}