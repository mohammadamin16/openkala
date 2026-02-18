# Iteration 03 Plan: Product Details Screen From Home Product Cards

## Summary
Add product-details navigation from the home bottom product cards to a full-screen details route with fast first paint and cache-first data loading.
Target is to match the provided screenshots for the core detail experience while keeping non-critical actions as placeholders in this iteration.

Locked decisions from your inputs:
1. Entry point is only home bottom product cards.
2. Screen mode is full-screen route.
3. Transition can be simple fade/slide now (shared element is optional, later).
4. CTA is UI-only for now (no real cart action).
5. Interest/likes badge can be hidden for now.
6. Top actions: close works; cart/search/menu can be placeholders.
7. Meta chips show counts only.
8. Variant selection updates image + price + seller/warranty.
9. Prefetch starts on tap only.

## API Structure and Endpoint Mapping
Base API used: `https://api.digikala.com`

### 1. Product Detail (Primary)
- Endpoint: `GET /v2/product/{productId}/`
- Purpose: Full product page payload.
- Core fields used in this iteration:
  - Breadcrumb/category path
  - Product title and brand/category labels
  - Rating value and counts
  - Media gallery (main images, additional images/videos if available)
  - Color options and selected variant metadata
  - Pricing (final price, discount, previous price if available)
  - Seller/warranty/shipping summary
  - Feature/attributes/spec preview for first section

### 2. Comments Count (Meta chip source)
- Endpoint: `GET /v1/product/{productId}/comments/?page=1`
- Purpose: Extract total comments count for the “دیدگاه” chip.

### 3. Questions Count (Meta chip source)
- Endpoint: `GET /v1/product/{productId}/questions/?page=1`
- Purpose: Extract total Q&A count for the “پرسش و پاسخ” chip.

### API Usage Notes
1. `v2/product/{id}` is the source of truth for detail content.
2. `comments` and `questions` endpoints are lightweight supplemental calls for counts.
3. Unknown/nullable fields must be treated as optional with resilient fallbacks.

## Public API Documentation Requirement (Human-Readable)
Every endpoint used must have standalone docs that can be published separately.
Create docs under:
- `/Users/amin/Projects/openkala/docs/api/product-detail.md`
- `/Users/amin/Projects/openkala/docs/api/product-comments.md`
- `/Users/amin/Projects/openkala/docs/api/product-questions.md`

Each doc should include:
1. Endpoint + method + sample URL.
2. Required path/query params.
3. Example response (trimmed).
4. Field-by-field mapping to app UI.
5. Error modes and retry guidance.
6. Cache policy (TTL/stale strategy) for that endpoint.

## Architecture and Data Flow
1. User taps a product card on home.
2. App navigates immediately to detail route with passed `productId` and lightweight preview data (image/title/price snapshot from card) for instant first frame.
3. Detail ViewModel starts parallel loads:
- cache read (`productId` key)
- network `v2/product/{id}`
- network comments/questions counts
4. UI shows cached or preview content instantly, then patches in fresh sections as responses arrive.
5. Persist normalized detail payload for next open.

## Screen Composition (Screenshot-Core Scope)
1. Top app row:
- Close action functional.
- Menu/cart/search visible as placeholders.
2. Breadcrumb strip:
- From detail API category path.
3. Media area:
- Main product image pager.
- Indicator dots.
- Optional side badges shown only if data exists.
4. Offer/info card:
- Deal/timer text if available.
- Title, subtitle line, rating + counts chips.
- Shipping quick info pill.
5. Variant/color selector:
- Color chips with selected state.
- Selection updates image + price + seller/warranty block.
6. Specifications preview block:
- First visible spec rows and “view all” affordance placeholder.
7. Bottom sticky buy bar:
- Price breakdown and discount badge.
- “افزودن به سبد خرید” button as UI-only in this iteration.

## Performance and "Instant Feel" Requirements
1. Instant navigation on tap (do not wait for network before entering details).
2. First frame target: render preview/cached state under `200ms` on warm path.
3. Cache-first rendering:
- disk cache keyed by `productId`
- stale-while-revalidate update model
4. Image loading:
- prefetch hero image on tap
- use sized requests and memory/disk caching
5. Recomposition control:
- split screen into stable section composables
- update only affected sections when variant changes
6. Loading UX:
- section-level skeletons/placeholders instead of blocking full-screen loader

## Navigation and Motion
1. Route:
- `product/{productId}`
2. Transition (this iteration):
- enter: subtle fade + short slide
- exit/back: reverse transition
3. Shared element animation:
- optional future step; prepare stable image keys and transition names now

## Missing Inputs / Clarifications To Improve Accuracy
1. For top-row placeholder icons (menu/cart/search), confirm final actions for next iteration.
2. Confirm exact fallback text for unavailable seller/warranty fields.
3. Confirm whether the “پیشنهاد شگفت‌انگیز” timer should be real-time ticking or static snapshot in this iteration.
4. Provide additional screenshots for lower sections (full specs, seller box, similar products) if you want them in this same iteration.

## Test Plan
1. Contract tests:
- Parse and map `v2/product/{id}`, comments, questions fixtures.
2. UI tests:
- Tap home product card opens detail route.
- Core sections appear in RTL order.
- Variant selection updates dependent UI blocks.
3. Resilience tests:
- network fail + cache hit => cached content visible.
- network fail + no cache => graceful error with retry.
4. Performance tests:
- time-to-first-content in cold and warm paths.
- scroll smoothness for long detail content.

## Deliverables
1. New iteration document:
- `/Users/amin/Projects/openkala/iterations/iteration-03.md`
2. Existing docs organized in:
- `/Users/amin/Projects/openkala/iterations/iteration-01.md`
- `/Users/amin/Projects/openkala/iterations/iteration-02.md`
3. Next implementation phase will add detail route, ViewModel/repository, API docs files, and UI sections above.
