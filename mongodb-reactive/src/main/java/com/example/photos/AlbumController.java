package com.example.photos;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;

import static io.micronaut.http.HttpResponse.*;

@Controller("/albums")
@RequiredArgsConstructor
@Slf4j
public class AlbumController {
    private final AlbumRepository albumRepository;

    @Get(uri = "/", produces = {MediaType.APPLICATION_JSON})
    public Flux<?> all() {
        return this.albumRepository.findAll();
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<Album>> byId(@PathVariable ObjectId id) {
        return this.albumRepository.findById(id)
                .map(HttpResponse::ok)
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post(uri = "/", consumes = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> create(@Body Album data) {
        return this.albumRepository.insertOne(data)
                .map(id -> created(URI.create("/albums/" + id)));
    }

    @Post(uri = "/{id}/photos", consumes = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<Object>> addPhotosToAlbum(@PathVariable ObjectId id, @Body AddPhotoToAlbumDto data) {
        return this.albumRepository.findById(id)
                .flatMap(album -> {
                    Arrays.stream(data.photoIds()).forEach(album::addPhoto);
                    return this.albumRepository.update(id, album);
                })
                .map(r -> HttpResponse.noContent())
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Delete(uri = "/{id}/photos", consumes = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<Object>> removePhotosToAlbum(@PathVariable ObjectId id, @Body RemovePhotoFromAlbumDto data) {
        return this.albumRepository.findById(id)
                .flatMap(album -> {
                    Arrays.stream(data.photoIds()).forEach(album::removePhoto);
                    return this.albumRepository.update(id, album);
                })
                .map(r -> HttpResponse.noContent())
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return this.albumRepository.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) {
                        return noContent();
                    } else {
                        return notFound();
                    }
                });
    }
}
