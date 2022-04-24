package com.example

trait LeakageDetector extends RepositoriesFixture {
    boolean hasLeakage() {
//        return transactionService.withTransaction {
//            personService.count() > 0
//        }
        return false
    }
}