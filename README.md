# Payment Processor

## Descripción
Microservicio para el reto técnico Interbank utilizando Java 17, Spring WebFlux y Azure SDKs.

## Ejecutar localmente

```bash
mvn clean spring-boot:run
```

## Construir imagen Docker

```bash
docker build -t payment-processor:latest .
docker run -p 8082:8080 payment-processor
```
