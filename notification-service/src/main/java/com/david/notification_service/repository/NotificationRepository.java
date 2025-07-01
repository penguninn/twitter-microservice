package com.david.notification_service.repository;

import com.david.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    @Query("{'userId': ?0}")
    @Update("{'$set': {'read': true}}")
    int markAllAsReadByUserId(String userId);

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndRead(String userId, boolean read, Pageable pageable);

    long countByUserIdAndRead(String userId, boolean read);

    int deleteByUserIdAndRead(String userId, boolean read);

    int deleteByUserId(String userId);
}
