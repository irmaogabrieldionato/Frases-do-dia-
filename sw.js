/* Cache offline limitado aos recursos próprios do jogo. Não armazena outras aplicações nem anúncios. */
const CACHE_NAME = "seven-note-musical-game-v6";
const APP_FILES = [
  "./",
  "./index.html",
  "./game-core.js",
  "./app.js",
  "./styles.css",
  "./tv.css",
  "./privacy.html",
  "./manifest.webmanifest",
  "./icon.svg"
];
const APP_URLS = new Set(APP_FILES.map((path) => new URL(path, self.registration.scope).href));
self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_FILES))
      .then(() => self.skipWaiting())
  );
});
self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((key) => key.startsWith("seven-note-musical-game-") &&
        key !== CACHE_NAME).map((key) => caches.delete(key)))
    ).then(() => self.clients.claim())
  );
});
self.addEventListener("fetch", (event) => {
  if (event.request.method !== "GET") return;
  const url = new URL(event.request.url);
  // A URL deve pertencer exatamente ao conjunto de arquivos do jogo.
  // Ignorar consultas, outras rotas e solicitações cross-origin evita
  // cachear dados privados ou arquivos de outros aplicativos do mesmo host.
  if (url.search || !APP_URLS.has(url.href)) return;
  event.respondWith(
    fetch(event.request).then((response) => {
      if (response.ok && response.type === "basic") {
        const copy = response.clone();
        event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy)));
      }
      return response;
    }).catch(() => caches.match(event.request)
      .then((cached) => cached || Response.error()))
  );
});
