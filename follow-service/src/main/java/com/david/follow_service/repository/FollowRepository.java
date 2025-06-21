package com.david.follow_service.repository;

import com.david.follow_service.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<UserFollow, String> {

    boolean existsByFollowerIdAndFollowedId(String followerId, String followedId);

    Optional<UserFollow> findByFollowerIdAndFollowedId(String followerId, String followedId);

    Page<UserFollow> findByFollowedId(String followedId, Pageable pageable);

    Page<UserFollow> findByFollowerId(String followerId, Pageable pageable);

    long countByFollowedId(String followedId);

    long countByFollowerId(String followerId);

    void deleteByFollowerIdAndFollowedId(String followerId, String followedId);
}
