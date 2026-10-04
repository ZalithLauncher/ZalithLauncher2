/* ==========================================================================
   Mirai Launcher — site behaviour
   Theme engine (mode + accent) · mobile nav · scroll reveal · sticky header
   ========================================================================== */
(function () {
  "use strict";

  var root = document.documentElement;
  var MODE_KEY = "mirai-theme";
  var ACCENT_KEY = "mirai-accent";
  var MODES = ["light", "dark", "auto"];
  var ACCENTS = ["violet", "amber", "forest", "ocean", "rose"];

  var mq = window.matchMedia("(prefers-color-scheme: dark)");

  /* ---------- storage ---------- */
  function read(key, fallback) {
    try {
      var v = localStorage.getItem(key);
      return v === null ? fallback : v;
    } catch (e) {
      return fallback;
    }
  }
  function write(key, value) {
    try { localStorage.setItem(key, value); } catch (e) { /* private mode */ }
  }

  /* ---------- theme ---------- */
  function resolve(mode) {
    if (mode === "auto") return mq.matches ? "dark" : "light";
    return mode === "dark" ? "dark" : "light";
  }

  /* ---------- giscus ----------
     giscus renders in its own iframe and knows nothing about the theme control
     on this page. Push the resolved theme into it, both when the theme changes
     and once the iframe has loaded — otherwise the comment box sits on the OS
     preference and visibly disagrees with the rest of the site. */
  function pushGiscusTheme(resolved) {
    var frame = document.querySelector("iframe.giscus-frame");
    if (!frame || !frame.contentWindow) return;
    frame.contentWindow.postMessage(
      { giscus: { setConfig: { theme: resolved } } },
      "https://giscus.app"
    );
  }

  // giscus fires nothing useful after load, so watch for the iframe instead.
  var giscusObserver = new MutationObserver(function () {
    if (document.querySelector("iframe.giscus-frame")) {
      pushGiscusTheme(resolve(read(MODE_KEY, "auto")));
      giscusObserver.disconnect();
    }
  });
  giscusObserver.observe(document.documentElement, { childList: true, subtree: true });

  function applyMode(mode) {
    var resolved = resolve(mode);
    root.setAttribute("data-theme", resolved);
    root.setAttribute("data-mode", mode);
    document.querySelectorAll("[data-mode-set]").forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.getAttribute("data-mode-set") === mode));
    });
    // keep the browser chrome in step with the page
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) {
      meta.setAttribute("content", resolved === "dark" ? "#14120F" : "#FAF6F1");
    }
    // and the comment iframe, if it happens to be loaded already
    pushGiscusTheme(resolved);
  }

  function applyAccent(accent) {
    if (ACCENTS.indexOf(accent) === -1) accent = ACCENTS[0];
    root.setAttribute("data-accent", accent);
    document.querySelectorAll("[data-set]").forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.getAttribute("data-set") === accent));
    });
  }

  function setMode(mode) {
    if (MODES.indexOf(mode) === -1) mode = "auto";
    write(MODE_KEY, mode);
    applyMode(mode);
  }

  function setAccent(accent) {
    write(ACCENT_KEY, accent);
    applyAccent(accent);
  }

  // ---------- boot ----------
  var mode = read(MODE_KEY, "auto");
  var accent = read(ACCENT_KEY, "violet");

  applyMode(mode);
  applyAccent(accent);

  // Enable colour transitions only after the first paint, so the initial
  // render never animates from the default palette.
  window.requestAnimationFrame(function () {
    window.requestAnimationFrame(function () {
      root.classList.add("theme-ready");
    });
  });

  // Follow the OS while in auto mode
  var onSystemChange = function () { if (read(MODE_KEY, "auto") === "auto") applyMode("auto"); };
  if (mq.addEventListener) mq.addEventListener("change", onSystemChange);
  else if (mq.addListener) mq.addListener(onSystemChange);

  /* ---------- theme popover ---------- */
  var panel = document.getElementById("theme-panel");
  var toggle = document.getElementById("theme-toggle");

  function closePanel() {
    if (!panel) return;
    panel.classList.remove("is-open");
    if (toggle) toggle.setAttribute("aria-expanded", "false");
  }

  if (panel && toggle) {
    toggle.addEventListener("click", function (e) {
      e.stopPropagation();
      var open = panel.classList.toggle("is-open");
      toggle.setAttribute("aria-expanded", String(open));
    });
    panel.addEventListener("click", function (e) { e.stopPropagation(); });

    document.addEventListener("click", closePanel);
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") { closePanel(); closeMenu(); }
    });
  }

  document.querySelectorAll("[data-mode-set]").forEach(function (btn) {
    btn.addEventListener("click", function () { setMode(btn.getAttribute("data-mode-set")); });
  });
  document.querySelectorAll("[data-set]").forEach(function (btn) {
    btn.addEventListener("click", function () { setAccent(btn.getAttribute("data-set")); });
  });

  /* ---------- mobile menu ---------- */
  var menu = document.getElementById("mobile-menu");
  var menuBtn = document.getElementById("menu-toggle");

  function closeMenu() {
    if (!menu) return;
    menu.classList.remove("is-open");
    if (menuBtn) menuBtn.setAttribute("aria-expanded", "false");
  }

  if (menu && menuBtn) {
    menuBtn.addEventListener("click", function (e) {
      e.stopPropagation();
      var open = menu.classList.toggle("is-open");
      menuBtn.setAttribute("aria-expanded", String(open));
    });
    menu.addEventListener("click", function (e) {
      if (e.target.closest("a")) closeMenu();
    });
  }

  /* ---------- sticky header ---------- */
  var header = document.querySelector(".site-header");
  if (header) {
    var onScroll = function () {
      header.classList.toggle("is-stuck", window.scrollY > 8);
    };
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
  }

  /* ---------- scroll reveal ---------- */
  var reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var items = document.querySelectorAll(".reveal");

  if (reduce || !("IntersectionObserver" in window)) {
    items.forEach(function (el) { el.classList.add("is-in"); });
  } else {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        entry.target.classList.add("is-in");
        io.unobserve(entry.target);
      });
    }, { rootMargin: "0px 0px -8% 0px", threshold: 0.08 });

    items.forEach(function (el) { io.observe(el); });
  }
})();
