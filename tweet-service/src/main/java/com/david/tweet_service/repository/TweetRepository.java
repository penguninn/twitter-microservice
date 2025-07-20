package com.david.tweet_service.repository;

import com.david.common.enums.Visibility;
import com.david.tweet_service.entity.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String> {

    Optional<Tweet> findByUserId(String tweetId);

    Optional<Tweet> findByIdAndVisibility(String tweetId, Visibility visibility);

    Page<Tweet> findAllByUserId(String userId, Pageable pageable);

    Page<Tweet> findAllByUserIdAndVisibility(String userId, Visibility visibility, Pageable pageable);

    long countByUserId(String userId);

    Page<Tweet> findAllByContentContaining(String keyword, Pageable pageable);

    Page<Tweet> findAllByHashtagsContaining(String hashtag, Pageable pageable);
}
