# Documentation — `e-commerce-api`

| Document | Contenu |
|----------|---------|
| **[rapport-test-performance.md](rapport-test-performance.md)** | **Rapport académique à rendre** — démarche, résultats, captures, analyse |
| [performance-test-guide.md](performance-test-guide.md) | Guide technique Gatling adapté au projet |
| [screenshots/](screenshots/) | Captures d'écran pour le rapport (13 figures) |

## Résumé guide performance

- Projet Gradle multi-modules : racine (Spring Boot) + `performance-tests` (Gatling)
- `bootRun` sur port **8080** → `gatlingRun` sur `:performance-tests:`
- Simulation `EcommerceSimulation` : 20+10 users, 70 requêtes, 5 endpoints REST
- Rapport : `performance-tests/build/reports/gatling/ecommercesimulation-*/index.html`
