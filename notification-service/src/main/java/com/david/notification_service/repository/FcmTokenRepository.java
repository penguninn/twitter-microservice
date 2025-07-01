package com.david.notification_service.repository;

import com.david.notification_service.entity.FcmToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {
    Optional<FcmToken> findByUserId(String userId);

    List<FcmToken> findAllByUserId(String userId);
}
