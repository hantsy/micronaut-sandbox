package com.example.photos;

import java.util.Date;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record PhotoInfo(String id, String filename, int chunkSize, long length, Date uploadedDate, String contentType) {
}
