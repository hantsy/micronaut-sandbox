package com.example.model

class Book {
    String title
    String releaseYear
    String isbn

    static hasMany = [authors: Author, reviews: Review]
    static mapping = {
        reviews cascade: 'all-delete-orphan'
    }
}
