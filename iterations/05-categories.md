# Categories Tab (Cache-First, Native-Smooth) Implementation Plan

## Summary
Build a real two-tab app shell (`خانه` + `دسته‌بندی`) and add a full Categories screen modeled after Digikala’s categories UX:
- Cache-first render (instant if cached).
- Shimmer placeholders only when no cached data exists.
- Background refresh with a **top progress bar** when stale/network refresh is running.
- Right-side rail from live Digikala `mega_menu` root categories.
- Left-side accordion content from selected category groups, with **single-open** behavior.
- Item taps are functional now via opening returned web URL (for this iteration changed by your choice to **No-op for now**, so we keep tap feedback only).

## Source of Truth and Data Contract
Use Digikala dictionary endpoint (verified from live site traffic):
- `GET https://api.digikala.com/v1/dictionaries/?types[0]=mega_menu&hashes[0]=`
- Parse `data[0].data.data` as hierarchical menu tree:
  - Top-level nodes => right rail categories.
  - First-level children => left accordion sections.
  - Second-level children => grid/list items inside expanded section.

## Public API / Interface Changes
1. Network interface updates in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/network/DigikalaApiService.kt`
- Add:
  - `suspend fun getDictionaries(types[0], hashes[0]): JsonObject`
  - Or a typed dedicated function for mega menu dictionary.

2. New domain models in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/model/CategoryModels.kt`
- `CategoryTabItem(id, titleFa, code, iconToken, urlUri)`
- `CategorySection(id, title, row, column, children)`
- `CategoryLeafItem(id, title, imageUrl, urlUri, row, column)`
- `CategoriesScreenData(tabs, selectedTabId, sectionsByTabId)`

3. New mapper in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/domain/mapper/CategoryMappers.kt`
- Map raw dictionary JSON to stable UI models.
- Sort by `row_number`, then `column_number`.
- Null-safe URL/image extraction.

4. New cache store in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/cache/CategoriesCacheStore.kt`
- DataStore keys:
  - `mega_menu_json`
  - `mega_menu_hash`
  - `updated_at`

5. New repository in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/repository/CategoriesRepository.kt`
- `streamCategories(): Flow<CategoriesPayload>`
- Behavior:
  - Emit cache immediately if present.
  - Fetch network and emit fresh data.
  - On network fail with cache present: keep stale data + error event.
  - On network fail with no cache: hard error state.

6. New ViewModel in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/categories/CategoriesViewModel.kt`
- State:
  - `LoadingNoCache`
  - `Content(data, isRefreshing, isStale, transientMessage)`
  - `ErrorNoData(message)`
- Actions:
  - `onTabSelected(tabId)`
  - `onSectionToggle(sectionId)` (single-open rule)
  - `refresh()`

7. New UI screen in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/categories/CategoriesScreen.kt`
- Layout:
  - Shared top area (search-like bar visual parity).
  - Right fixed rail (vertical category tabs).
  - Left scrollable content panel.
  - Accordion sections; expanded section shows item grid (3 columns), circular image placeholders.
- Loading:
  - Full shimmer scaffold when `LoadingNoCache`.
  - Top linear progress bar when `Content + isRefreshing=true`.
- Taps:
  - For this iteration: visual press/ripple only (no navigation).

8. Navigation refactor in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/navigation/OpenKalaNavHost.kt`
- Add top-level routes:
  - `home`
  - `categories`
- Keep product/detail and search routes unchanged.
- Move bottom bar ownership from Home screen into a shared root shell so active tab state is real.

9. Home screen adjustment in `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`
- Remove local hardcoded bottom bar.
- Keep existing content and callbacks intact.

## UX and Performance Decisions (Locked)
- Cache-first: always.
- Shimmer: only when no cached data.
- Refresh indicator with cache present: **top progress bar**.
- Rail source: **live mega_menu**.
- Accordion: **single-open**.
- Stale cache + network fail: **show stale content + toast/snackbar + retry affordance**.
- Motion:
  - 180–220ms fade/slide for tab switches.
  - Crossfade for section expand/collapse and rail selection.
  - Preserve scroll position per selected rail category in-memory for smooth back-and-forth.

## Error and Offline Behavior
- First launch offline (no cache): full error screen with retry.
- Offline with cache: show cached content immediately; show stale notice/snackbar.
- Malformed node data: skip bad node, keep rendering remaining tree.
- Empty child list in section: show compact empty placeholder row (not crash).

## Testing Plan
1. Unit tests
- `/Users/amin/Projects/openkala/app/src/test/.../CategoryMappersTest.kt`
  - Hierarchy mapping, sort order, null safety.
- `/Users/amin/Projects/openkala/app/src/test/.../CategoriesRepositoryTest.kt`
  - Cache-first emission order.
  - Network success overwrite.
  - Stale fallback on failure.

2. ViewModel tests
- `/Users/amin/Projects/openkala/app/src/test/.../CategoriesViewModelTest.kt`
  - Initial state with/without cache.
  - Single-open accordion behavior.
  - Top progress bar toggling during refresh.
  - Stale error event emission.

3. UI tests
- `/Users/amin/Projects/openkala/app/src/androidTest/.../CategoriesScreenTest.kt`
  - Shimmer shown only in no-cache loading.
  - Right rail selection updates left content.
  - Accordion opens one section at a time.
  - Refresh bar appears during background refresh.
  - RTL layout correctness and smooth scrolling.

4. Manual performance checks
- Warm open to categories with cache: first content under ~250ms perceived.
- Scroll jank check on long section lists.
- Rapid rail switching without dropped frames/stutter.

## Assumptions and Defaults
- `mega_menu` dictionary remains publicly accessible and stable.
- Icon token strings in payload (`cube-cat-*`) are optional; fallback to image/placeholders if no direct icon mapping.
- Item tap behavior remains non-navigating for this iteration by explicit decision.
- Existing app architecture (Hilt + Retrofit + DataStore + Flow + Compose) is preserved; no module split in this iteration.
