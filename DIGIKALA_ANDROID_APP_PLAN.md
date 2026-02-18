# Digikala Native Android (Instant UX) - Implementation Plan

## Summary
Build a native Android app for Digikala with the same important shopping capabilities as the official app, but engineered for speed so interactions feel instant.

Chosen direction:
- Direct API integration from app to Digikala APIs (no backend/BFF in v1)
- Phased parity rollout (fast core first, full important-feature parity by final milestone)

## Project Definition
- Platform: Android native only
- Language/UI: Kotlin + Jetpack Compose
- UX target: content appears immediately from cache, then refreshes silently
- Product rule: no important user flow is removed in final parity release
- Design input: user-provided screenshots are the visual source of truth

## Why Performance Is Core
- E-commerce speed directly affects conversion, retention, and trust.
- "Instant-feeling" behavior is a hard requirement.
- Every screen should support cached-first rendering, smooth scroll, and low-jank transitions.
- Performance is enforced through measurable budgets and CI gates.

## Performance Budgets (Release Gates)
- Cold start on mid-range Android: p50 <= 1.2s, p95 <= 2.0s
- Cached screen first paint: <= 250ms after navigation
- Network-backed list first content: p50 <= 900ms, p95 <= 1.6s
- Scroll fluidity: >= 55 FPS on mid-range devices
- Jank frames: <= 3%
- Search keystroke-to-visible update: <= 150ms (with local cache assist)
- Image loading: immediate placeholder, progressive final image

## Architecture
- Pattern: Clean Architecture + feature modules
- Modules:
  - `app`
  - `core:network`
  - `core:database`
  - `core:cache`
  - `core:ui`
  - `core:common`
  - `feature:home`
  - `feature:search`
  - `feature:category`
  - `feature:product`
  - `feature:cart`
  - `feature:account`
  - `feature:checkout`
  - `feature:orders`
- Data strategy: L1 memory cache + L2 Room DB + L3 HTTP cache
- Fetch policy: cache-first + stale-while-revalidate for read-heavy flows
- Tech stack:
  - Kotlin, Coroutines/Flow
  - Jetpack Compose + Navigation
  - Hilt
  - Retrofit + OkHttp
  - Paging 3
  - Room
  - DataStore
  - Coil
- Performance tooling:
  - Baseline Profiles
  - Macrobenchmark
  - startup tracing
  - frame metrics

## API Structure (Direct Digikala APIs)
Use a versioned API layer in app code so upstream changes stay isolated.

### Endpoint groups
- Home:
  - `GET https://api.digikala.com/v1/`
- Search:
  - `GET https://api.digikala.com/v1/search/?q={query}&page={n}&sort={code}`
- Category listing:
  - `GET https://api.digikala.com/v1/categories/{slug}/search/?page={n}&sort={code}`
- Brand listing:
  - `GET https://api.digikala.com/v1/brands/{slug}/search/?page={n}&sort={code}`
- Product details:
  - `GET https://api.digikala.com/v2/product/{numeric_id}/`
- User bootstrap:
  - `GET https://api.digikala.com/v1/user/init/`
- Cart snapshot:
  - `GET https://api.digikala.com/v1/cart/`
- Cart mutation candidate:
  - `POST https://api.digikala.com/v1/cart/add/` (request contract to be finalized during implementation discovery)

### Internal client interfaces
- `HomeApi`
- `SearchApi`
- `CategoryApi`
- `BrandApi`
- `ProductApi`
- `SessionApi`
- `CartApi`

### Core app-side models/types
- `ProductSummary`
- `ProductDetail`
- `PriceInfo`
- `InventoryInfo`
- `CategoryResultPage`
- `SearchResultPage`
- `CartState`
- `UserSessionState`

## Separate Human-Readable API Documentation (Publishable)
Create standalone docs under:

`/Users/amin/Projects/openkala/docs/api/`

One markdown file per endpoint, for example:
- `/Users/amin/Projects/openkala/docs/api/home-v1.md`
- `/Users/amin/Projects/openkala/docs/api/search-v1.md`
- `/Users/amin/Projects/openkala/docs/api/category-search-v1.md`
- `/Users/amin/Projects/openkala/docs/api/brand-search-v1.md`
- `/Users/amin/Projects/openkala/docs/api/product-v2.md`
- `/Users/amin/Projects/openkala/docs/api/user-init-v1.md`
- `/Users/amin/Projects/openkala/docs/api/cart-v1.md`
- `/Users/amin/Projects/openkala/docs/api/cart-add-v1.md`

Each API markdown file must include:
- Purpose
- Method and URL
- Auth/session requirements (cookies/headers)
- Query/body parameters with examples
- Success response schema with real example
- Error cases and meanings
- Cache policy and freshness notes
- Known quirks and compatibility notes
- Last verified date
- Changelog

## Feature Rollout (Phased Parity)
- Phase 0:
  - API contract discovery
  - model mapping
  - caching skeleton
  - performance harness
- Phase 1:
  - Home
  - search
  - category/brand listing
  - product detail
  - fast navigation
  - image pipeline
- Phase 2:
  - Session init
  - cart read/write
  - account basics
  - persistence hardening
- Phase 3:
  - Checkout/order flows
  - remaining important parity features
- Phase 4:
  - Optimization sprint
  - parity audit
  - release hardening

## Tests and Scenarios
- Unit tests:
  - repository cache policy
  - pagination merge
  - mapper stability
- Integration tests:
  - API deserialization against recorded fixtures per endpoint
- UI tests:
  - critical flows (`home -> search -> product -> cart`)
- Performance tests:
  - startup
  - scroll
  - list load
  - image-heavy product page
- Failure tests:
  - offline launch
  - slow network
  - 5xx retries
  - malformed payload fallback
- Parity tests:
  - feature checklist against original app/website core journeys

## Acceptance Criteria
- App feels instant in browse/search/product flows under normal network
- Offline or weak network still shows useful cached content quickly
- No important shopping flow missing by final parity milestone
- API docs are publishable as standalone technical docs
- Performance budgets pass in CI before release candidate

## Assumptions and Defaults
- Digikala public endpoints remain reachable for direct app access
- API contracts may change; versioned adapters absorb drift
- UI screenshots will be provided for design parity
- Checkout/payment may require extra request-contract validation
- Persian RTL support is first-class from day one
