package com.flowiee.pms.service.system;

import com.flowiee.pms.service.BaseCurdService;
import com.flowiee.pms.entity.system.FlowieeImport;

import java.util.List;

public interface ImportService extends BaseCurdService<FlowieeImport> {
    List<FlowieeImport> findAll();

    List<FlowieeImport> findByAccountId(Integer accountId);
}