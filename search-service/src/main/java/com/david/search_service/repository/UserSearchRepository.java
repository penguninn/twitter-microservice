package com.david.search_service.repository;

import com.david.search_service.document.UserDocument;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {
    List<UserDocument> findUserDocumentsByUsernameContaining(String username);
}
