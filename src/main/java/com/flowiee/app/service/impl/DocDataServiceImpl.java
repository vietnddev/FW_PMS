package com.flowiee.app.service.impl;

import com.flowiee.app.entity.DocData;
import com.flowiee.app.repository.DocDataRepository;
import com.flowiee.app.service.DocDataService;

import com.flowiee.app.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocDataServiceImpl implements DocDataService {
    @Autowired
    private DocDataRepository docDataRepository;

    public List<DocData> findAll() {
        return docDataRepository.findAll();
    }

    public DocData findById(Integer id) {
        return docDataRepository.findById(id).orElse(null);
    }

    public DocData save(DocData docData) {
        return docDataRepository.save(docData);
    }

    @Override
    public DocData update(DocData entity, Integer entityId) {
        entity.setId(entityId);
        return docDataRepository.save(entity);
    }

    public String delete(Integer id) {
        docDataRepository.deleteById(id);
        return AppConstants.SERVICE_RESPONSE_SUCCESS;
    }

    @Override
    public List<DocData> findByDocField(Integer docFieldId) {
        return docDataRepository.findByDocField(docFieldId);
    }

    @Override
    public DocData findByFieldIdAndDocId(Integer docFieldId, Integer documentId) {
        return docDataRepository.findByFieldIdAndDocId(docFieldId, documentId);
    }
}