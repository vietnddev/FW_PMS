package com.flowiee.app.service.impl;

import com.flowiee.app.entity.Notification;
import com.flowiee.app.exception.BadRequestException;
import com.flowiee.app.repository.NotificationRepository;
import com.flowiee.app.service.NotificationService;

import com.flowiee.app.utils.AppConstants;
import com.flowiee.app.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> findAllByReceiveId(Integer pageSize, Integer pageNum, Integer totalRecord, Integer accountId) {
        if (totalRecord != null) {
            return notificationRepository.findLimitByReceiveId(accountId, totalRecord);
        }
        return notificationRepository.findAllByReceiveId(accountId);
    }

    @Override
    public List<Notification> findLimitByReceiveId(Integer accountId, Integer limit) {
        return notificationRepository.findLimitByReceiveId(accountId, limit);
    }

    @Override
    public Notification findById(Integer notificationId) {
        return notificationRepository.findById(notificationId).orElse(null);
    }

    @Override
    public Notification save(Notification notification) {
        if (notification == null) {
            throw new BadRequestException();
        }
        Notification notificationSaved = notificationRepository.save(notification);
        logger.info(NotificationServiceImpl.class.getName() + ": Insert notification " + notification.toString());
        return notificationSaved;
    }

    @Override
    public Notification update(Notification entity, Integer entityId) {
        if (entity == null || entityId == null || entityId <= 0) {
            throw new BadRequestException();
        }
        entity.setId(entityId);
        return notificationRepository.save(entity);
    }

    @Override
    public String delete(Integer entityId) {
        Notification notification = this.findById(entityId);
        if (notification == null) {
            throw new BadRequestException();
        }
        notificationRepository.deleteById(entityId);
        return MessageUtils.DELETE_SUCCESS;
    }
}