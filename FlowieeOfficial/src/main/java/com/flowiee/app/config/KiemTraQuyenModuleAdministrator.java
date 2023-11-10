package com.flowiee.app.config;

import com.flowiee.app.common.utils.FlowieeUtil;
import com.flowiee.app.service.system.AccountService;
import com.flowiee.app.service.system.RoleService;
import com.flowiee.app.common.module.SystemModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KiemTraQuyenModuleAdministrator {
    @Autowired
    private RoleService roleService;
    @Autowired
    private AccountService accountService;

    private final String module = SystemModule.HE_THONG.name();


    public boolean kiemTraQuyenConfig() {
        if (FlowieeUtil.ACCOUNT_USERNAME.equals(FlowieeUtil.ADMINISTRATOR)) {
            return true;
        }
        final String action = "CONFIG";
        int accountId = accountService.findIdByUsername(FlowieeUtil.ACCOUNT_USERNAME);
        if (roleService.isAuthorized(accountId, module, action)) {
            return true;
        }
        return false;
    }
}