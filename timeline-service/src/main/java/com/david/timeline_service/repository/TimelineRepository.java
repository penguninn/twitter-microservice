package com.david.timeline_service.repository;

import com.david.timeline_service.entity.TimelineEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineRepository extends MongoRepository<TimelineEntry, String> {

    Page<TimelineEntry> findByUserId(String userId, Pageable pageable);

    void deleteByUserIdAndTweetOwnerId(String userId, String tweetId);
}
