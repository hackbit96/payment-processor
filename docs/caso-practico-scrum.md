# 📄 Caso Práctico - Formato Scrum

Este documento contiene las historias de usuario, criterios de aceptación, estimaciones y backlog propuesto para el reto técnico.

---

## 🟢 HU-001: Registro de orden

**Como** cliente externo  
**Quiero** enviar una orden al sistema  
**Para que** sea registrada y procesada de forma asíncrona

### ✅ Criterios de Aceptación
- La orden debe enviarse por un endpoint HTTP POST.
- La orden se debe publicar exitosamente en Azure Queue.
- El sistema debe responder con un identificador de orden y estado **202 Accepted**.

**Puntos de historia:** 3

---

## 🟢 HU-002: Procesamiento de orden

**Como** sistema de procesamiento  
**Quiero** consumir mensajes de la cola de órdenes  
**Para** registrar los datos en Cosmos DB

### ✅ Criterios de Aceptación
- El microservicio debe escuchar la cola.
- Validar el contenido de la orden.
- Guardar los datos estructurados en Cosmos DB.

**Puntos de historia:** 5

---

## 🟢 HU-003: Generación de auditoría

**Como** responsable de cumplimiento  
**Quiero** almacenar un archivo JSON con cada orden procesada  
**Para** auditar el comportamiento del sistema

### ✅ Criterios de Aceptación
- Al procesar una orden, debe generarse un JSON.
- El archivo debe guardarse correctamente en Azure Blob Storage.
- El nombre del archivo debe incluir el ID de la orden y un timestamp.

**Puntos de historia:** 3

---

## 🟢 HU-004: Documentación y pruebas

**Como** desarrollador  
**Quiero** tener documentación y pruebas automáticas  
**Para** facilitar validación y despliegue

### ✅ Criterios de Aceptación
- Codigo Fuente GitHub
- Pruebas Unitarias
- Diagrama de arquitectura
- Existe una colección Postman o documentación.
- Caso Práctico en Formato Scrum

**Puntos de historia:** 2

---

## 📌 Sprint Backlog (Sugerencia)

| Historia | Estimación | Sprint |
|----------|------------|--------|
| HU-001   | 3 pts      | 1      |
| HU-002   | 5 pts      | 1      |
| HU-003   | 3 pts      | 2      |
| HU-004   | 2 pts      | 2      |

---

**Total estimado:** 13 puntos  
**Duración sugerida por sprint:** 1 semana

---
