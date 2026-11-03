package com.example.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDto<T> {
    int pageNumber;
    int size;
    int numberOfElements;
    int totalPages;
    int totalSize;
    List<T> content = new ArrayList<>();
}
