# Product Comments API (`v1/product/{productId}/comments`)

## Endpoint
- Method: `GET`
- URL: `https://api.digikala.com/v1/product/{productId}/comments/?page=1`
- Example: `https://api.digikala.com/v1/product/17436974/comments/?page=1`

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
      "total_items": 7
    },
    "comments": [
      { "id": 123, "title": "..." }
    ]
  }
}
```

## App UI Mapping
- `data.pager.total_items` -> comments count chip (`دیدگاه`).
- `data.comments` list is not rendered in this iteration (count only).

## Error Modes and Retry Guidance
- `4xx`: invalid id or endpoint mismatch; fallback to `v2` count if present.
- `5xx`/timeout: keep cached count; do not block detail screen render.
- Empty pager: render `0` for comments chip.

Retry strategy:
1. Do not block initial detail content.
2. Retry only when user refreshes detail screen.

## Cache Policy
- Key: `product_{productId}_comments_count`.
- Strategy: lightweight supplemental fetch.
- TTL guideline: `15 minutes`; stale value is acceptable until refreshed.
