package com.example

import com.example.model.Address
import com.example.model.Person
import groovy.util.logging.Slf4j

@Slf4j
class PersonSpec extends ApplicationContextSpecification {

    String getSpecName() {
        return 'PersonSpec'
    }

    def 'test person'() {
        given:
        def p = new Person(name: "Fred", age: 40)
        transactionService.withTransaction {
            p.save()
        }

        when:
        def get = transactionService.withTransaction {
            Person.get(p.id)
        }

        then:
        get.name == "Fred"
        get.age == 40

        when:
        Person updatedPerson = transactionService.withTransaction {
            def first = Person.first()
            first.homeAddress = new Address(city: "Guangzhou", street: "test", zipCode: "000123")
            first.nicknames = ["Hello", "World"]
            first.save(flush: true)
        }

        then:
        updatedPerson
        updatedPerson.homeAddress
        updatedPerson.homeAddress.city == "Guangzhou"
        updatedPerson.nicknames == ["Hello", "World"].toSet()
        updatedPerson.dateCreated
        updatedPerson.dateCreated < updatedPerson.lastUpdated

        cleanup:
        applicationContext.stop()
    }

}
