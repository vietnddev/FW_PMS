package com.flowiee.app.common.config;

import com.flowiee.app.nguoidung.entity.AccountEntity;
import com.flowiee.app.log.entity.SystemLog;
import com.flowiee.app.nguoidung.service.AccountService;
import com.flowiee.app.log.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountDetailService implements UserDetailsService {
	@Autowired
	AccountService accountService;

	@Autowired
	SystemLogService systemLogService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AccountEntity accountEntity = accountService.getAccountByUsername(username);
		UserDetails userDetails = null; // Đây là class hỗ trợ sẵn của Spring Security

		if (accountEntity != null) {
			// Thiết lập role
			List<GrantedAuthority> grantlist = new ArrayList<GrantedAuthority>();
			GrantedAuthority authority = new SimpleGrantedAuthority("USER");
			grantlist.add(authority);

			userDetails = new org.springframework.security.core.userdetails.User(accountEntity.getUsername(),
					accountEntity.getPassword(), grantlist);

			WebAuthenticationDetails details = null;
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null) {
				Object authDetails = authentication.getDetails();
				if (authDetails instanceof WebAuthenticationDetails) {
					details = (WebAuthenticationDetails) authDetails;
				}
			}

			systemLogService.writeLog(new SystemLog("Hệ thống", accountEntity.getUsername(), "Đăng nhập", details != null ? details.getRemoteAddress() : "unknown"));

			System.out.println("Login thành công");

		} else {
			System.out.println("Login thất bại");
		}
		return userDetails;
	}
}