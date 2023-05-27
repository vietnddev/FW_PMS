package com.flowiee.app.danhmuc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.app.khotailieu.entity.DocField;
import com.flowiee.app.khotailieu.entity.Document;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Entity
@Table(name = "dm_loai_tai_lieu")
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LoaiTaiLieu implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai", nullable = false)
    private boolean trangThai;

    @JsonIgnoreProperties("loaiTaiLieu")
    @OneToMany(mappedBy = "loaiTaiLieu", fetch = FetchType.LAZY)
    private List<Document> listDocument;

    @JsonIgnoreProperties("loaiTaiLieu")
    @OneToMany(mappedBy = "loaiTaiLieu", fetch = FetchType.LAZY)
    private List<DocField> listDocField;

    @Override
    public String toString() {
        return "LoaiTaiLieu{" +
            "id=" + id +
            ", ten='" + ten + '\'' +
            ", moTa='" + moTa + '\'' +
            ", trangThai=" + trangThai +
            '}';
    }
}