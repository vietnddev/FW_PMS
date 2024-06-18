package com.flowiee.pms.service.system.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import com.flowiee.pms.exception.AppException;
import com.flowiee.pms.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flowiee.pms.entity.system.Language;
import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.repository.system.LanguagesRepository;
import com.flowiee.pms.service.system.LanguageService;

@Service
public class LanguageServiceImpl extends BaseService implements LanguageService {
	private final LanguagesRepository languagesRepo;

	@Autowired
	public LanguageServiceImpl(LanguagesRepository languagesRepo) {
		this.languagesRepo = languagesRepo;
	}

	@Override
	public List<Language> findAll() {
		return languagesRepo.findAll();
	}

	@Override
	public Optional<Language> findById(Integer langId) {
		return languagesRepo.findById(langId);
	}

	@Override
	public Language save(Language entity) {
		return null;
	}

	@Override
	public Map<String, String> findAllLanguageMessages(String langCode) {
		List<Language> languageList = languagesRepo.findByCode(langCode);
        Map<String, String> languageMessages = new HashMap<>();
        for (Language language : languageList) {
            languageMessages.put(language.getKey(), language.getValue());
        }
        return languageMessages;
	}

	@Override
	public Language update(Language language, Integer langId) {
		if (langId == null || langId <= 0) {
			throw new BadRequestException();
		}
		return languagesRepo.save(language);
	}

	@Override
	public String delete(Integer entityId) {
		return null;
	}

	@Override
	public void reloadMessage(String langCode) {
		try {
			Map<String, String> enMessages = this.findAllLanguageMessages(langCode);
			Properties properties = new Properties();
			OutputStream outputStream = new FileOutputStream(String.format("src/main/resources/language/messages_%s.properties", langCode));
			for (Map.Entry<String, String> entry : enMessages.entrySet()) {
				properties.setProperty(entry.getKey(), entry.getValue());
			}
			properties.store(outputStream, String.format("%s Messages", langCode));
		} catch (IOException e) {
			throw new AppException(e);
		}
	}
}