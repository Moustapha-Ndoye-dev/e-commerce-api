# E-Commerce API

API REST e-commerce en **Java 17** + **Spring Boot 3**, avec base **H2**, tests complets (unitaires + intégration) et tests de performance **Gatling**.

---

## Fonctionnalités

| Fonctionnalité | Description | Endpoint |
|----------------|-------------|----------|
| Lister les produits | Retourne le catalogue complet | `GET /api/products` |
| Détail produit | Récupère un produit par ID | `GET /api/products/{id}` |
| Créer un produit | Ajoute un produit au catalogue | `POST /api/products` |
| Créer une commande | Valide le stock, calcule le total, confirme la commande | `POST /api/orders` |
| Détail commande | Récupère une commande par ID | `GET /api/orders/{id}` |
| Historique client | Liste les commandes d'un client | `GET /api/orders?customerEmail=` |
| Annuler une commande | Passe le statut à `CANCELLED` | `POST /api/orders/{id}/cancel` |
| Documentation API | Swagger UI interactif | `/swagger-ui.html` |
| Console H2 | Explorateur SQL (dev) | `/h2-console` |

### Règles métier

- Décrémentation automatique du **stock** à la création de commande
- Rejet si stock insuffisant (`400 Bad Request`)
- Commande confirmée automatiquement après validation
- Validation des entrées (`@Valid`) : email, prix, quantités
- Gestion centralisée des erreurs (`404`, `400`, `500`)

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 17 |
| Framework | Spring Boot 3.4 |
| Persistance | Spring Data JPA |
| Base de données | H2 (in-memory) |
| Validation | Jakarta Validation |
| Documentation | SpringDoc OpenAPI |
| Tests | JUnit 5, Mockito, MockMvc |
| Couverture | JaCoCo (seuil min. 85 %) |
| Performance | Gatling 3.15 |

---

## Architecture

```
src/main/java/com/ecommerce/api/
├── domain/          # Entités JPA (Product, Order, OrderLine)
├── repository/      # Accès données H2
├── service/         # Logique métier
├── web/             # Controllers REST + DTOs + Mappers
├── exception/       # Gestion globale des erreurs
└── config/          # Initialisation des données (dev)

performance-tests/   # Simulations Gatling (module isolé)
```

---

## Démarrage rapide

### Prérequis

- Java 17+
- Gradle (wrapper inclus)

### Lancer l'API

```bash
.\gradlew.bat bootRun
```

L'API démarre sur **http://localhost:8080** avec 5 produits de démo.

### Console H2

- URL : `http://localhost:8080/h2-console`
- JDBC : `jdbc:h2:mem:ecommerce`
- User : `sa` / Password : *(vide)*

---

## Tests

### Exécuter tous les tests + couverture

```bash
.\gradlew.bat check
```

Génère le rapport JaCoCo : `build/reports/jacoco/test/html/index.html`

### Résultats actuels

| Métrique | Valeur |
|----------|--------|
| **Tests totaux** | **35** |
| **Statut** | Tous passent |
| **Couverture lignes** | **~95 %** (seuil min. 85 %) |

---

## Couverture des tests

### Tests unitaires (19 tests)

| Classe testée | Fichier | Couvert |
|---------------|---------|:-------:|
| `Product` | `ProductTest` | ✅ |
| `Order` | `OrderTest` | ✅ |
| `OrderLine` | `OrderLineTest` | ✅ |
| `ProductService` | `ProductServiceTest` | ✅ |
| `OrderService` | `OrderServiceTest` | ✅ |
| `ProductMapper` | `ProductMapperTest` | ✅ |
| `OrderMapper` | `OrderMapperTest` | ✅ |

**Scénarios couverts :**
- Gestion du stock (réduction, stock insuffisant, quantité invalide)
- Cycle de vie commande (ajout lignes, confirmation, annulation)
- Services : CRUD, exceptions métier et 404
- Mappers : mapping entité → DTO

### Tests d'intégration (15 tests)

| Endpoint | Fichier | Couvert |
|----------|---------|:-------:|
| `GET/POST /api/products` | `ProductControllerIntegrationTest` | ✅ |
| `GET/POST /api/orders` | `OrderControllerIntegrationTest` | ✅ |
| Contexte Spring + H2 | `EcommerceApplicationTest` | ✅ |

**Scénarios couverts :**
- Persistance réelle en base H2 (profil `test`)
- Codes HTTP : `200`, `201`, `400`, `404`
- Validation des requêtes invalides
- Flux complet : créer produit → commander → consulter → annuler

### Tests de performance — Gatling (1 simulation)

| Scénario | Description |
|----------|-------------|
| Browse products | 20 utilisateurs sur 10 s — liste + détail produit |
| Create order flow | 10 utilisateurs — liste, création commande, consultation |

**Derniers résultats :**

| Métrique | Valeur |
|----------|--------|
| Requêtes totales | 70 |
| Succès | **100 %** |
| P95 temps de réponse | **26 ms** |
| Assertions Gatling | Toutes passées |

```bash
# 1. Démarrer l'API
.\gradlew.bat bootRun

# 2. Lancer Gatling (autre terminal)
.\gradlew.bat :performance-tests:gatlingRun
```

**Guide pas à pas complet (commandes + captures d'écran) :** [docs/performance-test-guide.md](docs/performance-test-guide.md)

**Rapport académique à rendre (prof) :** [docs/rapport-test-performance.md](docs/rapport-test-performance.md)

Rapport HTML Gatling : `performance-tests/build/reports/gatling/`

> Option : `-DbaseUrl=http://localhost:8080` si le port diffère.

---

## Exemples de requêtes

### Créer un produit

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Casque Audio","description":"Bluetooth ANC","price":149.99,"stock":30}'
```

### Créer une commande

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"client@example.com","items":[{"productId":1,"quantity":2}]}'
```

### Lister les commandes d'un client

```bash
curl "http://localhost:8080/api/orders?customerEmail=client@example.com"
```

---

## Structure du projet

```
e-commerce-api/
├── build.gradle.kts              # App principale + JaCoCo
├── performance-tests/            # Module Gatling
│   └── src/gatling/java/simulations/
├── src/main/
│   ├── java/com/ecommerce/api/
│   └── resources/application.properties
└── src/test/
    ├── java/                     # 10 classes de test, 35 tests
    └── resources/application-test.properties
```

---

## Licence

Projet éducatif — libre d'utilisation.
