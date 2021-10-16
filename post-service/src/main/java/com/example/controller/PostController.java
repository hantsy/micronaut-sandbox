package com.example.controller;

import com.example.controller.dto.*;
import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.repository.PostSpecifications;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static io.micronaut.http.HttpResponse.ok;

@Controller("/posts")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Validated
public class PostController {
    private final PostRepository posts;
    private final CommentRepository comments;

//    @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
//    public HttpResponse<List<PostSummaryDto>> getAll() {
//        var body = posts.findAll()
//                .stream()
//                .map(p -> new PostSummaryDto(p.getId(), p.getTitle(), p.getCreatedAt()))
//                .toList();
//        return ok(body);
//    }

    @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
    @Transactional
    public HttpResponse<Page<PostSummaryDto>> getAll(@QueryValue(defaultValue = "") String q,
                                                     @QueryValue(defaultValue = "") String status,
                                                     @QueryValue(defaultValue = "0") int page,
                                                     @QueryValue(defaultValue = "10") int size) {
        var pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("createdAt")));
        var postStatus = StringUtils.hasText(status) ? com.example.domain.Status.valueOf(status) : null;
        var data = this.posts.findAll(PostSpecifications.filterByKeywordAndStatus(q, postStatus), pageable);
        var body = data.map(p -> new PostSummaryDto(p.getId(), p.getTitle(), p.getCreatedAt()));
        return ok(body);
    }

    @io.micronaut.http.annotation.Post(uri = "/", consumes = MediaType.APPLICATION_JSON)
    @Transactional
    public HttpResponse<Void> create(@Body @Valid CreatePostCommand dto) {
        var data = Post.builder().title(dto.title()).content(dto.content()).build();
        var saved = this.posts.save(data);
        return HttpResponse.created(URI.create("/posts/" + saved.getId()));
    }

    @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getById(@PathVariable UUID id) {
        return posts.findById(id)
                .map(p -> ok(new PostDetailsDto(p.getId(), p.getTitle(), p.getContent(), p.getStatus(), p.getCreatedAt())))
                .orElseThrow(() -> new PostNotFoundException(id));
        //.orElseGet(HttpResponse::notFound);
    }

    @Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Transactional
    public HttpResponse<?> deleteById(@PathVariable UUID id) {
        return posts.findById(id)
                .map(p -> {
                    this.posts.delete(p);
                    return HttpResponse.noContent();
                })
                .orElseThrow(() -> new PostNotFoundException(id));
        //.orElseGet(HttpResponse::notFound);
    }

    // nested comments endpoints
    @Get(uri = "/{id}/comments", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getCommentsByPostId(@PathVariable UUID id) {
        return posts.findById(id)
                .map(post -> {
                    var comments = this.comments.findByPost(post);
                    return ok(comments.stream().map(c -> new CommentDetailsDto(c.getId(), c.getContent(), c.getCreatedAt())));
                })
                .orElseThrow(() -> new PostNotFoundException(id));
        //.orElseGet(HttpResponse::notFound);
    }

    @io.micronaut.http.annotation.Post(uri = "/{id}/comments", consumes = MediaType.APPLICATION_JSON)
    @Transactional
    public HttpResponse<?> create(@PathVariable UUID id, @Body @Valid CreateCommentCommand dto) {

        return posts.findById(id)
                .map(post -> {
                    var data = Comment.builder().content(dto.content()).post(post).build();
                    post.getComments().add(data);
                    var saved = this.comments.save(data);
                    return HttpResponse.created(URI.create("/comments/" + saved.getId()));
                })
                .orElseThrow(() -> new PostNotFoundException(id));
        // .orElseGet(HttpResponse::notFound);

    }
}