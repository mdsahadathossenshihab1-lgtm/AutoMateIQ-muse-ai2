
self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  // Focus existing window if available
  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      // If we have a URL data attached, try to find a matching window
      const urlToOpen = (event.notification.data && event.notification.data.url) || '/';

      for (const client of clientList) {
        if (client.url === urlToOpen && 'focus' in client) {
          return client.focus();
        }
      }
      
      // If no matching window, open a new one (or just focus the first available one)
      if (clientList.length > 0) {
        return clientList[0].focus();
      }

      if (self.clients.openWindow) {
        return self.clients.openWindow(urlToOpen);
      }
    })
  );
});

// Generic Push Handler (Future Proofing)
self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : {};
  const title = data.title || 'New Notification';
  const options = {
    body: data.body || '',
    icon: 'https://cdn-icons-png.flaticon.com/512/893/893268.png',
    badge: 'https://cdn-icons-png.flaticon.com/512/893/893268.png',
    tag: 'automateiq-alert',
    vibrate: [200, 100, 200],
    requireInteraction: true,
    data: { url: self.location.origin }
  };
  event.waitUntil(self.registration.showNotification(title, options));
});
