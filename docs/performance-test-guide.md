# Guide Gatling — Projet `e-commerce-api`

Guide pas à pas pour exécuter les **tests de performance** sur **cette** application Spring Boot + Gradle.

> **Projet :** `e-commerce-api` · `com.ecommerce` · v1.0.0  
> **Stack :** Spring Boot 3.4.5 · Java 17 · Gradle 9.3 · H2 · Gatling 3.15.0.3

---

## Sommaire

1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Architecture Gradle](#2-architecture-gradle)
3. [Ce que Gatling teste dans ce projet](#3-ce-que-gatling-teste-dans-ce-projet)
4. [Prérequis](#4-prérequis)
5. [Installation Gatling (plugin Gradle)](#5-installation-gatling-plugin-gradle)
6. [Fichiers de configuration du projet](#6-fichiers-de-configuration-du-projet)
7. [Exécution pas à pas](#7-exécution-pas-à-pas)
8. [Résultats et rapport HTML](#8-résultats-et-rapport-html)
9. [Captures d'écran à produire](#9-captures-décran-à-produire)
10. [Dépannage](#10-dépannage)
11. [Commandes récapitulatives](#11-commandes-récapitulatives)

---

## 1. Vue d'ensemble du projet

Ce dépôt contient une **API REST e-commerce** Spring Boot avec :

| Composant | Détail |
|-----------|--------|
| Point d'entrée | `com.ecommerce.api.EcommerceApplication` |
| Base de données | H2 in-memory (`jdbc:h2:mem:ecommerce`) |
| Port HTTP | **8080** (`application.properties`) |
| Données de démo | 5 produits injectés au `bootRun` via `DataInitializer` |
| Tests unitaires/intégration | 35 tests JUnit (`.\gradlew.bat test`) |
| Tests de performance | 1 simulation Gatling (`EcommerceSimulation`) |

### Schéma d'exécution perf

```
Terminal 1                          Terminal 2
──────────                          ──────────
.\gradlew.bat bootRun        →      .\gradlew.bat :performance-tests:gatlingRun
(Spring Boot :8080)                 (70 requêtes HTTP simulées)
     │                                       │
     └─────────── Tomcat REST API ───────────┘
```

---

## 2. Architecture Gradle

Projet **multi-modules** : l'API et Gatling sont séparés volontairement.

```
e-commerce-api/
├── gradlew / gradlew.bat                 # Gradle 9.3.0 (wrapper)
├── settings.gradle.kts                   # include("performance-tests")
├── build.gradle.kts                      # Spring Boot + JaCoCo
├── src/
│   ├── main/java/com/ecommerce/api/
│   │   ├── EcommerceApplication.java
│   │   ├── config/DataInitializer.java   # 5 produits au démarrage
│   │   ├── domain/                       # Product, Order, OrderLine
│   │   ├── repository/
│   │   ├── service/
│   │   ├── web/                          # ProductController, OrderController
│   │   └── exception/
│   ├── main/resources/application.properties
│   └── test/                             # 10 classes, 35 tests JUnit
└── performance-tests/                    # Module Gatling (isolé)
    ├── build.gradle.kts
    └── src/gatling/
        ├── java/simulations/EcommerceSimulation.java
        └── resources/gatling.conf
```

### Pourquoi `performance-tests` est un module séparé ?

Le module racine embarque Spring Boot (Tomcat, JPA, etc.). Gatling utilise sa propre version de Netty. Les fusionner dans un seul `build.gradle.kts` provoque des erreurs (`NoClassDefFoundError`). D'où le sous-module dédié.

---

## 3. Ce que Gatling teste dans ce projet

### Endpoints API disponibles (Spring Boot)

| Méthode | Endpoint | Testé par Gatling |
|---------|----------|:-----------------:|
| `GET` | `/api/products` | ✅ |
| `GET` | `/api/products/{id}` | ✅ (id=1) |
| `POST` | `/api/products` | ❌ |
| `POST` | `/api/orders` | ✅ |
| `GET` | `/api/orders/{id}` | ✅ |
| `GET` | `/api/orders?customerEmail=` | ❌ |
| `POST` | `/api/orders/{id}/cancel` | ❌ |

### Produits injectés au `bootRun` (`DataInitializer`)

| ID | Nom | Prix | Stock |
|----|-----|------|-------|
| 1 | Laptop Pro | 1299.99 € | 50 |
| 2 | Wireless Mouse | 49.99 € | 200 |
| 3 | Mechanical Keyboard | 89.99 € | 120 |
| 4 | USB-C Hub | 39.99 € | 300 |
| 5 | Monitor 27" | 399.99 € | 75 |

> La simulation commande le **produit id=1** (`Laptop Pro`). Après plusieurs runs Gatling, le stock peut s'épuiser → redémarrer `bootRun`.

### Scénarios Gatling (`EcommerceSimulation.java`)

| Scénario | Charge | Requêtes HTTP |
|----------|--------|---------------|
| **Browse products** | 20 users sur 10 s | `List products` → pause 100 ms → `Get product by id` |
| **Create order flow** | 10 users simultanés | `List products for order` → `Create order` → `Get order` |

**Corps JSON de la commande simulée :**
```json
{
  "customerEmail": "perf-user@example.com",
  "items": [{"productId": 1, "quantity": 1}]
}
```

### Assertions configurées

| Assertion | Seuil |
|-----------|-------|
| Taux de succès global | **> 90 %** |
| P95 temps de réponse | **< 2000 ms** |

### Résultats obtenus sur ce projet (référence)

| Métrique | Valeur mesurée |
|----------|----------------|
| Requêtes totales | 70 |
| Succès | 100 % |
| P95 | ~26 ms |
| Rapport | `performance-tests/build/reports/gatling/ecommercesimulation-*/index.html` |

---

## 4. Prérequis

| Prérequis | Vérification |
|-----------|--------------|
| Java **17** | `java -version` |
| Projet ouvert | dossier `e-commerce-api` |
| Gradle wrapper | `gradlew.bat` présent (Gradle **9.3.0**) |
| 2 terminaux | un pour `bootRun`, un pour Gatling |
| Port **8080** libre | voir [§10 Dépannage](#10-dépannage) si occupé |

---

## 5. Installation Gatling (plugin Gradle)

Gatling **n'est pas installé manuellement**. Il est déclaré dans `performance-tests/build.gradle.kts` :

```kotlin
plugins {
    java
    id("io.gatling.gradle") version "3.15.0.3"
}
```

### Première compilation (télécharge Gatling)

```powershell
cd e-commerce-api
.\gradlew.bat :performance-tests:compileGatlingJava
```

**Attendu :** `BUILD SUCCESSFUL`

### Vérifier les tâches Gatling

```powershell
.\gradlew.bat :performance-tests:tasks --group=gatling
```

| Tâche | Rôle |
|-------|------|
| `:performance-tests:gatlingRun` | Lance `EcommerceSimulation` |
| `:performance-tests:compileGatlingJava` | Compile la simulation |
| `:performance-tests:gatlingClasses` | Compile simulation + `gatling.conf` |

---

## 6. Fichiers de configuration du projet

### `settings.gradle.kts` (racine)

```kotlin
rootProject.name = "e-commerce-api"
include("performance-tests")
```

### `build.gradle.kts` (racine — Spring Boot)

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.ecommerce"
version = "1.0.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

Tâches utiles :

| Commande | Rôle |
|----------|------|
| `.\gradlew.bat bootRun` | Démarre l'API |
| `.\gradlew.bat test` | 35 tests JUnit + JaCoCo |
| `.\gradlew.bat check` | Tests + couverture min. 85 % |

### `performance-tests/build.gradle.kts` (Gatling)

```kotlin
gatling {
    jvmArgs = listOf(
        "-server", "-Xms512m", "-Xmx1g",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
    )
    systemProperties = mapOf("baseUrl" to "http://localhost:8080")
}
```

| Paramètre | Valeur projet | Rôle |
|-----------|---------------|------|
| `baseUrl` | `http://localhost:8080` | Doit correspondre à `server.port` |
| `--add-opens` | 3 flags Java 17 | Obligatoire pour Gatling 3.15 |

### `src/main/resources/application.properties`

```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:ecommerce;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
springdoc.swagger-ui.path=/swagger-ui.html
```

| URL utile | Adresse |
|-----------|---------|
| API produits | http://localhost:8080/api/products |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Console H2 | http://localhost:8080/h2-console (user: `sa`, password: vide) |

### `performance-tests/src/gatling/resources/gatling.conf`

```hocon
gatling {
  core {
    outputDirectoryBaseName = "ecommerce-perf"
  }
  charting {
    indicators { lowerBound = 800; higherBound = 1200 }
  }
}
```

### `EcommerceSimulation.java` — URL lue par priorité

1. `-DbaseUrl=...` en ligne de commande
2. `systemProperties` dans `performance-tests/build.gradle.kts`
3. Défaut dans le code : `http://localhost:8080`

---

## 7. Exécution pas à pas

### Étape 1 — Compiler le projet (optionnel, première fois)

```powershell
cd e-commerce-api
.\gradlew.bat build -x test
```

### Étape 2 — Démarrer Spring Boot

**Terminal 1 :**

```powershell
.\gradlew.bat bootRun
```

**Logs attendus :**
```
:: Spring Boot ::                (v3.4.5)
Tomcat started on port 8080 (http)
Started EcommerceApplication
H2 console available at '/h2-console'
```

📸 Capture → `docs/screenshots/09-spring-boot-run.png`

> Ne pas fermer ce terminal.

### Étape 3 — Vérifier l'API

**Terminal 2 :**

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/products" -UseBasicParsing
```

**Attendu :** `StatusCode : 200` + JSON avec 5 produits (Laptop Pro, Wireless Mouse, …).

Navigateur : http://localhost:8080/api/products

📸 Capture → `docs/screenshots/10-api-response.png`

### Étape 4 — Lancer Gatling

**Terminal 2** (Spring Boot toujours actif) :

```powershell
.\gradlew.bat :performance-tests:gatlingRun
```

Port différent (ex. 9090) :

```powershell
.\gradlew.bat bootRun --args="--server.port=9090"
.\gradlew.bat :performance-tests:gatlingRun "-DbaseUrl=http://localhost:9090"
```

**Sortie attendue :**
```
Simulation simulations.EcommerceSimulation started...
Simulation simulations.EcommerceSimulation completed in ~9 seconds
Global: percentage of successful events is greater than 90.0 : true
Global: 95th percentile of response time is less than 2000.0 : true
BUILD SUCCESSFUL
```

📸 Capture → `docs/screenshots/11-gatling-run.png`

---

## 8. Résultats et rapport HTML

### Emplacement exact dans ce projet

```
performance-tests/build/reports/gatling/
└── ecommercesimulation-<timestamp>/
    ├── index.html
    ├── simulation.log
    ├── req_list-products--679476602.html
    ├── req_get-product-by--922495913.html
    ├── req_create-order-235397962.html
    └── req_get-order-1343287780.html
```

Exemple réel déjà généré :
```
performance-tests/build/reports/gatling/ecommercesimulation-20260524210717587/index.html
```

### Ouvrir le dernier rapport (PowerShell)

```powershell
$report = Get-ChildItem performance-tests\build\reports\gatling\ -Directory |
          Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

### Requêtes nommées dans le rapport

| Nom Gatling | Endpoint Spring Boot |
|-------------|---------------------|
| List products | `GET /api/products` |
| Get product by id | `GET /api/products/1` |
| List products for order | `GET /api/products` |
| Create order | `POST /api/orders` |
| Get order | `GET /api/orders/{id}` |

📸 Capture → `docs/screenshots/13-gatling-report.png`

---

## 9. Captures d'écran à produire

Dossier : `docs/screenshots/`

| # | Fichier | Contenu (projet e-commerce-api) |
|---|---------|----------------------------------|
| 1 | `01-java-version.png` | `java -version` → 17.x |
| 2 | `02-spring-boot-gradle-tasks.png` | `.\gradlew.bat tasks --group=application` |
| 3 | `03-gatling-compile.png` | `:performance-tests:compileGatlingJava` OK |
| 4 | `04-gatling-gradle-tasks.png` | Tâches groupe `gatling` |
| 5 | `05-spring-boot-build-gradle.png` | `build.gradle.kts` racine ouvert dans IDE |
| 6 | `06-gatling-build-gradle.png` | `performance-tests/build.gradle.kts` |
| 7 | `07-application-properties.png` | `server.port=8080` + config H2 |
| 8 | `08-gatling-simulation.png` | `EcommerceSimulation.java` |
| 9 | `09-spring-boot-run.png` | `bootRun` + `Started EcommerceApplication` |
| 10 | `10-api-response.png` | JSON 5 produits sur `/api/products` |
| 11 | `11-gatling-run.png` | `gatlingRun` BUILD SUCCESSFUL |
| 12 | `12-gatling-summary.png` | Tableau Global Information (70 req, 100 % OK) |
| 13 | `13-gatling-report.png` | Rapport HTML avec les 5 requêtes nommées |

---

## 10. Dépannage

| Problème | Cause (ce projet) | Solution |
|----------|-------------------|----------|
| `Port 8080 was already in use` | Autre app sur 8080 | `bootRun --args="--server.port=9090"` + `-DbaseUrl=http://localhost:9090` |
| `Simulation crashed` | API non démarrée | Lancer `bootRun` **avant** `gatlingRun` |
| Succès < 90 % | Stock produit 1 épuisé (H2) | Redémarrer `bootRun` (réinitialise H2 + 5 produits) |
| `Create order` en 400 | Stock insuffisant après plusieurs runs | Idem — redémarrer l'API |
| `NoClassDefFoundError: IoHandle` | Gatling dans module racine | Garder Gatling dans `performance-tests/` uniquement |
| `IllegalAccessException` | Java 17 sans `--add-opens` | Vérifier `performance-tests/build.gradle.kts` |
| Rapport introuvable | Run échoué | `Get-ChildItem performance-tests\build\reports\gatling\` |

### Différence profils Spring Boot

| Profil | Quand | H2 | DataInitializer |
|--------|-------|-----|-----------------|
| `default` | `bootRun`, Gatling | `ecommerce` | ✅ 5 produits |
| `test` | `.\gradlew.bat test` | `ecommerce-test` | ❌ (`@Profile("!test")`) |

Gatling teste toujours l'API en profil **default** via `bootRun`.

---

## 11. Commandes récapitulatives

```powershell
# ── Setup (première fois) ──
cd e-commerce-api
java -version
.\gradlew.bat :performance-tests:compileGatlingJava

# ── Tests Spring Boot (35 tests, JaCoCo 85 %) ──
.\gradlew.bat check

# ── Test de performance ──
# Terminal 1
.\gradlew.bat bootRun

# Terminal 2
Invoke-WebRequest -Uri "http://localhost:8080/api/products" -UseBasicParsing
.\gradlew.bat :performance-tests:gatlingRun

# Rapport HTML
$report = Get-ChildItem performance-tests\build\reports\gatling\ -Directory | Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

---

## Voir aussi

| Fichier | Description |
|---------|-------------|
| [README.md](../README.md) | Vue d'ensemble du projet |
| [performance-tests/README.md](../performance-tests/README.md) | Aide-mémoire Gatling |
| [performance-tests/src/gatling/java/simulations/EcommerceSimulation.java](../performance-tests/src/gatling/java/simulations/EcommerceSimulation.java) | Code simulation |
| [src/main/java/com/ecommerce/api/config/DataInitializer.java](../src/main/java/com/ecommerce/api/config/DataInitializer.java) | Produits de démo |
