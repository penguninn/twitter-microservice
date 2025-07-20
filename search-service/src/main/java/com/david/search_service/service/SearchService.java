package com.david.search_service.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.david.search_service.document.TweetDocument;
import com.david.search_service.document.UserDocument;
import com.david.search_service.dto.response.TweetSearchResponseDTO;
import com.david.search_service.dto.response.UserSearchResponseDTO;
import com.david.search_service.repository.TweetSearchRepository;
import com.david.search_service.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final TweetSearchRepository tweetSearchRepository;
    private final UserSearchRepository userSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void indexTweet(TweetDocument tweet) {
        try {
            tweetSearchRepository.save(tweet);
            log.info("Indexed tweet: {}", tweet.getTweetId());
        } catch (Exception e) {
            log.error("Error indexing tweet {}: {}", tweet.getTweetId(), e.getMessage(), e);
        }
    }

    public void deleteTweetFromIndex(String tweetId) {
        try {
            tweetSearchRepository.deleteById(tweetId);
            log.info("Deleted tweet from index: {}", tweetId);
        } catch (Exception e) {
            log.error("Error deleting tweet {} from index: {}", tweetId, e.getMessage(), e);
        }
    }

    public void indexUser(UserDocument user) {
        try {
            userSearchRepository.save(user);
            log.info("Indexed user: {}", user);
        } catch (Exception e) {
            log.error("Error indexing user {}", e.getMessage(), e);
        }
    }

    public Page<TweetSearchResponseDTO> searchTweets(String queryTerm, String currentUserId, Pageable pageable) {
        log.info("Searching tweets for term: '{}', by user: {}", queryTerm, currentUserId);

        // Build main content query
        Query contentQuery = MultiMatchQuery.of(m -> m
                .query(queryTerm)
                .fields("content", "hashtags")
                .fuzziness("AUTO")
        )._toQuery();

        // Build security filter
        BoolQuery.Builder securityFilterBuilder = new BoolQuery.Builder()
                .should(TermQuery.of(t -> t
                        .field("visibility.keyword")
                        .value("PUBLIC")
                )._toQuery());

        if (currentUserId != null && !currentUserId.isBlank()) {
            Query privateUserQuery = BoolQuery.of(b -> b
                    .must(TermQuery.of(t -> t
                            .field("visibility.keyword")
                            .value("PRIVATE")
                    )._toQuery())
                    .must(TermQuery.of(t -> t
                            .field("userId.keyword")
                            .value(currentUserId)
                    )._toQuery())
            )._toQuery();
            securityFilterBuilder.should(privateUserQuery);
        }

        Query securityFilter = securityFilterBuilder
                .minimumShouldMatch("1")
                .build()
                ._toQuery();

        // Combine queries
        Query finalQuery = BoolQuery.of(b -> b
                .must(contentQuery)
                .filter(securityFilter)
        )._toQuery();

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(finalQuery)
                .withPageable(pageable)
                .build();

        SearchHits<TweetDocument> searchHits = elasticsearchOperations.search(searchQuery, TweetDocument.class);
        List<TweetSearchResponseDTO> dtos = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> TweetSearchResponseDTO.builder()
                        .id(doc.getTweetId())
                        .content(doc.getContent())
                        .userId(doc.getUserId())
                        .hashtags(getHashtagsFromDocument(doc))
                        .createdAt(doc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, searchHits.getTotalHits());
    }

    public Page<UserSearchResponseDTO> searchUsers(String queryTerm, Pageable pageable) {
        log.info("Searching users for term: '{}'", queryTerm);
        
        Query query = MultiMatchQuery.of(m -> m
                .query(queryTerm)
                .fields("displayName", "username", "bio")
                .fuzziness("AUTO")
        )._toQuery();

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(searchQuery, UserDocument.class);
        List<UserSearchResponseDTO> dtos = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> UserSearchResponseDTO.builder()
                        .userId(doc.getUserId())
                        .displayName(getDisplayNameFromDocument(doc))
                        .username(doc.getUsername())
                        .bio(getBioFromDocument(doc))
                        .profileImageUrl(getProfileImageUrlFromDocument(doc))
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, searchHits.getTotalHits());
    }

    // Helper methods to handle potential missing getters in document classes
    private List<String> getHashtagsFromDocument(TweetDocument doc) {
        try {
            return doc.getHashtags();
        } catch (Exception e) {
            log.warn("Could not get hashtags from tweet document: {}", e.getMessage());
            return List.of();
        }
    }

    private String getDisplayNameFromDocument(UserDocument doc) {
        try {
            return doc.getDisplayName();
        } catch (Exception e) {
            log.warn("Could not get display name from user document: {}", e.getMessage());
            return doc.getUsername(); // fallback to username
        }
    }

    private String getBioFromDocument(UserDocument doc) {
        try {
            return doc.getBio();
        } catch (Exception e) {
            log.warn("Could not get bio from user document: {}", e.getMessage());
            return null;
        }
    }

    private String getProfileImageUrlFromDocument(UserDocument doc) {
        try {
            return doc.getProfileImageUrl();
        } catch (Exception e) {
            log.warn("Could not get profile image URL from user document: {}", e.getMessage());
            return null;
        }
    }

}