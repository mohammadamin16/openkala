# Search Results / Explore Screen (Post-Search PLP) Plan

## Summary
Implement a reusable search-results screen that opens after a submitted search query and supports:
- Sticky top area (search bar + filter/sort chips).
- Recommendation block above results (when available).
- One-column product rows with Digikala-like layout.
- Infinite pagination.
- Cache-first + stale-while-revalidate (SWR) behavior.
- Product click -> existing product-detail route, with back returning to the same search list state.

Locked decisions:
- Enter results on `Enter/Search` and on suggestion/trend tap.
- Infinite scroll pagination.
- Cache-first + SWR.
- Results top search bar is a launcher back to `search-entry` (not inline edit).
- Back arrow uses normal back stack.
- Bottom tabs remain visible on results screen.

---

## Verified API Contracts To Use
1. Global search results:
- `GET /v1/search/?page={n}&q={query}`

2. Category-scoped search results:
- `GET /v1/categories/{categoryCode}/search/?page={n}&q={query}`

3. Recommendation source:
- `GET /v1/autocomplete/?q={query}`
- Use `data.categories[]` (`keyword` + `category.code` + `category.title_fa`)

4. Recommendation click behavior (verified):
- Navigate to category-scoped search query, equivalent to:
  - URL shape: `/search/{categoryCode}/?q={keyword}`
  - API shape: `/v1/categories/{categoryCode}/search/?page=1&q={keyword}`

---

## Public Interfaces / Type Changes
1. Navigation routes in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`
- Add:
  - `search-results?query={query}&categoryCode={categoryCode}`
- Keep existing `search-entry` and product route.
- Ensure bottom bar is visible on `search-results`.

2. API methods in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/network/DigikalaApiService.kt`
- Add:
  - `suspend fun search(query: String, page: Int, sort: Int? = null): JsonObject`
  - `suspend fun categorySearch(categoryCode: String, query: String, page: Int, sort: Int? = null): JsonObject`
- Keep autocomplete call and reuse for recommendations.

3. New domain models in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/model/SearchResultsModels.kt`
- `SearchProductItem(id, title, imageUrl, rating, price, rrpPrice, discountPercent, shippingText, colorCount)`
- `SearchRecommendationItem(keyword, categoryCode, categoryTitle)`
- `SearchResultsHeader(totalItems, chips, selectedSortTitle, query, categoryCode?)`
- `SearchResultsPage(products, totalItems, currentPage, totalPages, recommendations)`
- `SearchResultsPayload(data, source, isStale, message?)`

4. New mapper in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/mapper/SearchResultsMappers.kt`
- Map `/v1/search` and `/v1/categories/{code}/search` response to `SearchResultsPage`.
- Normalize rating to UI scale (API percent -> 0..5 display).
- Extract recommendation list from autocomplete categories.

5. New cache store in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/cache/SearchResultsCacheStore.kt`
- DataStore keys for page-1 snapshot per query signature:
  - signature = `query|categoryCode|sort`
  - value = JSON + updatedAt
- Keep existing search trends cache untouched.

6. New repository in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/repository/SearchResultsRepository.kt`
- `streamFirstPage(query, categoryCode, sort): Flow<SearchResultsPayload>`
- `loadNextPage(query, categoryCode, sort, page): SearchResultsPage`
- Strategy:
  - L1 memory cache first.
  - L2 disk cache (page 1) second.
  - network refresh third.
  - pages 2+ network + memory merge (disk persistence optional for page1 only).

7. New ViewModel in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/results/SearchResultsViewModel.kt`
- State:
  - `LoadingNoCache`
  - `Content(items, header, isRefreshing, isAppending, hasMore, errorMessage?)`
  - `ErrorNoData`
- Events:
  - `load(query, categoryCode?)`
  - `loadNextPage()`
  - `onRecommendationClick(item)`
  - `retry()`
  - `onProductClick(item)`
- Preserve list state for back navigation.

8. Search-entry integration changes
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/SearchEntryScreen.kt`
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/SearchEntryViewModel.kt`
- Add submit callback from keyboard action and suggestion/trend tap navigation to `search-results`.

---

## UI Composition Plan
1. New screen:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/results/SearchResultsScreen.kt`

2. Structure:
- `LazyColumn` with `stickyHeader` containing:
  - Row A: back arrow + read-only shared-style search bar launcher.
  - Row B: filter chips row (UI-only for now): `جستجوی توصیفی`, `مرتبط‌ترین`, `فیلتر`, `محدوده قیمت`.
- Non-sticky body:
  - Optional recommendation block (`برای نتایج دقیق‌تر...`) shown before products.
  - Product rows list.
  - Append loader row.

3. Recommendation block behavior:
- Show when `autocomplete.categories` non-empty and current route is non-category search.
- Each row text:
  - `{keyword} در دسته {categoryTitle}`
- Click:
  - Reload results with `categoryCode=category.code` and `query=keyword`.

4. Product row click:
- Navigate via existing `OpenKalaDestinations.productRoute(ProductPreview(...))`.
- Back from detail naturally returns to same results scroll position.

---

## Data Flow
1. From `search-entry` submit/tap suggestion -> navigate to `search-results`.
2. `SearchResultsViewModel` loads:
- Cache emit (memory/disk) if available.
- Network refresh in background.
- In parallel, fetch autocomplete categories for recommendation block (if global search).
3. UI renders cached rows instantly, then patches with fresh rows.
4. Infinite scroll triggers `loadNextPage` when near list end.
5. Recommendation click reloads dataset with category-scoped endpoint.

---

## Error / Edge Cases
1. No cache + network fail -> full error state with retry.
2. Cache exists + network fail -> keep stale content + snackbar.
3. Empty results -> explicit empty-state panel.
4. Recommendation fetch fail -> hide recommendation block, keep products.
5. Re-query while previous load running -> cancel previous jobs and reset pagination.
6. Duplicate products across pages -> dedupe by product id.

---

## Testing Plan
1. Unit tests
- Mapper correctness for:
  - global search response.
  - category search response.
  - autocomplete category recommendations.
- Repository cache-first emission order.
- Pagination merge + dedupe logic.

2. ViewModel tests
- Initial load with memory cache.
- Initial load with disk cache only.
- Fresh network overwrite.
- Infinite paging state transitions.
- Recommendation click resets query/category and reloads page 1.

3. UI tests
- Search-entry submit navigates to results.
- Sticky header remains visible while scrolling.
- Recommendation block renders and click changes dataset.
- Product click opens detail; back returns to results list.
- Bottom tab bar remains visible on results route.

4. Build verification
- `./gradlew :app:compileDebugKotlin`
- Existing navigation flows still compile and route correctly.

---

## Assumptions / Defaults
1. Filter/sort chips are visual-only in this iteration (no functional filtering yet).
2. `providers-products` endpoint is out of scope for now; core list uses `/v1/search` and `/v1/categories/{code}/search`.
3. Cache persistence is guaranteed for first page; pages 2+ are session-memory cached.
4. Search bar in results launches back to `search-entry` for query editing (not inline edit).
5. Bottom tabs remain visible on results screen to match the requested UX direction.
