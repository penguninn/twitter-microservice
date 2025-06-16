package com.david.comment_service.mapper;

import com.david.comment_service.dto.request.CommentRequest;
import com.david.comment_service.entity.Comment;
import com.david.common.dto.comment.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentResponse toDto(Comment comment);

    Comment toEntity(CommentRequest request);
}
