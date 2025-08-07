# payment_gateway

## install 
```bash
mvn clean install
```


## start 
```bash
mvn spring-boot:run
```

## api doc 
```bash
http://localhost:8080/swagger-ui/index.html
```

## Perform 3 concurrent tests when payment method is B
```bash
 ./mvnw test -Dtest=PaymentServiceTest
```