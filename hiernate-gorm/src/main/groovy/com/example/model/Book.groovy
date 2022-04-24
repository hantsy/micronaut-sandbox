package com.example.model

import grails.gorm.annotation.Entity

@Entity
class Book {
    String title
    String releaseYear
    String isbn

    static hasMany = [authors: Author, reviews: Review]
    static mapping = {
        reviews cascade: 'all-delete-orphan'
    }
}
