package com.flowiee.app.controller;

import com.flowiee.app.model.Account;
import com.flowiee.app.services.AccountService;
import com.flowiee.app.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/profile")
public class InformationController {
	@Autowired
	private AccountService accountService;

	@Autowired
	private FileService filessService;

	BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

	@GetMapping("")
	public String showInformation(@AuthenticationPrincipal UserDetails userDetails, ModelMap modelMap) {
		// Lấy thông tin của user đã đăng nhập
		Account account = accountService.getAccountByUsername(userDetails.getUsername());
		if (account != null) {
			modelMap.addAttribute("information", account);
		}
		
		modelMap.addAttribute("account", new Account());

		return "pages/profile/information";
	}

	@PostMapping("/update")
	public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute("account") Account account) {
		String username = userDetails.getUsername();
		String password = accountService.getAccountByUsername(username).getPassword();
		int accountID = accountService.getAccountByUsername(username).getID();

		account.setID(accountID);
		account.setUsername(username);
		account.setPassword(password);
		account.setStatus(true);
		accountService.saveAccount(account);

		return "redirect:/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute("account") Account account, ModelMap modelMap) {
		String password_old = request.getParameter("password_old");
		String password_new = request.getParameter("password_new");
		String password_renew = request.getParameter("password_renew");

		if (bCrypt.matches(password_old,
				accountService.getAccountByUsername(userDetails.getUsername()).getPassword())) {
			if (password_new.equals(password_renew)) {
				account.setID(accountService.getAccountByUsername(userDetails.getUsername()).getID());
				account.setUsername(userDetails.getUsername());
				account.setName(accountService.getAccountByUsername(userDetails.getUsername()).getName());
				account.setPassword(bCrypt.encode(password_new));
				account.setStatus(true);
				accountService.saveAccount(account);
				
				modelMap.addAttribute("successMessage", "OK rồi nhé");
			}		 			
			modelMap.addAttribute("successMessage", "OK rồi nhée");
		}  
		modelMap.addAttribute("successMessage", "OK rồi nhée");
		
		return "redirect:/profile";
		//return "pages/profile/information";
	}  
}   