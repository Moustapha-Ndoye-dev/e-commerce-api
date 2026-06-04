<div align="center">

# Rapport de Test de Performance

**API REST E-Commerce · Gatling 3.15 · Spring Boot 3.4.5**

<br/>

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Gatling](https://img.shields.io/badge/Gatling-3.15-FF6E00?style=for-the-badge&logo=apache-groovy&logoColor=white)](https://gatling.io/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)

<br/>

**Mouhamad Moustapha Ndoye** · **Ndeye Madeleine Diallo**  
*Encadré par Keba Deme — 25 mai 2026*

<br/>

---

### Verdict : test concluant ✅

| **2 050** requêtes | **100 %** succès | **17 ms** moyenne | **10 ms** P95 | **34 req/s** |
|:---:|:---:|:---:|:---:|:---:|

*Simulation `EcommerceSimulation` — run `ecommercesimulation-20260603201436007` (vérifié le 3 juin 2026)*

</div>

---

<p align="center">

[Qu'est-ce que Gatling ?](#quest-ce-que-gatling-) ·
[Architecture](#architecture-du-projet) ·
[Configuration Spring Boot + Gatling](#configuration-spring-boot--gatling) ·
[Exécution pas à pas](#exécution-pas-à-pas) ·
[Résultats](#résultats) ·
[Analyse](#analyse) ·
[Commandes](#commandes)

</p>

---

## Qu'est-ce que Gatling ?

**Gatling** est un outil open source de **test de charge et de performance** pour applications web et API REST. Il simule des centaines ou milliers d'utilisateurs virtuels qui envoient des requêtes HTTP en parallèle, afin de mesurer :

| Métrique | Description |
|:---------|:------------|
| **Temps de réponse** | Latence min, moyenne, max, percentiles (P50, P95, P99) |
| **Débit** | Nombre de requêtes par seconde (req/s) |
| **Taux de succès** | Pourcentage de requêtes HTTP 2xx vs erreurs |
| **Stabilité** | Comportement de l'API sous charge progressive ou soutenue |

### Gatling vs JUnit

| | JUnit (tests unitaires/intégration) | Gatling (tests de performance) |
|:--|:--|:--|
| **Objectif** | Vérifier la **correction fonctionnelle** | Mesurer la **performance sous charge** |
| **Charge** | 1 requête à la fois | Des centaines d'utilisateurs simultanés |
| **Rapport** | Pass / Fail | Graphiques HTML, percentiles, débit |
| **Dans ce projet** | `.\gradlew.bat test` (35 tests) | `.\gradlew.bat :performance-tests:gatlingRun` |

### Concepts clés Gatling

| Concept | Rôle |
|:--------|:-----|
| **Simulation** | Classe Java qui décrit scénarios, charge et assertions (`EcommerceSimulation.java`) |
| **Scénario** | Enchaînement d'actions utilisateur (GET produits → POST commande…) |
| **Injection** | Profil de charge : `rampUsers(500)` = montée progressive, `constantUsersPerSec(15)` = trafic soutenu |
| **Assertion** | Seuil de validation automatique (ex. P95 < 5000 ms, succès > 90 %) |
| **Rapport HTML** | Généré automatiquement dans `performance-tests/build/reports/gatling/` |

> Documentation officielle : [docs.gatling.io](https://docs.gatling.io/)

---

## Architecture du projet

Ce dépôt est un projet **Gradle multi-modules** : l'API Spring Boot et Gatling sont volontairement **séparés**.

```
e-commerce-api/
├── build.gradle.kts                      # Spring Boot 3.4.5 + JaCoCo
├── settings.gradle.kts                   # include("performance-tests")
├── src/main/java/com/ecommerce/api/      # Contrôleurs REST, services, JPA
├── src/main/resources/application.properties
└── performance-tests/                    # Module Gatling (isolé)
    ├── build.gradle.kts                  # Plugin io.gatling.gradle
    └── src/gatling/
        ├── java/simulations/EcommerceSimulation.java
        └── resources/gatling.conf
```

### Pourquoi un module séparé ?

Spring Boot embarque **Tomcat** et **Netty** (via ses dépendances). Gatling utilise **sa propre version de Netty**. Les fusionner dans un seul `build.gradle.kts` provoque des conflits de classpath (`NoClassDefFoundError: IoHandle`). Le sous-module `performance-tests/` isole Gatling de l'API.

### Schéma d'exécution

```mermaid
flowchart LR
    T1[Terminal 1<br/>bootRun :8081] --> API[API Spring Boot<br/>Tomcat + H2]
    T2[Terminal 2<br/>gatlingRun] --> SIM[EcommerceSimulation]
    SIM -->|2 050 requêtes HTTP| API
    SIM --> RAP[Rapport HTML]
```

| Terminal | Commande | Rôle |
|:---------|:---------|:-----|
| **1** | `.\gradlew.bat bootRun` | Démarre l'API sur le port 8081 (laisser ouvert) |
| **2** | `.\gradlew.bat :performance-tests:gatlingRun` | Lance la simulation |

> Le port **8081** est configuré dans `application.properties` et dans `performance-tests/build.gradle.kts`. Gatling et l'API partagent le **même port** via `baseUrl`.

---

## Configuration Spring Boot + Gatling

### 1. Enregistrer le module Gatling — `settings.gradle.kts`

```kotlin
rootProject.name = "e-commerce-api"
include("performance-tests")
```

### 2. API Spring Boot — `build.gradle.kts` (racine)

Le module racine ne contient **pas** Gatling. Il déclare uniquement Spring Boot :

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    // ...
}
```

| Commande | Rôle |
|:---------|:-----|
| `.\gradlew.bat bootRun` | Démarre l'API sur le port configuré |
| `.\gradlew.bat test` | 35 tests JUnit + rapport JaCoCo |
| `.\gradlew.bat check` | Tests + couverture minimale 85 % |

### 3. Port et base de données — `application.properties`

```properties
server.port=8081
spring.datasource.url=jdbc:h2:mem:ecommerce;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

| URL | Adresse |
|:----|:--------|
| API produits | http://localhost:8081/api/products |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Console H2 | http://localhost:8081/h2-console |

Au démarrage (`bootRun`), `DataInitializer` injecte **5 produits de démo** en base H2 in-memory.

### 4. Plugin Gatling — `performance-tests/build.gradle.kts`

Gatling **n'est pas installé manuellement** : le plugin Gradle le télécharge automatiquement.

```kotlin
plugins {
    java
    id("io.gatling.gradle") version "3.15.0.3"
}

gatling {
    jvmArgs = listOf(
        "-server", "-Xms1g", "-Xmx2g",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
    )
    systemProperties = mapOf("baseUrl" to "http://localhost:8081")
}
```

| Paramètre | Rôle |
|:----------|:-----|
| `io.gatling.gradle` | Plugin officiel — ajoute les tâches `gatlingRun`, `compileGatlingJava` |
| `--add-opens` (×3) | **Obligatoire** avec Java 17 — ouvre des modules internes JVM pour Gatling 3.15 |
| `-Xms1g / -Xmx2g` | Mémoire allouée au moteur Gatling pendant le test |
| `baseUrl` | URL de l'API Spring Boot — **doit correspondre** au `server.port` |

**Priorité de `baseUrl` :**

1. `-DbaseUrl=...` en ligne de commande (prioritaire)
2. `systemProperties` dans `performance-tests/build.gradle.kts`
3. Défaut dans le code : `http://localhost:8081`

### 5. Configuration Gatling — `gatling.conf`

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

Ce fichier configure le nom des dossiers de rapport et les seuils visuels des graphiques HTML.

### 6. Simulation — `EcommerceSimulation.java`

La simulation décrit **ce que Gatling teste** et **comment la charge est injectée** :

| Scénario | Injection | Requêtes HTTP |
|:---------|:----------|:--------------|
| **Browse products** | 500 users / 60 s (ramp) | `GET /api/products` → pause → `GET /api/products/{id}` |
| **Create order flow** | 200 users / 45 s (ramp) | `GET /api/products` → `POST /api/orders` → `GET /api/orders/{id}` |
| **Sustained peak traffic** | 15 req/s / 30 s | `GET /api/products` (health check) |

**Assertions automatiques :**

| Assertion | Seuil |
|:----------|:------|
| Taux de succès global | > 90 % |
| P95 temps de réponse | < 5000 ms |

**Endpoints testés :**

| Méthode | Endpoint | Testé |
|:--------|:---------|:-----:|
| `GET` | `/api/products` | ✅ |
| `GET` | `/api/products/{id}` | ✅ |
| `POST` | `/api/orders` | ✅ |
| `GET` | `/api/orders/{id}` | ✅ |
| `POST` | `/api/products` | ❌ |
| `POST` | `/api/orders/{id}/cancel` | ❌ |

### 7. Tâches Gradle Gatling disponibles

```powershell
.\gradlew.bat :performance-tests:tasks --group=gatling
```

| Tâche | Rôle |
|:------|:-----|
| `:performance-tests:gatlingRun` | Compile et lance `EcommerceSimulation` |
| `:performance-tests:compileGatlingJava` | Compile uniquement la simulation |
| `:performance-tests:gatlingClasses` | Compile simulation + `gatling.conf` |

---

## Exécution pas à pas

> **Prérequis :** Java 17 · 2 terminaux PowerShell · port **8081** libre

---

### Étape 1 — Vérification de Java 17

```powershell
java -version
```

<p align="center">
<img src="docs/screenshots/01-java-version.png" alt="Figure 1 — Java 17" width="750"/>
<br/><em>Figure 1 — Version OpenJDK 17 (Temurin)</em>
</p>

---

### Étape 2 — Compilation Gatling (première fois)

Télécharge Gatling et compile la simulation :

```powershell
.\gradlew.bat :performance-tests:compileGatlingJava
```

<p align="center">
<img src="docs/screenshots/03-gatling-compile.png" alt="Figure 2 — Compilation Gatling" width="750"/>
<br/><em>Figure 2 — BUILD SUCCESSFUL après compilation de la simulation</em>
</p>

---

### Étape 3 — Démarrage de l'API Spring Boot

**Terminal 1** (laisser ouvert) :

```powershell
.\gradlew.bat bootRun
```

*(Le port **8081** est configuré dans `application.properties` — pas besoin de `--args`.)*

**Logs attendus :**
```
:: Spring Boot ::                (v3.4.5)
Tomcat started on port 8081 (http)
Started EcommerceApplication
```

<p align="center">
<img src="docs/screenshots/09-spring-boot-run.png" alt="Figure 3 — Spring Boot" width="750"/>
<br/><em>Figure 3 — Tomcat started on port 8081 · Started EcommerceApplication</em>
</p>

---

### Étape 4 — Vérification de l'endpoint REST

**Terminal 2** :

```powershell
(Invoke-WebRequest -Uri "http://localhost:8081/api/products" -UseBasicParsing).Content
```

**Attendu :** HTTP 200 + JSON avec 5 produits (Laptop Pro, Wireless Mouse…).

<p align="center">
<img src="docs/screenshots/10-api-response.png" alt="Figure 4 — Réponse API" width="750"/>
<br/><em>Figure 4 — HTTP 200 · JSON des 5 produits</em>
</p>

---

### Étape 5 — Exécution du test Gatling

**Terminal 2** (API toujours active, ~2 minutes) :

```powershell
.\gradlew.bat :performance-tests:gatlingRun
```

*(Le `baseUrl` par défaut pointe déjà vers `http://localhost:8081`.)*

**Sortie attendue :**
```
Simulation simulations.EcommerceSimulation started...
Simulation simulations.EcommerceSimulation completed in ~60 seconds
Global: percentage of successful events is greater than 90.0 : true
Global: 95th percentile of response time is less than 5000.0 : true
BUILD SUCCESSFUL
```

<p align="center">
<img src="docs/screenshots/11-gatling-run.png" alt="Figure 5 — Test Gatling" width="750"/>
<br/><em>Figure 5 — 2 050 requêtes · BUILD SUCCESSFUL · assertions OK</em>
</p>

---

### Étape 6 — Résumé terminal (Global Information)

Sortie console du run vérifié (`docs/screenshots/gatling-run-output-success.txt`) :

```
Simulation simulations.EcommerceSimulation completed in 60 seconds
> request count                    | 2,050 | 2,050 | -
> mean response time (ms)          |    17 |    17 | -
> response time 95th percentile    |    10 |    10 | -
> mean throughput (rps)            | 33.61 | 33.61 | -
Global: percentage of successful events is greater than 90.0 : true (actual : 100.0)
Global: 95th percentile of response time is less than 5000.0 : true (actual : 10.0)
BUILD SUCCESSFUL
```

<p align="center">
<img src="docs/screenshots/12-gatling-summary.png" alt="Figure 6 — Global Information" width="750"/>
<br/><em>Figure 6 — Tableau Global Information (requêtes, OK, percentiles)</em>
</p>

---

### Étape 7 — Rapport HTML Gatling

```powershell
$report = Get-ChildItem performance-tests\build\reports\gatling\ -Directory |
          Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

Exemple de run vérifié :

```powershell
start performance-tests\build\reports\gatling\ecommercesimulation-20260603201436007\index.html
```

<p align="center">
<img src="docs/screenshots/13-gatling-report-global.png" alt="Figure 7 — Rapport HTML" width="90%"/>
<br/><em>Figure 7 — Vue globale du rapport HTML Gatling</em>
</p>

---

### Dépannage rapide

| Problème | Cause | Solution |
|:---------|:------|:---------|
| `Port 8081 was already in use` | Port occupé | Changer `server.port` dans `application.properties` + `baseUrl` dans `performance-tests/build.gradle.kts` |
| `Simulation crashed` | API non démarrée | Lancer `bootRun` **avant** `gatlingRun` |
| Succès < 90 % | Stock produit épuisé (H2) | Redémarrer `bootRun` (réinitialise la base) |
| `NoClassDefFoundError: IoHandle` | Gatling dans module racine | Garder Gatling dans `performance-tests/` uniquement |
| `IllegalAccessException` | Java 17 sans `--add-opens` | Vérifier `performance-tests/build.gradle.kts` |

---

## Résultats

<div align="center">

**Run :** `ecommercesimulation-20260603201436007` · **Durée :** 1 min 15 s · **Date :** 3 juin 2026

| Métrique | Valeur |
|:---------|:------:|
| Requêtes totales | **2 050** |
| Succès | **100 %** |
| Erreurs | **0** |
| Temps min / moy / max | 1 ms / **17 ms** / 1 149 ms |
| P50 / P75 / P95 / P99 | 4 / 5 / **10** / 639 ms |
| Débit | **34 req/s** |

</div>

<p align="center">
<img src="docs/screenshots/13-gatling-report-global.png" alt="Rapport Gatling — vue globale" width="90%"/>
<br/><em>Rapport HTML Gatling — run `ecommercesimulation-20260603201436007`</em>
</p>

### Par endpoint

| Requête | Total | Moyenne | P95 | Max |
|:--------|:-----:|:-------:|:---:|:---:|
| List products | 500 | 8 ms | 16 ms | 137 ms |
| Get product by id | 500 | **4 ms** | 9 ms | 79 ms |
| Health check products | 450 | 11 ms | 24 ms | 135 ms |
| List products for order | 200 | 10 ms | 20 ms | 168 ms |
| Create order | 200 | 22 ms | 27 ms | 574 ms |
| Get order | 200 | 7 ms | 16 ms | 65 ms |

```mermaid
xychart-beta
    title "Temps de réponse moyen (ms)"
    x-axis ["List", "Get", "Health", "List order", "Create", "Get order"]
    y-axis "ms" 0 --> 25
    bar [8, 4, 11, 10, 22, 7]
```

---

## Analyse

<div align="center">

| Critère | Observation |
|:--------|:------------|
| **Fiabilité** | 2 050 requêtes, 0 erreur — API stable sous charge |
| **Latence** | P95 = 10 ms, largement sous le seuil de 5000 ms |
| **Débit** | 34 req/s avec 700 users injectés + trafic soutenu |
| **Point faible** | Quelques requêtes `Create order` jusqu'à 1 149 ms (écriture H2 + stock) |
| **Point fort** | P50 = 4 ms — la majorité des requêtes sont quasi instantanées |

<br/>

**Conclusion :** l'API **e-commerce-api** répond de manière **fiable et performante** sous charge simulée type production en local.

*Recommandation : tests complémentaires avec PostgreSQL/MySQL en environnement cloud.*

</div>

---

## Commandes

```powershell
# ── Setup (première fois) ──
java -version
.\gradlew.bat :performance-tests:compileGatlingJava

# ── Tests Spring Boot (35 tests, JaCoCo 85 %) ──
.\gradlew.bat check

# ── Test de performance ──
# Terminal 1 — API
.\gradlew.bat bootRun

# Terminal 2 — Vérification + Gatling
Invoke-WebRequest -Uri "http://localhost:8081/api/products" -UseBasicParsing
.\gradlew.bat :performance-tests:gatlingRun

# Rapport HTML (dernier run)
$report = Get-ChildItem performance-tests\build\reports\gatling\ -Directory |
          Sort-Object Name -Descending | Select-Object -First 1
start "$($report.FullName)\index.html"
```

<p align="center">

[Simulation Gatling](performance-tests/src/gatling/java/simulations/EcommerceSimulation.java) ·
[Captures d'écran](docs/screenshots/) ·
[Documentation Gatling](https://docs.gatling.io/)

</p>

---

<div align="center">

<br/>

**Mouhamad Moustapha Ndoye** · **Ndeye Madeleine Diallo** · *Keba Deme*

*Projet e-commerce-api — Université*

</div>
