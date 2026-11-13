package com.example;

import com.example.dto.CustomerDto;
import com.example.dto.NewCustomerCommand;
import com.example.dto.NewCustomerCommand;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "mock", transactional = false)
class CustomerControllerTest {

    @MockBean(CustomerRepository.class)
    CustomerRepository mockedCustomerRepository() {
        return mock(CustomerRepository.class);
    }

    @Inject
    CustomerRepository customerRepository;

    @Inject
    @Client("/customers")
    ReactorHttpClient client;

    @Test
    @DisplayName("get all customers")
    public void testGetAllCustomers() {
        when(this.customerRepository.findAll())
                .thenReturn(
                        List.of(
                                new Customer(UUID.randomUUID(), "Jack", 20, new Address("xian", "xian", "510000"), 1L),
                                new Customer(UUID.randomUUID(), "William", 40, new Address("xian", "xian", "510000"), 1L)
                        )
                );

        var request = HttpRequest.GET("");
        client.exchange(request, Argument.listOf(CustomerDto.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).anyMatch(customer -> customer.name().equals("Jack"));
                })
                .verifyComplete();

        verify(this.customerRepository, times(1)).findAll();
        verifyNoMoreInteractions(this.customerRepository);
    }

    @Test
    @DisplayName("create a new customer")
    public void testCreateCustomer() {
        var id = UUID.randomUUID();
        when(this.customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer(id, "Jack", 20, null, 1L));

        var request = HttpRequest.POST("", new NewCustomerCommand("Jack", 20, null));
        client.exchange(request, Argument.VOID)
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.CREATED, res.getStatus());
                    assertThat(res.getHeaders().get("Location")).isEqualTo("/customers/" + id);
                })
                .verifyComplete();

        verify(this.customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("get a customer by id")
    public void testGetCustomerById() {
        when(this.customerRepository.findById(any(UUID.class)))
                .thenReturn(
                        Optional.of(
                                new Customer(UUID.randomUUID(), "Jack", 20, new Address("xian", "xian", "510000"), 1L)
                        )
                );

        var request = HttpRequest.GET(UriBuilder.of("/{id}").expand(Map.of("id", UUID.randomUUID())));
        client.exchange(request, Argument.of(CustomerDto.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body().name()).isEqualTo("Jack");
                })
                .verifyComplete();

        verify(this.customerRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.customerRepository);
    }

    @Test
    @DisplayName("get a customer by a non-existing id")
    public void testGetCustomerByNonExistingId() {
        when(this.customerRepository.findById(any(UUID.class)))
                .thenReturn(Optional.empty());

        var request = HttpRequest.GET(UriBuilder.of("/{id}").expand(Map.of("id", UUID.randomUUID())));
        client.exchange(request, Argument.of(CustomerDto.class))
                .as(StepVerifier::create)
                .consumeErrorWith(r -> {
                    assertThat(r).isInstanceOf(HttpClientResponseException.class);
                    assertEquals(HttpStatus.NOT_FOUND, ((HttpClientResponseException) r).getStatus());
                })
                .verify();

        verify(this.customerRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.customerRepository);
    }

}
