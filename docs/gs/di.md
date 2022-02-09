
# Understanding Dependency Injection in Micronaut

In Micronaut, it used  JSR330(aka @Inject) specification to annotate the injectable beans. JSR330 originally is lead by SpringSource(now VMware) and Google. 

> Spring also has built-in JSR330 support, by default it is not activated. You should add `inject` artifact in your project dependencies to enable it.

When a class is annotated with `@Singleton` means there is only one instance shared in the application scope, `@Prototype` will produce a new instance for every injection.

Micronaut provides a `@Factory` to produces simple beans in groups, for example.

```java
@Factory
class MyConfig{
    
    @Singleton
    public Foo foo(){}
    
    @Singleton
    public Bar bar(){}
}
```

As described in former sections,  Micronaut process IOC at compile time. When building the application,  explore the project *build/classes* folder, you will find there are a lot of extra classes generated at compile time which names are start with a USD("**$**") symbol.