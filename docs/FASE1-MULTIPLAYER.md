# Fase 1 — Multiplayer (branch multiplayer)

Módulos de rede e contas, prontos para a UI da fase 0 consumir.
Dependência zero: HttpURLConnection + org.json (nativo do Android).

## Módulos

| caminho | papel |
|---------|-------|
| accounts/Account.kt | modelo de conta (offline + Microsoft) com serialização JSON |
| accounts/OfflineUuid.kt | UUID offline oficial: v3 (MD5) de "OfflinePlayer:nome" |
| accounts/AccountRepository.kt | contas persistidas em accounts.json, ativa, troca de perfil |
| accounts/MicrosoftAuth.kt | device code, XBL, XSTS, login com Xbox, perfil |
| accounts/LocalSkinStore.kt | skins locais (PNG validado) para contas offline |
| session/SessionManager.kt | sessão ativa, renovação de token, join no servidor |
| servers/ServerList.kt | lista + favoritos + último status em servers.json |
| servers/ServerPing.kt | Server List Ping binário (handshake/status/VarInt) |

Pacote: com.zalith.launcher.multiplayer
Fonte: core/src/main/java/com/zalith/launcher/multiplayer/…

## Login Microsoft (device code)

1. requestDeviceCode() devolve o código; o usuário digita em microsoft.com/link
2. awaitApproval() faz poll até aprovar (ou refresh() com token salvo)
3. loginWithXbox(): XBL → XSTS → login com Xbox → perfil (nome/UUID/skin)

Antes de usar: registrar um "public client" no Azure Portal e preencher
MicrosoftAuth.clientId.

## Protocolo de ping (SLP)

TCP → handshake (protocolo -1, estado 1) → pedido de status →
resposta VarInt + JSON (motd, jogadores, versão). Implementado à mão
em servers/ServerPing.kt, sem dependências.

## Como a UI consome (fase 0)

- tela de contas: AccountRepository.list() / add() / setActive()
- adicionar conta Microsoft: MicrosoftAuth.requestDeviceCode() + awaitApproval()
- lista de servidores: ServerList.list() + ServerPing.pingAsync()
- antes de conectar: SessionManager.startSession(); joinServer() quando o servidor pedir

Todas as funções de rede são bloqueantes: chamar fora da main thread.
