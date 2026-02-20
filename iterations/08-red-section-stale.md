## Iteration 08 Plan: Fix Red Home Section Staleness (Retry + 6h Section TTL)

### Summary
Investigation result:
1. The API key for red section is still correct: `data.incredible_products` (not renamed).
2. Main staleness risk is refresh flow: home refresh is all-or-nothing across 3 endpoints (`/v1/`, pillars, widget66). If any one fails, app keeps cached home.
3. Cache has no freshness limit, so stale red products can persist indefinitely when refresh keeps failing.

Goal:
1. Keep your chosen strategy: retry full refresh.
2. Prevent stale red section specifically after 6 hours.
3. Leave other home sections visible from cache.

Locked decisions:
1. Retry full refresh attempts: **3**.
2. Red section stale policy: **hide red section** if cache age > 6h and network refresh still fails.
3. Keep all-or-nothing fetch semantics (after retries).

---

### API / Data Reality (Grounded)
Source endpoint: `GET https://api.digikala.com/v1/`
- Red section key exists and is current: `data.incredible_products`
- Fresh green key: `data.fresh_incredible_products`
- No key mismatch found.

---

### Important Interface / Type Changes
No public API contract change required.

Internal additions:
1. In repository layer, introduce red-section freshness policy function using cache `updatedAt`.
2. Optionally add internal helper constants:
- `HOME_REFRESH_MAX_ATTEMPTS = 3`
- `INCREDIBLE_MAX_CACHE_AGE_MS = 6 * 60 * 60 * 1000L`

No new endpoint required.

---

### Implementation Plan

#### 1) Add retry loop for full home refresh
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/repository/HomeRepository.kt`

Behavior:
1. Keep fetching all 3 endpoints together (`home`, `pillars`, `widget66`) per attempt.
2. Retry whole bundle up to 3 attempts on failure.
3. If any attempt succeeds:
- write cache
- emit `DataSource.NETWORK`
4. If all 3 attempts fail:
- fallback to cached emission path (already exists).

Implementation detail:
- Add small retry helper (local private function) with fixed attempts and small delay backoff (e.g. 250ms, 500ms).

#### 2) Apply red-section-only stale suppression
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/data/repository/HomeRepository.kt`

Behavior:
1. On cached emit, compute age from `CacheSnapshot.updatedAt`.
2. If cache age > 6h:
- replace `data.incredibleOffers` with empty items before emit.
- keep all other sections untouched.
3. If age <= 6h:
- emit cache normally.

This enforces “prevent stale red section only.”

#### 3) Ensure UI fully hides red section when empty
File:
- `/Users/amin/Projects/openkala/app/src/main/java/com/openkala/app/ui/home/HomeScreen.kt`

Behavior:
1. In `IncredibleSection(...)`, add early return:
- `if (items.isEmpty()) return`
2. This avoids showing empty red header/container when repository suppresses stale section.

---

### Data Flow After Fix
1. App starts:
- reads cache
- if cache older than 6h, cached red section is hidden
- other cached sections still render immediately
2. Repository runs network refresh with 3 attempts.
3. If success:
- fresh network red section replaces cache state
4. If all retries fail:
- app stays on cached content, but red section remains hidden if cache too old.

---

### Test Cases and Scenarios

#### Repository tests (new)
File:
- `/Users/amin/Projects/openkala/app/src/test/java/com/openkala/app/data/repository/HomeRepositoryTest.kt`

Cases:
1. `incredible_products` mapping still works from fresh network data.
2. Cached age <= 6h + network failure -> cached red section still shown.
3. Cached age > 6h + network failure -> red section suppressed (empty) while other sections remain.
4. Network fails first attempts and succeeds on 3rd -> emits network result and updates cache.
5. All 3 attempts fail with no cache -> emits error (existing behavior).

#### UI tests
File:
- `/Users/amin/Projects/openkala/app/src/androidTest/java/com/openkala/app/ui/home/...`

Cases:
1. `IncredibleSection` hidden when items empty.
2. Home screen does not leave red empty shell when stale suppression applied.

---

### Failure Modes and Handling
1. widget66 keeps failing:
- retries attempted
- no fresh home emission
- stale red hidden after 6h threshold
2. total offline + very old cache:
- home still usable via cache except red section hidden
3. successful refresh after suppression:
- red section automatically returns with fresh data.

---

### Assumptions and Defaults
1. `incredible_products` remains the canonical source for red section.
2. 6-hour threshold is strict and applies only to red section.
3. Retry strategy is fixed 3 attempts per refresh call (no user-visible setting).
4. Green section and other home blocks are not suppressed by this policy.
