package com.david.tweet_service.mapper;

import com.david.tweet_service.dto.response.MediaResponse;
import com.david.tweet_service.entity.Media;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaResponse toDto(Media media);
    Media toEntity(MediaResponse mediaResponse);
}
