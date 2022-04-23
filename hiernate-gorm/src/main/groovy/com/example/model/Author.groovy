package com.example.model

class Author {
    String name
    static hasMany = [books: Book]
}
