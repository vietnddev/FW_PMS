package com.flowiee.app.danhmuc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.app.sanpham.entity.BienTheSanPham;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@Entity
@Table(name = "dm_loai_kich_co")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
@AllArgsConstructor
public class LoaiKichCo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "ma_loai", length = 50)
    private String maLoai;

    @Column(name = "ten_loai", nullable = false)
    private String tenLoai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai", nullable = false)
    private boolean trangThai;

    @OneToMany(mappedBy = "loaiKichCo", fetch = FetchType.LAZY)
    private List<BienTheSanPham> listBienTheSanPham;

    @Override
    public String toString() {
        return "LoaiKichCo{" +
            "id=" + id +
            ", maLoai='" + maLoai + '\'' +
            ", tenLoai='" + tenLoai + '\'' +
            ", ghiChu='" + ghiChu + '\'' +
            ", trangThai=" + trangThai +
            '}';
    }
}