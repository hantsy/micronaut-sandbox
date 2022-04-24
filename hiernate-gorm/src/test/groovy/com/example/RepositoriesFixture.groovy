package com.example

import com.example.service.PersonService
import grails.gorm.transactions.TransactionService
import io.micronaut.context.ApplicationContext

trait RepositoriesFixture {
    abstract ApplicationContext getApplicationContext()

    PersonService getPersonService() {
        applicationContext.getBean(PersonService)
    }

    TransactionService getTransactionService() {
        applicationContext.getBean(TransactionService)
    }
}