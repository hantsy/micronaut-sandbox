package com.example.client;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
public class PageDto<T> {
    int totalSize;
    List<T> content = new ArrayList<>();
    Pageable pageable;

    @Data
    @NoArgsConstructor
    @Serdeable
    public static class Pageable {
        int number;
        int size;
    }
}
