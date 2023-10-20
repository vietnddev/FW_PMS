package com.flowiee.app.sanpham.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flowiee.app.common.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
@Table(name = "garment_factory")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GarmentFactory extends BaseEntity implements Serializable {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "note")
    private String note;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "bienTheSanPham", fetch = FetchType.LAZY)
    private List<SanPhamBienTheImport> listProductVariantImport;
}