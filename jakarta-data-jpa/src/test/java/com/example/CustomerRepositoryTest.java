package com.example;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@Slf4j
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
        log.debug("setup....");
    }

    @Test
    public void testInsertAndQuery() {
        var saved = customerRepository.save(Customer.of("Jack", 20, Address.of("", "New York", "210000")));
        assertNotNull(saved);
        var found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        found.ifPresent(it -> assertThat(it.getName()).isEqualTo("Jack"));

        var rose = customerRepository.save(Customer.of("Rose", 18, Address.of("", "New York", "210000")));

        var customerByNameLike = customerRepository.byNameLike("%Rose");
        assertNotNull(customerByNameLike);
        log.debug("Customer query by name like:{}", (Object) customerByNameLike);

        var foobar = customerRepository.save(Customer.of("foobar", 18, Address.of("", "Bei Jing", "100000")));
        var byAddressZip = customerRepository.findByAddressZip("210000", Order.by(Sort.desc("name")));
        log.debug("Customer query by address zip: {}", byAddressZip);

        var byAddressCityLike = customerRepository.findByAddressCityLike("New%", PageRequest.ofPage(1, 10, true));
        log.debug("Customer query by address city like: {}", byAddressCityLike);
    }
}
