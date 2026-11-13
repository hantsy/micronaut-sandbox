package com.example;

import io.micronaut.context.annotation.Property;
import com.example.dto.NewCustomerCommand;
import com.example.dto.UpdateCustomerCommand;
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
import org.mockito.ArgumentCaptor;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "mock", transactional = false)
@Property(name = "mongodb.uri", value = "mongodb://localhost:27017/mydb")
@Property(name = "micronaut.data.mongodb.create-collections", value = "false")
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
                                new Customer(null, "Jack", 20, new Address("xian", "xian", "510000")),
                                new Customer(null, "William", 40, new Address("xian", "xian", "510000"))
                        )
                );

        var request = HttpRequest.GET("");
        client.exchange(request, Argument.listOf(Customer.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).anyMatch(customer -> customer.name().equals("Jack"));
                })
                .verifyComplete();

        verify(this.customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("create a new customer")
    public void testCreateCustomer() {
        var id = UUID.randomUUID().toString();
        when(this.customerRepository.save(any(Customer.class)))
                .thenReturn(new Customer(id, "Jack", 20, null));

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
    @DisplayName("update a customer")
    public void testUpdateCustomer() {
        var id = UUID.randomUUID().toString();
        when(this.customerRepository.findById(anyString()))
                .thenReturn(Optional.of(new Customer(id, "Jack", 20, null)));
        when(this.customerRepository.update(any(Customer.class)))
                .thenReturn(new Customer(id, "William", 40, null));

        var request = HttpRequest.PUT(UriBuilder.of("/{id}").expand(Map.of("id", id)),
                new UpdateCustomerCommand("William", 40, null));
        client.exchange(request, Argument.VOID)
                .as(StepVerifier::create)
                .consumeNextWith(res -> assertEquals(HttpStatus.NO_CONTENT, res.getStatus()))
                .verifyComplete();

        verify(this.customerRepository, times(1)).findById(anyString());
        var captor = ArgumentCaptor.forClass(Customer.class);
        verify(this.customerRepository, times(1)).update(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("William");
        assertThat(captor.getValue().age()).isEqualTo(40);
        assertThat(captor.getValue().address()).isNull();
    }

    @Test
    @DisplayName("update a non-existing customer")
    public void testUpdateNonExistingCustomer() {
        when(this.customerRepository.findById(anyString())).thenReturn(Optional.empty());

        var request = HttpRequest.PUT(UriBuilder.of("/{id}").expand(Map.of("id", UUID.randomUUID().toString())),
                new UpdateCustomerCommand("William", 40, null));
        client.exchange(request, Argument.VOID)
                .as(StepVerifier::create)
                .consumeErrorWith(r -> {
                    assertThat(r).isInstanceOf(HttpClientResponseException.class);
                    assertEquals(HttpStatus.NOT_FOUND, ((HttpClientResponseException) r).getStatus());
                })
                .verify();

        verify(this.customerRepository, times(1)).findById(anyString());
        verify(this.customerRepository, never()).update(any(Customer.class));
    }

    @Test
    @DisplayName("get a customer by id")
    public void testGetCustomerById() {
        var id = UUID.randomUUID().toString();
        when(this.customerRepository.findById(anyString()))
                .thenReturn(
                        Optional.of(new Customer(id, "Jack", 20, new Address("xian", "xian", "510000")))
                );

        var request = HttpRequest.GET(UriBuilder.of("/{id}").expand(Map.of("id", id)));
        client.exchange(request, Argument.of(Customer.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body().name()).isEqualTo("Jack");
                })
                .verifyComplete();

        verify(this.customerRepository, times(1)).findById(anyString());
    }

    @Test
    @DisplayName("get a customer by a non-existing id")
    public void testGetCustomerByNonExistingId() {
        when(this.customerRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        var request = HttpRequest.GET(UriBuilder.of("/{id}").expand(Map.of("id", UUID.randomUUID().toString())));
        client.exchange(request, Argument.of(Customer.class))
                .as(StepVerifier::create)
                .consumeErrorWith(r -> {
                    assertThat(r).isInstanceOf(HttpClientResponseException.class);
                    assertEquals(HttpStatus.NOT_FOUND, ((HttpClientResponseException) r).getStatus());
                })
                .verify();

        verify(this.customerRepository, times(1)).findById(anyString());
    }
}
