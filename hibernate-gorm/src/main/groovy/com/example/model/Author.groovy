package com.example.model

import grails.gorm.annotation.Entity

@Entity
class Author {
    String name
    static hasMany = [books: Book]
}
