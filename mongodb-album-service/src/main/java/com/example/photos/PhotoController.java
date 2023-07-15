package com.example.photos;

import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.StreamingFileUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

import static io.micronaut.http.HttpResponse.noContent;
import static io.micronaut.http.HttpResponse.ok;

@Controller("/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final GridFSBucket bucket;

    @Get(uri = "", produces = {MediaType.APPLICATION_JSON})
    public Flux all() {
        return Flux.from(this.bucket.find())
                .map(file -> new PhotoInfo(
                                file.getObjectId().toHexString(),
                                file.getFilename(),
                                file.getChunkSize(),
                                file.getLength(),
                                file.getUploadDate(),
                                file.getMetadata().getString("contentType")
                        )
                );
    }

    @Post(uri = "", consumes = {MediaType.MULTIPART_FORM_DATA})
    public Mono<HttpResponse<?>> upload(@Part StreamingFileUpload file) {
        var filename = file.getFilename();
        var name = file.getName();
        var contentType = file.getContentType();
        var size = file.getSize();
        log.debug("uploading file...\n filename:{},\n name:{},\n contentType: {},\n size: {} ", filename, name, contentType, size);
        var options = new GridFSUploadOptions();
        contentType.ifPresent(c -> options.metadata(new Document("contentType", c)));
        return Mono.from(this.bucket.uploadFromPublisher(
                                filename,
                                Mono.from(file).mapNotNull(partData -> {
                                    try {
                                        return partData.getByteBuffer();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }),
                                options
                        )
                )
                .map(ObjectId::toHexString)
                .map(id -> ok(Map.of("id", id)));
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_OCTET_STREAM})
    public Mono<HttpResponse<?>> download(@PathVariable ObjectId id) {
        return Mono.from(this.bucket.downloadToPublisher(id))
                .map(HttpResponse::ok);
    }

    @Get(uri = "/{id}/info", produces = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> fileInfo(@PathVariable ObjectId id) {
        return Mono.from(this.bucket.downloadToPublisher(id).getGridFSFile())
                .map(file -> new PhotoInfo(id.toHexString(), file.getFilename(), file.getChunkSize(), file.getLength(), file.getUploadDate(), file.getMetadata().getString("contentType")))
                .map(HttpResponse::ok);
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return Mono.from(this.bucket.delete(id))
                .map(v -> noContent());
    }
}
