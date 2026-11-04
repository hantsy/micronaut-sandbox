package com.example.photos;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AddPhotoToAlbumDto(String[] photoIds) {
}
