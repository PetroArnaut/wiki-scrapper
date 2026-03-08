# Wiki Scrapper — Implementation Notes

## What Was Done

- Traverses all wiki pages from a root link, following page links recursively until the full graph is visited, saving each page to the DB — cycle-safe
- `HtmlWikiClient` and `JsonWikiClient` both implement `WikiPageReader` and are profile-gated (`html & !json`, `json & !html`)
- No default profile — app fails fast on startup if neither is set
- Error handling via `GlobalExceptionHandler`:
  - `null/blank` root → `IllegalArgumentException` → `400`
  - missing page → `WikiPageNotFound` → `404`
  - unexpected errors → `500`

## Tests

- Domain unit tests: full graph traversal, cycle deduplication, trailing slash normalization, single page, null/blank root, null-returning reader
- Integration test (`@SpringBootTest @ActiveProfiles("json")`): full stack scrape using real JSON classpath fixtures, verifies all pages saved via `@SpyBean`
- `404` verified via integration test with negative assertion (`verify never save`)

## Not Implemented

- Dedicated `@WebMvcTest` for HTTP contract (`200`, `404`, `400`) in isolation

## How to Run

```bash
# Build all modules
mvn clean install

./mvnw spring-boot:run -pl app -Dspring-boot.run.profiles=html
./mvnw spring-boot:run -pl app -Dspring-boot.run.profiles=json
```

Endpoint: `POST http://localhost:8080/wiki/scrap`
