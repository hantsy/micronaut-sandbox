package com.example.model

import grails.gorm.annotation.Entity

import java.time.LocalDateTime

@Entity
class Person {

    // declare a id type explicitly.
    UUID id

    String name
    Integer age = 20
    Address homeAddress
    Address workAddress

    //by default it will be a `Set`
    List<String> nicknames

    //will be filled automatically when inserting and updating
    LocalDateTime dateCreated
    LocalDateTime lastUpdated

    static constraints = {
        homeAddress nullable: true
        workAddress nullable: true
    }

    static embedded = ['homeAddress', 'workAddress']
    static hasMany = [nicknames: String]

    static mapping = {

        // it can be `assigned`.
        id generator : 'uuid2', type: 'pg-uuid' // we uses Postgres here.
        nicknames joinTable: [name  : 'bunch_o_nicknames',
                              key   : 'person_id',
                              column: 'nickname',
                              type  : "text"]
    }

}
