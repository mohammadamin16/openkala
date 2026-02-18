# Iteration 04: Search Entry Screen + Shared Search-Bar Transition + Hot Searches

## Summary
Implement a dedicated search-entry screen that opens when the home search bar is tapped, with a real shared-element transition for the search bar itself.

Data source for hot searches and live suggestions:
- `GET https://api.digikala.com/v1/autocomplete/` for `data.trends` (empty query)
- `GET https://api.digikala.com/v1/autocomplete/?q={query}` for `data.auto_complete`

Locked decisions:
1. Use real shared-element animation.
2. Tapping hot-search chip should fill query only (no results navigation yet).
3. Add live autocomplete with debounce in this iteration.

## Current State
1. Home search bar is local UI only in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`.
2. Navigation currently includes `home` and `product/{...}` in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`.
3. No search-specific repository/viewmodel/models exist yet.
4. Verified API contract:
- `/v1/autocomplete/` includes `trends`, `banner`, `promotion_banner`, `auto_complete`.
- `/v1/autocomplete/?q=...` includes live `auto_complete` suggestions.

## Scope
### In
1. Open a new search screen from home search bar tap.
2. Shared-element morph between home search bar and search screen top bar.
3. Show hot searches (`trends`) as chips.
4. Live suggestions while typing (`auto_complete`) with debounce.
5. Chip tap writes text into input and triggers suggestion fetch.

### Out
1. Search results listing route (`/v1/search/?q=...`) navigation.
2. Ad block implementation from screenshot.
3. Voice/image search features.

## Public Interfaces / Types to Add or Change
1. Navigation:
- Add route: `search-entry`
2. API interface:
- Add in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/network/DigikalaApiService.kt`:
  - `suspend fun getAutocomplete(@Query("q") q: String? = null): JsonObject`
3. Domain models:
- `SearchTrendItem(keyword: String, uri: String?)`
- `SearchSuggestionItem(keyword: String, uri: String?)`
- `SearchEntryData(trends: List<SearchTrendItem>, suggestions: List<SearchSuggestionItem>, banner: Banner?)`
4. Repository:
- `SearchRepository`
  - `suspend fun fetchHotTrends(): SearchEntryData`
  - `suspend fun fetchSuggestions(query: String): List<SearchSuggestionItem>`
5. UI state:
- `SearchEntryUiState(query, trends, suggestions, isLoadingTrends, isLoadingSuggestions, error)`
6. Home screen callback:
- Extend `HomeScreenRoute` with `onSearchClick: () -> Unit`

## Architecture and Data Flow
1. App starts on `home`.
2. User taps home search bar.
3. Navigate to `search-entry` using shared-element transition key (same key used in both screens).
4. `SearchEntryViewModel` loads initial hot trends from `/v1/autocomplete/` once.
5. Input behavior:
- Update local query on each keystroke.
- Debounce `300ms`, `distinctUntilChanged`.
- If query blank: show trends and clear suggestions.
- If query non-blank: call `/v1/autocomplete/?q={query}` and show suggestions.
6. Hot chip click:
- Set query from chip text.
- Trigger suggestions refresh.

## Shared Element Animation Design
1. Use Compose shared-transition API (experimental) with one shared key: `search_bar_shared`.
2. Home and search bars share shape/background/border/height tokens.
3. Motion spec:
- Duration `260ms`
- Easing `FastOutSlowIn`
4. Shared properties:
- Container bounds, corner radius, elevation.
5. Non-shared elements:
- Back arrow/title/chips fade-slide after `80ms` delay.

Fallback:
- If shared-transition API is not available with current compose setup, use route slide/fade while preserving visual parity.

## UI Composition Spec
1. Home search bar:
- Make full search container clickable.
- Keep current style; add shared-element wrapper + callback.
2. Search entry screen:
- Top row: back arrow + shared search field.
- Placeholder: `جستجو در همه کالاها`.
- Section title row: `جستجوهای پرطرفدار` + icon.
- Horizontal chips from `trends`.
- Suggestion list under input when query is non-empty.
- Ignore ad card for now.

## Error / Failure Modes
1. Initial hot trends fetch fails:
- Show empty trends state with retry.
2. Suggestion request fails:
- Keep previous suggestions and show subtle error text.
3. Rapid typing:
- Cancel previous requests (`flatMapLatest`).
4. Empty arrays:
- Show empty state; no crash.

## Caching / Performance Defaults
1. Keep last trends snapshot in-memory for session reuse.
2. Optional disk cache later (`search_trends_json` in DataStore).
3. Debounce: `300ms`.
4. Minimum query length: `1`.

## Files Expected to Change
1. `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`
2. `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`
3. `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/network/DigikalaApiService.kt`
4. New: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/repository/SearchRepository.kt`
5. New: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/model/SearchModels.kt`
6. New: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/mapper/SearchMappers.kt`
7. New: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/SearchEntryViewModel.kt`
8. New: `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/search/SearchEntryScreen.kt`

## Test Cases and Scenarios
1. Tap home search bar opens `search-entry`.
2. Shared-element transition runs without layout jump.
3. Hot trends load from `/v1/autocomplete/`.
4. Typing triggers debounced autocomplete requests.
5. New input cancels previous request.
6. Hot chip tap fills input and refreshes suggestions.
7. API errors render fallback states without crash.
8. RTL layout remains correct.
9. Transition and typing remain smooth.

## Assumptions and Defaults
1. Hot searches source is `/v1/autocomplete/` -> `trends`.
2. Chip tap does not navigate to result page in this iteration.
3. Ad/promo row is intentionally omitted.
4. Shared-element uses experimental compose API if compatible; otherwise fallback transition is acceptable.
