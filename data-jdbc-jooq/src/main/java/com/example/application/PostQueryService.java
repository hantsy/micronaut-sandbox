package com.example.application;

import java.util.UUID;

public interface PostQueryService {
    PaginatedResult<PostSummary> findPostsByKeyword(String keyword, int offset, int limit);
    PostDetails getPostDetailsById(UUID postId);
}
