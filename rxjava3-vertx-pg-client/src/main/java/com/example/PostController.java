package com.example;


import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.UUID;

import static io.micronaut.http.HttpResponse.*;

@Controller("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;

    @Get
    public MutableHttpResponse<Flowable<Post>> getPosts() {
        return ok(this.postRepository.findAll());
    }

    @Get("{id}")
    public @NonNull Single<MutableHttpResponse<Post>> getById(@PathVariable UUID id) {
        return this.postRepository.findById(id).map(data -> ok(data));
    }

    @io.micronaut.http.annotation.Post
    public @NonNull Single<MutableHttpResponse<Void>> createPost(@Body @Valid CreatePostCommand data) {
        var saved = this.postRepository.save(Post.of(data.title(), data.content()));
        return saved.map(s -> created(URI.create("/posts/" + saved)));
    }

    @Delete("{id}")
    public Single<MutableHttpResponse<Void>> deleteById(@PathVariable UUID id) {
        return postRepository.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) return noContent();
                    else return notFound();
                });
    }
}
