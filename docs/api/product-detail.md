# Product Detail API (`v2/product/{productId}`)

## Endpoint
- Method: `GET`
- URL: `https://api.digikala.com/v2/product/{productId}/`
- Example: `https://api.digikala.com/v2/product/17436974/`

## Parameters
- Path:
  - `productId` (`Long`, required): Digikala product id.
- Query:
  - none for this iteration.

## Example Response (Trimmed)
```json
{
  "status": 200,
  "data": {
    "product": {
      "id": 17436974,
      "title_fa": "هدفون بلوتوثی سونی مدل ULT WEAR",
      "breadcrumb": [{ "title": "دیجی‌کالا" }, { "title": "کالای دیجیتال" }],
      "rating": { "rate": 88, "count": 10 },
      "comments_count": 7,
      "questions_count": 3,
      "variants_images": {
        "main": { "webp_url": ["..."] },
        "list": [{ "webp_url": ["..."] }]
      },
      "variants": [
        {
          "id": 76537017,
          "color": { "id": 2, "title": "سفید", "hex_code": "#FFFFFF" },
          "seller": { "title": "دیجی‌کالا" },
          "warranty": { "title_fa": "..." },
          "shipment_methods": { "providers": [{ "type": "jet", "delivery_day": "today" }] },
          "price": {
            "selling_price": 189990000,
            "rrp_price": 236683000,
            "discount_percent": 20,
            "timer": 13916,
            "badge": { "title": "پیشنهاد شگفت انگیز" }
          }
        }
      ],
      "specifications": [
        {
          "title": "مشخصات",
          "attributes": [{ "title": "نوع هدفون", "values": ["..."] }]
        }
      ]
    }
  }
}
```

## App UI Mapping
- `product.title_fa` -> product title block.
- `product.breadcrumb[].title` -> breadcrumb strip.
- `product.variants_images.main/list` + `product.images` -> media gallery.
- `product.rating.rate` + `product.rating.count` -> rating chip (`rate / 20`).
- `product.comments_count` + `product.questions_count` -> fallback meta counts.
- `product.variants[]` -> selectable variants/colors.
- `variants[].seller.title` -> seller text.
- `variants[].warranty.title_fa` -> warranty text.
- `variants[].shipment_methods` -> shipping quick-info pill.
- `variants[].price.selling_price` -> sticky buy price.
- `variants[].price.rrp_price` + `discount_percent` -> old price + discount badge.
- `variants[].price.badge.title` + `timer` -> offer header.
- `product.specifications[].attributes[]` -> specifications preview.

## Error Modes and Retry Guidance
- `4xx`: invalid product id or unavailable product. Show friendly error and back option.
- `5xx` or timeout: use cached snapshot if present; otherwise show retry CTA.
- Parse mismatch/missing fields: render partial UI using safe fallbacks (`-`, hidden sections).

Retry strategy:
1. First failure: immediate cached fallback.
2. User retry: manual retry button.
3. Optional backoff for repeated transient failures: `1s -> 2s`.

## Cache Policy
- Key: `product_{productId}_detail_json`.
- Strategy: `stale-while-revalidate`.
  - Render cached snapshot immediately.
  - Fetch fresh payload in background and patch UI.
- TTL guideline for this iteration: up to `15 minutes` considered fresh for price-sensitive content; stale data is still shown until network refresh completes.
