package com.flowiee.pms.service.sales.impl;

import com.flowiee.pms.entity.sales.GarmentFactory;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.sales.GarmentFactoryRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.sales.GarmentFactoryService;

import com.flowiee.pms.utils.constants.MessageCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GarmentFactoryServiceImpl extends BaseService implements GarmentFactoryService {
    GarmentFactoryRepository garmentFactoryRepo;

    @Override
    public List<GarmentFactory> findAll() {
        return garmentFactoryRepo.findAll();
    }

    @Override
    public Optional<GarmentFactory> findById(Integer entityId) {
        return garmentFactoryRepo.findById(entityId);
    }

    @Override
    public GarmentFactory save(GarmentFactory entity) {
        if (entity == null) {
            throw new BadRequestException();
        }
        return garmentFactoryRepo.save(entity);
    }

    @Override
    public GarmentFactory update(GarmentFactory entity, Integer entityId) {
        if (this.findById(entityId).isEmpty()) {
            throw new BadRequestException();
        }
        entity.setId(entityId);
        return garmentFactoryRepo.save(entity);
    }

    @Override
    public String delete(Integer entityId) {
        if (this.findById(entityId).isEmpty()) {
            throw new BadRequestException();
        }
        garmentFactoryRepo.deleteById(entityId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }
}