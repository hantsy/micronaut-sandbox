package com.example.interfaces;

import com.example.application.CreatePostCommand;
import com.example.application.PaginatedResult;
import com.example.application.PostCommandService;
import com.example.application.PostDetails;
import com.example.application.PostQueryService;
import com.example.application.PostSummary;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    @Get("/posts")
    public PaginatedResult<PostSummary> findPosts(
            @QueryValue(value = "q", defaultValue = "") String keyword,
            @QueryValue(defaultValue = "0") int offset,
            @QueryValue(defaultValue = "10") int limit
    ) {
        return postQueryService.findPostsByKeyword(keyword, offset, limit);
    }

    @Get("/posts/{id}")
    public HttpResponse<PostDetails> findPost(@PathVariable UUID id) {
        var details = postQueryService.getPostDetailsById(id);
        return details == null ? HttpResponse.notFound() : HttpResponse.ok(details);
    }

    @Post("/posts")
    public HttpResponse<?> createPost(@Body CreatePostCommand command) {
        var savedPostID = postCommandService.createPost(command);
        return HttpResponse.created(URI.create("/posts/" + savedPostID));
    }
}
