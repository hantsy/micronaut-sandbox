package com.example.model

class Review {
    String author
    String content

    static belongsTo = [book: Book]
}
