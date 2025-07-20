package com.david.search_service.repository;

import com.david.search_service.document.TweetDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetSearchRepository extends ElasticsearchRepository<TweetDocument, String> {
    List<TweetDocument> findTweetDocumentsByContentContaining(String content);
}
