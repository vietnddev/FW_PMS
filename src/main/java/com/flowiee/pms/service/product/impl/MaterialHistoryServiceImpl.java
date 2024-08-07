package com.flowiee.pms.service.product.impl;

import com.flowiee.pms.entity.product.Material;
import com.flowiee.pms.entity.product.MaterialHistory;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.product.MaterialHistoryRepository;
import com.flowiee.pms.service.product.MaterialHistoryService;

import com.flowiee.pms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MaterialHistoryServiceImpl implements MaterialHistoryService {
    MaterialHistoryRepository materialHistoryRepo;

    @Override
    public List<MaterialHistory> findAll() {
        return materialHistoryRepo.findAll();
    }

    @Override
    public Optional<MaterialHistory> findById(Integer entityId) {
        return materialHistoryRepo.findById(entityId);
    }

    @Override
    public MaterialHistory save(MaterialHistory entity) {
        if (entity == null) {
            throw new BadRequestException();
        }
        return materialHistoryRepo.save(entity);
    }

    @Override
    public MaterialHistory update(MaterialHistory entity, Integer entityId) {
        if (entity == null || entityId == null || entityId <= 0) {
            throw new BadRequestException();
        }
        entity.setId(entityId);
        return materialHistoryRepo.save(entity);
    }

    @Override
    public String delete(Integer entityId) {
        if (entityId == null || entityId <= 0) {
            throw new BadRequestException();
        }
        materialHistoryRepo.deleteById(entityId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }

    @Override
    public List<MaterialHistory> findByMaterialId(Integer materialId) {
        return materialHistoryRepo.findByMaterialId(materialId);
    }

    @Override
    public List<MaterialHistory> findByFieldName(String fieldName) {
        return materialHistoryRepo.findByFieldName(fieldName);
    }

    @Override
    public List<MaterialHistory> save(Map<String, Object[]> logChanges, String title, Integer materialId) {
        List<MaterialHistory> materialHistories = new ArrayList<>();
        for (Map.Entry<String, Object[]> entry : logChanges.entrySet()) {
            String field = entry.getKey();
            String oldValue = entry.getValue()[0].toString();
            String newValue = entry.getValue()[1].toString();
            MaterialHistory materialHistory = MaterialHistory.builder()
                    .title("Update material")
                    .material(new Material(materialId))
                    .fieldName(field)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            materialHistories.add(this.save(materialHistory));
        }
        return materialHistories;
    }
}