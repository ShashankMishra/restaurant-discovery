# Testing

```bash
./gradlew test
```

Dev Services pulls `mongo:7`. `%test` sets `discovery.ingest.enabled=false` so we don't load the CSV; integration tests insert what they need.

| Kind | Class | Mongo |
|---|---|---|
| Unit | `NaturalLanguageParserTest`, `RestaurantMapperTest` | no |
| Integration | `RestaurantResourceTest` | yes |
| Arch | `LayerRulesTest` (ArchUnit) | no |

One class:

```bash
./gradlew test --tests LayerRulesTest
```

## Reports (written every `./gradlew test`)

Gradle is set to always emit both:

| Report | Path |
|---|---|
| HTML (open this) | `build/reports/tests/test/index.html` |
| JUnit XML (CI) | `build/test-results/test/TEST-*.xml` |

There is one XML file per test class (resource, parser, mapper, ArchUnit). `build/` is gitignored; generate it locally.

No JaCoCo coverage report in this MVP — only pass/fail reports.

I did not add image-level `@QuarkusIntegrationTest` or a full Kaggle ingest test.
