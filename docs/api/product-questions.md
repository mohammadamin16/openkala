# Product Questions API (`v1/product/{productId}/questions`)

## Endpoint
- Method: `GET`
- URL: `https://api.digikala.com/v1/product/{productId}/questions/?page=1`
- Example: `https://api.digikala.com/v1/product/17436974/questions/?page=1`

## Parameters
- Path:
  - `productId` (`Long`, required): Digikala product id.
- Query:
  - `page` (`Int`, required in current usage): page number.

## Example Response (Trimmed)
```json
{
  "status": 200,
  "data": {
    "pager": {
      "current_page": 1,
      "total_pages": 1,
      "total_items": 3
    },
    "questions": [
      { "id": 456, "text": "..." }
    ]
  }
}
```

## App UI Mapping
- `data.pager.total_items` -> questions count chip (`پرسش و پاسخ`).
- `data.questions` list is not rendered in this iteration (count only).

## Error Modes and Retry Guidance
- `4xx`: invalid id/path; fallback to `v2` questions count when available.
- `5xx`/timeout: keep cached count and continue rendering detail page.
- Empty pager: render `0` for questions chip.

Retry strategy:
1. Keep count optional, never block page load.
2. Retry on user-initiated refresh.

## Cache Policy
- Key: `product_{productId}_questions_count`.
- Strategy: supplemental count fetch with stale fallback.
- TTL guideline: `15 minutes`.
