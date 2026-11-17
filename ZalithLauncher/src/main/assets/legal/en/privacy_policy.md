# ZalithLauncher2 Privacy Policy

**Project:** ZalithLauncher2 (**ZL2**)  
**Developer:** ZalithTeam (**we**, **us**, or **our**)  
**Version:** 1.0  
**Effective and last updated:** October 30, 2026

## 1. Introduction

This Privacy Policy explains how information is stored, processed, and transmitted when you use ZL2 on Android. It applies to official ZL2 builds and project-operated information services. It does not govern modified builds, unofficial websites, or third-party services, whose own policies apply.

ZL2 is designed primarily to operate on your device. The current version does not include a ZalithTeam-operated user-account system, advertising SDK, or behavioral analytics SDK. Core local features do not require you to register with us.

ZL2 is nevertheless a networked launcher. Authentication, searches, downloads, updates, multiplayer connectivity, optional log uploads, and external web pages require communication with third-party providers. Those providers can receive information such as your IP address and request metadata even when no information is sent to a ZalithTeam server.

Before first launching or using ZL2, you must read in full and expressly agree to both this Policy and the ZL2 User Agreement. If you decline or do not agree to either document, the launcher will not allow you to enter or continue using ZL2 or any of its features.

After either document is changed, the next time you open the app, the launcher will again display the full updated content of both documents and require your renewed explicit consent. If you decline or do not agree to either updated document, the launcher will not allow you to use ZL2.

## 2. Information Stored and Processed on Your Device

Depending on the features you use, ZL2 stores or processes the following locally:

### 2.1 Settings and application data

- launcher preferences, selected language-related settings, appearance, controls, download sources, and feature options;
- game directory paths, installed-version information, Java runtimes, renderer and component configurations;
- control layouts, imported files, downloaded resources, caches, skins, capes, and exported files; and
- game saves, logs, mods, modpacks, resource packs, shaders, and other files that you create, import, or download.

### 2.2 Account information

For Microsoft/Minecraft accounts, ZL2 may locally store your player name, profile UUID, Xbox user identifier, access token, refresh token, token expiry, account type, and downloaded skin or cape.

For a user-selected third-party authentication server, ZL2 may locally store the server URL, account name or email, password, access token, client token, profile UUID, and related account data. The current account implementation retains the third-party server password so that the account can be used for authentication. You should use this option only with a server you trust and should protect access to your device.

Microsoft device-code authorization is handled through Microsoft services. ZL2 does not ask for or receive your Microsoft account password. Microsoft may request and process information under its own privacy policy.

### 2.3 Diagnostics and logs

ZL2 and the launched game can generate local launcher logs, native logs, crash reports, game logs, and Terracotta connection logs. These may contain:

- app and game versions, Android version, device model, CPU architecture, graphics and runtime information;
- timestamps, errors, stack traces, performance or connection details;
- player names, profile identifiers, file paths, server addresses, room codes, network addresses, and command or configuration details; and
- other information written into logs by Minecraft, mods, runtimes, plugins, or the operating system.

Logs remain local unless you deliberately upload, export, or share them. Android's Logcat and diagnostic facilities may also make some information available to the operating system or to persons and tools with appropriate device access.

### 2.4 Microphone and sensor information

The microphone test reads audio samples locally to calculate and display a relative volume level. ZL2's microphone test does not save an audio recording or transmit the audio samples to us.

Gyroscope or motion-sensor input, touch input, and vibration features are processed on the device to provide game controls. ZL2 does not upload this sensor input as part of those local control features.

## 3. Information Sent During Network Features

The data involved depends on your actions and selected providers. Network providers commonly receive your IP address, requested URL, time of request, and standard connection or HTTP metadata. We do not control their server logs or retention practices.

### 3.1 Microsoft, Xbox, and Minecraft services

When you add, refresh, validate, or use a Microsoft account, ZL2 communicates with Microsoft, Xbox Live, and Minecraft/Mojang services. Requests can include device codes, OAuth tokens, Xbox tokens, player profile identifiers, game-entitlement queries, and skin or cape requests. These services process account information under their own terms and privacy policies.

When you play online, Minecraft, installed mods, and selected game servers may separately exchange account, gameplay, network, and device information. ZL2 does not control those independent communications.

### 3.2 Third-party authentication servers

If you select a third-party authentication server, ZL2 sends the account name or email and password you enter to that server for authentication. It also sends or validates access tokens and retrieves player profiles, skins, or capes as needed.

The server operator, not ZalithTeam, determines how that information is used and retained. ZL2 permits user-configured server addresses and some content may use unencrypted HTTP if you configure or retrieve such an address. Use HTTPS and a trusted provider whenever possible.

### 3.3 Game, runtime, and content downloads

When you browse versions, search for content, install a game or mod loader, check a mod for updates, or download a resource, ZL2 may send version numbers, game or mod-loader selections, project or file identifiers, search terms, installed-mod identifiers and versions, architecture information, and download requests to the relevant source.

Sources can include:

- Mojang/Minecraft distribution services;
- Forge, NeoForge, Fabric, Quilt, Legacy Fabric, OptiFine, Maven repositories, and other runtime or library sources;
- Modrinth and CurseForge, including their content-delivery services;
- BMCLAPI, MCIM Mirror, Tencent Maven mirrors, GT New Horizons repositories, and other mirrors or community sources; and
- custom URLs and sources selected by you or contained in content you import.

Downloaded or imported third-party content can make its own network connections after installation. Its behavior is not covered by our promises about ZL2 itself.

### 3.4 Update checks and project information

When ZL2 checks for launcher updates or retrieves project information such as sponsor data, it may contact GitHub APIs and content services. For Chinese-language environments, update retrieval may fall back to `repo.miawa.cn` if the GitHub request fails.

An update check can reveal the requested project resource and standard network metadata; the current app version is compared locally with retrieved release information. Selecting an update, plugin, cloud-drive link, or external download opens or contacts the relevant third-party provider.

Language, locale, and time-zone settings can be used locally to select translations, regional mirrors, or fallback sources. ZL2 does not request precise-location permissions for this purpose.

### 3.5 Optional log uploads

ZL2 does not automatically upload crash reports or logs to us. If you explicitly choose to upload and share a log, its full contents are sent to the selected upload service:

- **mclo.gs** (`api.mclo.gs`); or
- **logshare.cn** (`api.logshare.cn`), which may be tried before mclo.gs in environments identified locally as Chinese mainland environments.

If the first attempted service fails, ZL2 may retry with another listed service. An uploaded log may be assigned a link that others can open. Treat it as information shared with the service and anyone who receives the link.

Before uploading, review and remove sensitive information where possible. The upload service's own access, retention, and deletion rules apply. We may be unable to delete a log from a third-party service on your behalf.

### 3.6 Terracotta/EasyTier multiplayer

When you use multiplayer connectivity, ZL2 retrieves a node list from `terracotta.glavo.site` unless you configure a custom node. To host or join a room, Terracotta/EasyTier, selected or custom nodes, relays, and other participants may process:

- room or invitation codes and player names;
- machine or peer identifiers, IP addresses, ports, connection status, and routing metadata;
- node addresses and diagnostic logs; and
- network packets needed to establish and maintain the multiplayer connection.

The data path depends on the nodes and participants involved. Do not use this feature for unlawful activity or assume that it provides anonymity. Stop the connection when it is no longer needed.

### 3.7 Web pages, cookies, and external links

ZL2 can display project pages or third-party content in an embedded WebView or open them in another app. The embedded WebView enables JavaScript and DOM storage and allows cookies, including third-party cookies, while the page is in use. Visited sites may therefore store identifiers and collect browsing and device information under their own policies.

When you leave ZL2's embedded WebView screen, ZL2 destroys the WebView, clears its history, deletes its app-managed WebView cache directory, and requests removal of WebView cookies. This does not delete information already collected by the visited site or data stored by an external browser.

ZL2 does not add its own advertising or tracking cookies to these pages.

### 3.8 DNS fallback

ZL2 first uses the operating system's DNS resolver. If system DNS resolution fails, it may send the hostname being resolved through DNS over HTTPS to Cloudflare, Google Public DNS, or AliDNS. Successful fallback results are cached in memory for up to 10 minutes. These DNS providers receive the queried hostname and standard network metadata under their own policies.

## 4. Android Permissions and Device Features

Permissions are used for the following purposes:

| Permission or feature | Purpose |
|---|---|
| Internet and network-state access | Authentication, searches, downloads, updates, multiplayer, web pages, and connectivity checks |
| External/shared storage, including all-files access on supported Android versions | Manage game directories, saves, mods, runtimes, imported/exported files, and other user-selected content |
| Microphone | Calculate real-time volume for the optional microphone test; not used by ZL2 to upload voice recordings |
| Notifications and foreground services | Show task, game, runtime, and multiplayer status and keep requested background work running |
| VPN service | Establish the Terracotta/EasyTier multiplayer tunnel after Android authorization |
| Wake lock | Keep relevant game or background tasks running when required |
| Vibration and motion sensors | Provide game-control feedback and optional motion-based controls |

Denying a permission may limit the related feature but does not prevent unrelated local features from working. You can change permissions in Android settings. Broad storage access can expose any file you choose to manage, so use it carefully and only with content you trust.

## 5. How Information Is Used

ZL2 uses information locally and through the requested providers to:

- authenticate accounts and maintain signed-in sessions;
- install, configure, launch, and manage Minecraft and related content;
- provide search, download, update, file-management, control, and multiplayer features;
- display status, diagnose errors, and maintain local logs;
- remember preferences and selected sources; and
- provide the specific sharing or upload action you request.

The current ZL2 app does not use your local account credentials, saves, logs, or files to build advertising profiles, sell personal information, or train a ZalithTeam-operated profiling system.

## 6. When Information Is Shared

We do not sell or rent your personal information.

Information leaves your device in the circumstances described in Section 3, principally because you use a feature that communicates directly with a provider. ZL2 may also disclose information you voluntarily submit to us, such as a bug report or email, to contributors or service providers when reasonably necessary to respond, investigate a security issue, or maintain the project.

We may disclose information in our possession where required by applicable law, a valid legal process, or to protect the rights, safety, and security of users, contributors, and the public.

Information already disclosed to a third-party provider, uploaded-log recipient, multiplayer participant, or external website is governed by that recipient's practices, not solely by this Policy.

## 7. Retention and Deletion

Local information is generally retained until you delete it, remove the relevant account, clear the app's data, or uninstall ZL2, subject to the following:

- Launcher logs are retained for **7 days by default**, with an available setting from **1 to 14 days**, and older logs are cleaned during logger initialization. Other game, native, crash, or Terracotta logs may follow separate lifecycle rules.
- Cached files may remain until replaced, cleared, or removed by Android or by you.
- Files in shared or external storage, exported content, and separately installed game files may remain after uninstallation.
- Removing an account from ZL2 deletes its local database entry and associated local skin file; it does not revoke or erase data held by the account provider. Revoke sessions through that provider where necessary.

Android backup and device-transfer features are enabled for ZL2. Depending on your device and settings, app data may be copied by the operating system to a cloud-backup service or another device. Google, your device manufacturer, or another backup provider controls that processing under its own policy. Deleting local data may not immediately delete an existing system backup.

Third-party services determine their own retention periods. Contact the relevant provider to request deletion of server-side account data or uploaded logs.

## 8. Security

ZL2 relies on Android's application sandbox and local storage controls and uses network-security mechanisms for supported connections. However, no application, device, network, or storage method is completely secure.

Protect your device with a screen lock, keep Android and ZL2 updated, avoid untrusted modified builds and authentication servers, and do not share tokens or unreviewed logs. Third-party account credentials stored locally may be exposed if someone gains sufficient access to your app data, device backup, or a compromised device.

ZL2 allows cleartext network traffic for compatibility with user-selected and third-party sources. Information sent over HTTP rather than HTTPS can be intercepted or modified. Prefer trusted HTTPS sources.

## 9. Your Choices and Rights

Within ZL2 or Android, you can:

- view and remove locally saved accounts;
- change download sources, custom authentication servers, and feature settings;
- revoke microphone, storage, notification, or VPN permissions, subject to Android version and device controls;
- choose whether to upload, export, or share logs and other files;
- clear app data, delete local files, or uninstall ZL2; and
- disable or manage system backup through your device or backup-provider settings.

Depending on your jurisdiction, you may also have rights to access, correct, delete, restrict, object to, or obtain a copy of personal information, and to withdraw consent. Because ZL2 stores most information locally and does not maintain a general user-account database, you can exercise many of these choices directly on your device.

For information held by ZalithTeam, contact us using the details below. For information held by Microsoft, a custom authentication server, a content platform, a log-upload service, a multiplayer node, or another provider, direct your request to that provider. We may need enough information to verify a request, but do not send us your password or authentication tokens.

## 10. Children and Minors

ZL2 is not designed to collect a child's information through a ZalithTeam account because we do not provide such an account system. Nevertheless, account, online-game, multiplayer, and web features can involve personal information and third-party services.

If you are a minor, use ZL2 only with a parent or legal guardian's consent and supervision. Parents and guardians should review account settings, downloads, shared logs, multiplayer activity, and third-party privacy policies. Age requirements and parental-consent mechanisms of Microsoft, Mojang, platform providers, and applicable law must also be followed.

If you believe a child has provided personal information directly to us inappropriately, contact us so we can assess and handle the information in accordance with applicable law.

## 11. International Processing

ZL2 can contact providers and multiplayer participants in different countries or regions. Your IP address, account requests, downloads, uploaded logs, and multiplayer data may therefore be processed outside your place of residence. Privacy protections and government-access rules may differ between jurisdictions.

Where ZalithTeam is responsible for a transfer that requires additional safeguards under applicable law, we will take reasonably appropriate steps. Third-party providers remain responsible for their own transfer mechanisms and legal obligations.

## 12. Changes to This Policy

We may update this Policy when ZL2's features, providers, or legal obligations change. The revised version will include a new date and version number and will be provided with ZL2 or through an official project channel. For material changes, we will provide reasonable additional notice where practicable.

After any change to this Policy or the User Agreement, the next time you open the app, the launcher will again display the full updated content of both documents. You must expressly agree to both updated documents again; if you decline or do not agree to either one, the launcher will not allow you to use ZL2.

## 13. Contact

For privacy questions, requests, or security reports, contact ZalithTeam through:

- **Project:** https://github.com/ZalithLauncher/ZalithLauncher2
- **Issues:** https://github.com/ZalithLauncher/ZalithLauncher2/issues
- **Email:** movtery228@qq.com

For sensitive security reports, prefer email rather than posting details publicly. Do not include passwords, access tokens, refresh tokens, or unreviewed logs.

This Policy is available in English and Simplified Chinese. Both versions are intended to have the same meaning; if they differ, the Simplified Chinese version prevails to the extent permitted by applicable law.
