
# Understanding Dependency Injection in Micronaut

Micronaut provides a JSR330(aka `@Inject` specification) compatiable IOC container.

> JSR330 was led by SpringSource(now VMware) and Google. 

> Spring also has built-in JSR330 support, but it is not activated by default. You should add `inject` artifact in your project dependencies to enable it.

## Declare Beans

To declare an injectable bean in Micronaut applications,  add a `@Singleton` annotation to your class. Thus the bean is available in the Micronaut IOC container.

```java
@Singleton
class OrderService{}
```

To use it in other beans, inject it with a `@Inejct` or use constructor injection.

```java
@Controller
class OrderController{
    @Inject OrderService orderService;
    //....
}

// constructor injection.
@Controller
class OrderController{
    private final OrderService orderService;
    
    public OrderController( OrderService orderService){
        this.orderService = orderService;
    }
}
```

Micronaut also provides an `ApplicationContext` which allows you to fetch beans manually.

```java
var context = ApplicationContext.run();
var orderService = context.getBean(OrderService.class);
```

Or register your POJOs as beans.

```java
var context = ApplicationContext.builder()
        .build();
context.registerSingleton(new FooBar());
context.start();

var fooBar = context.getBean(FooBar.class);
```        

A `@Singleton` bean means there is only one instance in the application at runtime.  `@Prototype` ensures that it produces a new instance for every injection.

```java
@Prototype
class UserModel {
}

var context = ApplicationContext.run();
var model = context.getBean(UserModel.class);
var model2 = context.getBean(UserModel.class);

assertThat(model == model2).isFalse();
```

Like Spring Boot's `Configuration`, Micronaut provides a `@Factory` to group simple beans in a central configuration. For example.

```java
@Factory
class MyConfig{
    
    @Bean
    public Foo foo(){}
    
    @Bean
    public Bar bar(){}
}
```

## Build-time AOT 

When building the application, explore the project *build/classes* folder, there are some extra classes generated at compile time.

For example, when the above `UserModel` is compiled, there are two extra classes generated: `$UserModel$Definition` and `$UserModel$Definition$Reference`. These two classes incldue all metadata info of defining a bean and injecting a bean.

Open them in your IDE and have a look at the source codes that are already anti-compiled to Java.

```java
@Generated
class $UserModel$Definition extends AbstractInitializableBeanDefinition<UserModel> implements BeanFactory<UserModel> {
    private static final MethodOrFieldReference $CONSTRUCTOR = new MethodReference(UserModel.class, "<init>", (Argument[])null, (AnnotationMetadata)null, false);

    public UserModel build(BeanResolutionContext var1, BeanContext var2, BeanDefinition var3) {
        UserModel var4 = new UserModel();
        var4 = (UserModel)this.injectBean(var1, var2, var4);
        return var4;
    }

    protected Object injectBean(BeanResolutionContext var1, BeanContext var2, Object var3) {
        UserModel var4 = (UserModel)var3;
        return super.injectBean(var1, var2, var3);
    }

    public $UserModel$Definition() {
        this(UserModel.class, $CONSTRUCTOR);
    }

    protected $UserModel$Definition(Class var1, MethodOrFieldReference var2) {
        super(var1, var2, $UserModel$Definition$Reference.$ANNOTATION_METADATA, (MethodReference[])null, (FieldReference[])null, (ExecutableMethodsDefinition)null, (Map)null, Optional.of("io.micronaut.context.annotation.Prototype"), false, false, false, false, false, false, false, false);
    }
}

@Generated(
    service = "io.micronaut.inject.BeanDefinitionReference"
)
public final class $UserModel$Definition$Reference extends AbstractInitializableBeanDefinitionReference {
    public static final AnnotationMetadata $ANNOTATION_METADATA;

    static {
        DefaultAnnotationMetadata.registerAnnotationDefaults($micronaut_load_class_value_0(), AnnotationUtil.mapOf("typed", ArrayUtils.EMPTY_OBJECT_ARRAY));
        $ANNOTATION_METADATA = new DefaultAnnotationMetadata(AnnotationUtil.internMapOf("io.micronaut.context.annotation.Prototype", Collections.EMPTY_MAP), AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP), AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", Collections.EMPTY_MAP, "javax.inject.Scope", Collections.EMPTY_MAP), AnnotationUtil.internMapOf("io.micronaut.context.annotation.Prototype", Collections.EMPTY_MAP), AnnotationUtil.mapOf("io.micronaut.context.annotation.Bean", AnnotationUtil.internListOf(new Object[]{"io.micronaut.context.annotation.Prototype"}), "javax.inject.Scope", AnnotationUtil.internListOf(new Object[]{"io.micronaut.context.annotation.Prototype"})), false, true);
    }

    public $UserModel$Definition$Reference() {
        super("com.example.UserModel", "com.example.$UserModel$Definition", $ANNOTATION_METADATA, false, false, false, false, false, false, false, false);
    }

    public BeanDefinition load() {
        return new Definition();
    }

    public Class getBeanDefinitionType() {
        return Definition.class;
    }

    public Class getBeanType() {
        return UserModel.class;
    }
}
```
At runtime, Micornaut uses these two classes to define a bean named `UserModel` in the `prototype` scope and make it available in the `ApplicationContext`. When other beans inject it, Micronaut uses them to produce a new instance for use. In the whole progress, it does not invoke the Java reflection APIs to get the metadata info of `UserModel`.


