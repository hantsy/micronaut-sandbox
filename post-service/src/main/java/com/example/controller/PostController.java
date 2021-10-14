package com.example.controller;

import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Controller("/posts")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PostController {
    private final PostRepository posts;
    private final CommentRepository comments;

    @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<List<Post>> getAll() {
        return HttpResponse.ok(posts.findAll());
    }

    @io.micronaut.http.annotation.Post(uri = "/", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<Void> create(@Body CreatePostDto dto) {
        var data = Post.builder().title(dto.title()).content(dto.content()).build();
        var saved = this.posts.save(data);
        return HttpResponse.created(URI.create("/posts/" + saved.getId()));
    }

    @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Post> getById(@PathVariable UUID id) {
        return posts.findById(id)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::notFound);
    }

    @Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> deleteById(@PathVariable UUID id) {
        return posts.findById(id)
                .map(p -> {
                    this.posts.delete(p);
                    return HttpResponse.noContent();
                })
                .orElseGet(HttpResponse::notFound);
    }

    // nested comments endpoints
    @Get(uri = "/{id}/comments", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getCommentsByPostId(@PathVariable UUID id) {
        return posts.findById(id)
                .map(post -> {
                    var comments = this.comments.findByPost(post);
                    return HttpResponse.ok(comments.stream().map(c -> new CommentDetailsDto(c.getId(), c.getContent())));
                })
                .orElseGet(HttpResponse::notFound);
    }

    @io.micronaut.http.annotation.Post(uri = "/{id}/comments", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> create(@PathVariable UUID id, @Body CreateCommentDto dto) {

        return posts.findById(id)
                .map(post -> {
                    var data = Comment.builder().content(dto.content()).post(post).build();
                    var saved = this.comments.save(data);
                    return HttpResponse.created(URI.create("/comments/" + saved.getId()));
                })
                .orElseGet(HttpResponse::notFound);

    }
}