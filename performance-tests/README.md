# Tests de performance — `e-commerce-api`

Simulation : **`EcommerceSimulation`** · Rapport : **`performance-tests/build/reports/gatling/`**

> Guide complet : [docs/performance-test-guide.md](../docs/performance-test-guide.md)

## Commandes (ce projet)

```powershell
# Terminal 1 — API Spring Boot (port 8080, 5 produits H2)
.\gradlew.bat bootRun

# Terminal 2 — Gatling (70 requêtes, 2 scénarios)
.\gradlew.bat :performance-tests:gatlingRun

# Dernier rapport HTML
$report = Get-ChildItem build\reports\gatling\ -Directory | Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

## Fichiers de ce projet

| Fichier | Contenu |
|---------|---------|
| `build.gradle.kts` | Spring Boot 3.4.5, JaCoCo, H2 |
| `performance-tests/build.gradle.kts` | Gatling 3.15.0.3, `baseUrl=8080` |
| `performance-tests/src/gatling/java/simulations/EcommerceSimulation.java` | Browse + Create order |
| `performance-tests/src/gatling/resources/gatling.conf` | Seuils graphiques |
| `src/main/resources/application.properties` | `server.port=8080` |
| `src/main/java/.../config/DataInitializer.java` | 5 produits (id=1 = Laptop Pro) |

## Endpoints testés par Gatling

- `GET /api/products`
- `GET /api/products/1`
- `POST /api/orders` (productId: 1, qty: 1)
- `GET /api/orders/{id}`

## Assertions

- Succès > 90 %
- P95 < 2000 ms

## Port alternatif

```powershell
.\gradlew.bat bootRun --args="--server.port=9090"
.\gradlew.bat :performance-tests:gatlingRun "-DbaseUrl=http://localhost:9090"
```
