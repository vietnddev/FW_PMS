package com.flowiee.pms.service.system.impl;

import com.flowiee.pms.entity.system.FileImportHistory;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.system.AppImportRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.system.ImportService;

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
public class ImportServiceImpl extends BaseService implements ImportService {
    AppImportRepository appImportRepository;

    @Override
    public List<FileImportHistory> findAll() {
        return appImportRepository.findAll();
    }

    @Override
    public List<FileImportHistory> findByAccountId(Integer accountId) {
        return appImportRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<FileImportHistory> findById(Integer importId) {
        return appImportRepository.findById(importId);
    }

    @Override
    public FileImportHistory save(FileImportHistory fileImportHistory) {
        if (fileImportHistory == null) {
            throw new BadRequestException();
        }
        return appImportRepository.save(fileImportHistory);
    }

    @Override
    public FileImportHistory update(FileImportHistory entity, Integer entityId) {
        if (entity == null || entityId == null || entityId <= 0) {
            throw new BadRequestException();
        }
        entity.setId(entityId);
        return appImportRepository.save(entity);
    }

    @Override
    public String delete(Integer entityId) {
        Optional<FileImportHistory> fImport = this.findById(entityId);
        if (fImport.isEmpty()) {
            throw new BadRequestException();
        }
        appImportRepository.deleteById(entityId);
        return MessageCode.DELETE_SUCCESS.getDescription();
    }
}