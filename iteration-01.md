# First Screen Implementation Plan (Digikala Tab Live, Other Tabs Mocked)

## Summary
Implement the first/home screen UI from your screenshot as a native Compose screen with real Digikala data for the `دیجی‌کالا` tab, while keeping the other top tabs visible but non-functional (mocked/no-op).
Focus is instant rendering via cache-first loading and skeletons, with logged-out behavior only in this phase.

## Scope Locked From Your Decisions
1. No logged-in-dependent functionality now (cart add, account-specific actions can be deferred).
2. All top tabs must be shown.
3. Only `دیجی‌کالا` tab works now.
4. Other tabs are mocked and clicking them does nothing for now.
5. Location row is static `تحویل به استان تهران، شهر تهران`.
6. UI can be implemented with interpretation where details are missing.

## Screen Block -> API Mapping
1. Top strip promo banner:
`GET https://api.digikala.com/v1/`
Use `data.header_banners` (the “چرم مشهد” style strip is from this group).
2. Top service tabs (`سرویس‌ها`, `دیجی‌کالا`, `۴۵ دقیقه‌ای`, `طلای دیجیتال`, `سوپرمارکت`, ...):
`GET https://api.digikala.com/v1/super-app/pillars/`
Use `data.active_pillars`.
3. Search bar row:
UI component (local), no required remote payload for placeholder text.
4. Purple Plus status bar:
Mocked UI in this phase (real “subscription expired” state is user-session dependent).
5. Hero carousel:
`GET https://api.digikala.com/v1/`
Use `data.header_banners` entries with slider placement.
6. Round shortcut row (`ایکیا`, `طلای سرمایه‌گذاری`, `کالابرگ`, ...):
`GET https://api.digikala.com/v1/`
Use `data.deep_links`.
7. Incredible offers section (`شگفت‌انگیز` + timer + products):
`GET https://api.digikala.com/v1/`
Use `data.incredible_products`.
8. Bottom navigation:
Native static UI/navigation shell in this phase.

## Public Interfaces and Types To Add
1. `SuperAppTabsApi`
2. `HomeFeedApi`
3. `SuperAppPillarDto`, `HomeBannerDto`, `DeepLinkDto`, `IncredibleProductsDto`
4. `SuperAppTab`, `TopBanner`, `ShortcutItem`, `IncredibleOfferItem`
5. `HomeRepository`
6. `GetHomeScreenDataUseCase`
7. `HomeUiState` with `Loading`, `Content`, `Error`, `CachedContentRefreshing`

## Data Flow
1. On entry, load cached home payload immediately from Room/DataStore-backed snapshot.
2. Render first frame from cache/skeleton in under 250ms target.
3. In parallel, fetch fresh payload from `/v1/` and `/v1/super-app/pillars/`.
4. Diff and patch UI sections (avoid full-list recomposition where possible).
5. Persist normalized response for next launch.

## Mocking Rules (Explicit)
1. Tabs other than `دیجی‌کالا` are rendered using real API labels/icons/colors.
2. Clicking non-`دیجی‌کالا` tabs performs no navigation and no network call.
3. Purple Plus bar uses a local mocked state model for now.
4. Location line always shows static Tehran text in this phase.

## Performance Requirements for This Screen
1. Cache-first render path mandatory.
2. Use Coil with memory+disk caching and fixed-size image requests.
3. Use lazy lists with stable keys for all repeated sections.
4. Preload first visible banner and first row of offer images.
5. Keep recomposition scope small by splitting each block into isolated composables.

## Test Cases and Scenarios
1. Contract tests:
Validate parsing for `/v1/` and `/v1/super-app/pillars/` against saved fixtures.
2. UI tests:
Verify all screenshot sections appear in order and in RTL.
3. Interaction tests:
Verify only `دیجی‌کالا` tab is actionable; others are no-op.
4. Resilience tests:
Show cached content when network fails.
5. Performance tests:
Measure first render, banner load latency, and scroll jank in Macrobenchmark.

## Assumptions and Defaults
1. This phase is logged-out-only behavior by design.
2. Plus-expired banner is visual mock; real account state wiring is later.
3. Location is hardcoded Tehran now.
4. Non-digikala top tabs are present but intentionally inactive.
5. All text direction and layout are RTL-first.

## Optional Inputs You Can Still Provide (Non-Blocking)
1. Additional screenshots for the same screen in expanded/scroll states.
2. Preferred Persian typeface and exact spacing/radius tokens.
3. Exact copy for the mocked Plus status bar states.
