package com.example.photos;

import java.util.Date;

public record PhotoInfo(String id, String filename, int chunkSize, long length, Date uploadedDate, String contentType) {
}
