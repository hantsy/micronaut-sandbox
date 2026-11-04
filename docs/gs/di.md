
# Dependency injection

Micronaut creates and wires application beans from metadata generated at
compile time. Use scope annotations to register a class as a bean, then use
constructor injection to declare its dependencies.

## Declare and inject beans

Annotate a service with `@Singleton` to register one instance in the
application context:

```java
@Singleton
class OrderService {
}
```

Inject the service through a constructor. Micronaut can use the single
constructor without an `@Inject` annotation:

```java
@Controller("/orders")
class OrderController {
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
}
```

Use `@Prototype` when the context should create a new bean instance for each
injection request. Use `ApplicationContext` when you need to retrieve a bean
programmatically, such as in a standalone application or a test:

```java
try (var context = ApplicationContext.run()) {
    var orderService = context.getBean(OrderService.class);
}
```

## Compile-time bean metadata

The Micronaut build plugins process bean definitions during compilation. The
generated metadata lets the application resolve and inject beans without
discovering their definitions through runtime classpath scanning. See the
[Micronaut dependency injection guide](https://docs.micronaut.io/latest/guide/#ioc)
for scopes, factories, and additional bean configuration options.


