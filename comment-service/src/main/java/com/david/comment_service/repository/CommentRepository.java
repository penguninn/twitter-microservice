package com.david.comment_service.repository;

import com.david.comment_service.entity.Comment;
import com.david.common.enums.CommentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    Page<Comment> findAllByTweetIdAndType(String tweetId, CommentType type, Pageable pageable);

    Page<Comment> findAllByParentIdAndType(String commentId, CommentType type, Pageable pageable);

    void deleteAllByParentId(String commentId);
}
