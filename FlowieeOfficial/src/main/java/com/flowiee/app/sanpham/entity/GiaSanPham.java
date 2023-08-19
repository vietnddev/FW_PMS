package com.flowiee.app.sanpham.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.app.common.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Builder
@Entity
@Table(name = "san_pham_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GiaSanPham extends BaseEntity implements Serializable {
    @JsonIgnoreProperties("listGiaBan")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bien_the_id", nullable = false)
    private BienTheSanPham bienTheSanPham;

    @Column(name = "gia_ban", nullable = false)
    private Double giaBan;

    @Column(name = "trang_thai", nullable = false)
    private boolean trangThai;

    @Override
    public String toString() {
        return "GiaSanPham{" +
                "id=" + id +
                ", bienTheSanPham=" + bienTheSanPham +
                ", giaBan=" + giaBan +
                ", trangThai=" + trangThai +
                '}';
    }
}