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

## Deploying

1. **Settings → Pages → Build and deployment → Source → GitHub Actions.** ✅ already set
   Without this the deploy job fails with a `404` from the Pages API — Pages is not registered yet.
2. Run **Deploy website** from the Actions tab, or push a change under `website/` to
   `Mirai-launcher`. To publish without merging first, choose this branch in the run dialog —
   `workflow_dispatch` accepts any branch.

## The address

The site lives at:

**<https://entitybrian69-bit.github.io/Mirai-launcher/>**

That is free, permanent, served over HTTPS, and needs no purchase or renewal. Every page's
`<link rel="canonical">` and Open Graph tags already point at it, so link previews on Discord,
Twitter and the rest resolve correctly.

### If you ever want a shorter address

A custom domain is optional and can be attached at any time without touching the HTML content —
only the canonical and OG tags need updating (see below). Free routes that actually work:

| Route | How | Notes |
| --- | --- | --- |
| **`<name>.is-a.dev`** | Open a PR adding a JSON file to the [is-a.dev](https://github.com/is-a-dev/register) repo | Free subdomain for developers. Points straight at GitHub Pages. Days, not weeks. |
| **`<name>.js.org`** | PR to [js.org](https://github.com/js-org/js.org) | Free and well known, but **only for JavaScript projects**. This launcher is Kotlin and Java, so it would be a dishonest fit. |
| **`<name>.eu.org`** | Application form | Free, but approval is slow and unreliable — often weeks, sometimes never. |

One warning if you go the subdomain route: you are renting the name from someone else's
repository. If that project ever changes its rules, the address goes away. The
`github.io` URL above cannot be taken from you, which is why it is the default here.

### Attaching a domain later

1. **Settings → Pages → Custom domain**, enter it and save. GitHub writes the CNAME into the
   published site itself, so there is no CNAME file in this directory to drift out of sync.
2. Point DNS at GitHub:
   - apex domain (`example.com`) → four `A` records to `185.199.108.153`, `185.199.109.153`,
     `185.199.110.153`, `185.199.111.153`
   - `www`, or any subdomain → one `CNAME` record to `entitybrian69-bit.github.io`
3. Wait for the certificate, then tick **Enforce HTTPS**.
4. Update the URLs baked into the HTML:

```sh
cd website
BASE='https://your.actual.domain'
sed -i "s|https://entitybrian69-bit.github.io/Mirai-launcher|$BASE|g" *.html
```

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
