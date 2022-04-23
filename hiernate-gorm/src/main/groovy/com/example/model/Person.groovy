package com.example.model

class Person {
    Address homeAddress
    Address workAddress

    static embedded = ['homeAddress', 'workAddress']
    static hasMany = [nicknames: String]

    static mapping = {
        nicknames joinTable: [name: 'bunch_o_nicknames',
                              key: 'person_id',
                              column: 'nickname',
                              type: "text"]
    }
}
