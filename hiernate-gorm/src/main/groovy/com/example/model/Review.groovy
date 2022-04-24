package com.example.model

import grails.gorm.annotation.Entity

@Entity
class Review {
    String author
    String content

    static belongsTo = [book: Book]
}
