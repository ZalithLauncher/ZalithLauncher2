# Mirai Launcher website

A static site for the launcher. No build step, no dependencies, no framework — plain HTML, one
stylesheet and one small script. It is published to GitHub Pages by
[`.github/workflows/deploy_pages.yml`](../.github/workflows/deploy_pages.yml) on every push to
`Mirai-launcher` that touches `website/`.

## Pages

| File | Purpose |
| --- | --- |
| `index.html` | Home — hero, why Mirai, screenshots, renderer teaser, privacy, download |
| `features.html` | The full feature list, grouped |
| `renderers.html` | The renderer lineup, the two-LTW split, how selection works |
| `guide.html` | Install and first-launch walkthrough, tuning, troubleshooting |
| `faq.html` | 14 common questions |
| `changelog.html` | Release notes, currently 1.0.0 |

## Theming

Visitors pick their own theme, stored in `localStorage` and applied before first paint so there
is no flash of the wrong colours.

- **Mode:** Light · Dark · System (System follows `prefers-color-scheme` and keeps following it
  if the OS changes while the page is open)
- **Accent:** Violet (default, matching the launcher) · Amber · Forest · Ocean · Rose

Both live in `assets/js/site.js`. The palettes are CSS custom properties in
`assets/css/style.css` — `[data-theme="light"]`, `[data-theme="dark"]` and `[data-accent="…"]`.
Adding an accent means one token block plus one `.swatch` button in each page's theme panel.

Motion is deliberately small: a float on the logo, a fade-up on scroll, and hover lifts. All of
it is disabled under `prefers-reduced-motion: reduce`.

## First-time setup

1. **Settings → Pages → Build and deployment → Source → GitHub Actions.**
   Without this the deploy job fails with a `404` from the Pages API — the site is not
   registered yet.
2. Run **Deploy website** once from the Actions tab, or merge something under `website/`.
3. Under **Custom domain**, enter the domain and save. GitHub writes the CNAME into the
   published site, so there is no CNAME file here to drift out of sync.
4. Point DNS at GitHub:
   - apex domain (`example.com`) → four `A` records to `185.199.108.153`, `185.199.109.153`,
     `185.199.110.153`, `185.199.111.153`
   - `www` or any subdomain → one `CNAME` record to `entitybrian69-bit.github.io`
5. Wait for the certificate, then tick **Enforce HTTPS**.

### Domain references in the HTML

Each page carries a `<link rel="canonical">` and Open Graph tags pointing at
`https://YOUR-DOMAIN/`. **These are placeholders.** Once the domain is settled, replace them so
search engines and link previews resolve properly:

```sh
cd website
grep -rl 'YOUR-DOMAIN' *.html | xargs sed -i 's|https://YOUR-DOMAIN|https://your.actual.domain|g'
```

`og:image` is currently a relative path. Social scrapers generally need it absolute — after
setting the domain, make it `https://your.actual.domain/assets/img/mirai-logo.png`.

## Local preview

Any static server works:

```sh
cd website
python3 -m http.server 8000
```

Then open <http://localhost:8000>. Opening `index.html` directly from the filesystem also works,
though the Google Fonts request needs a network connection.

## Editing

The header and footer are duplicated across the six pages on purpose — there is no templating
engine and no build step to hide it behind. If you change a nav item, change it in all six files'
header, mobile menu and footer. `grep -c 'aria-current' *.html` is a quick sanity check that
every page still marks itself as current exactly once.

Images in `assets/img/` are optimized copies of the originals in the repository's top-level
`assets/` directory. Screenshots are 1200px wide, the logo is 512px, and all of them are
metadata-stripped. Re-optimize after replacing a screenshot:

```sh
convert ../assets/screenshots/shot-home.jpg -resize 1200x -strip -interlace Plane -quality 82 \
  assets/img/shot-home.jpg
```

## Download links

Every download button points at
<https://github.com/entitybrian69-bit/Mirai-launcher/releases/latest> rather than at a specific
APK filename, so the site keeps working across releases without edits.

## Accessibility

Semantic landmarks, a skip link, visible focus rings, `aria-current` on the active nav item, and
`aria-pressed` on the theme controls. The theme popover closes on `Escape` and on an outside
click.
