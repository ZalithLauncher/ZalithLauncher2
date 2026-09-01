# Fase 2 — Minecraft Java (branch minecraft-java)

Motor de versões, instâncias e loaders. Dependência zero:
HttpURLConnection + org.json (nativo do Android).

## Módulos

| caminho | papel |
|---------|-------|
| game/Http.kt | http mínimo bloqueante (user-agent do launcher) |
| game/VersionManifest.kt | manifest do piston-meta com cache (1h lista, 24h versão) |
| game/VersionInfo.kt | version json: libs com regras, downloads, assets, inheritsFrom e ResolvedVersion |
| game/Rules.kt | motor de regras (os/features) — Android conta como linux, arch cru |
| game/Downloader.kt | downloads paralelos com sha1, retomada por Range e skip incremental |
| game/AssetManager.kt | índice e objetos de assets, endereçamento por sha1, virtual legado |
| game/JavaRuntime.kt | java 8/16/17/21 exigido por versão + runtimes/jreN |
| game/LaunchArguments.kt | jvm/game args finais (formato novo e legado) + classpath |
| game/GameSetup.kt | orquestrador: resolve, baixa libs, cliente, log4j e assets |
| instances/InstanceManager.kt | instâncias isoladas (.minecraft por instância) + store comum dedupe |
| loaders/FabricLikeInstaller.kt | fabric e quilt: profile json direto do meta |
| loaders/ForgeInstaller.kt | forge: installer, profile, libs; processors ficam pra fase runtime |

Pacotes: com.zalith.launcher.game / .instances / .loaders
Fonte: core/src/main/java/com/zalith/launcher/...

## Layout de dados

    <raiz>/instances/<id>/.minecraft/{saves,mods,config,resourcepacks,logs}
    <raiz>/instances/<id>/versions/<loader>.json
    <raiz>/common/libraries/...        (maven, verificado por sha1)
    <raiz>/common/clients/<versao>.jar
    <raiz>/common/assets/{indexes,objects,virtual}
    <raiz>/common/log-configs/
    <raiz>/common/installers/
    <raiz>/runtimes/jre8|jre16|jre17|jre21
    <raiz>/manifests/                  (cache do mojang)

Decisão de otimização: bibliotecas e assets são um repositório comum
endereçado por conteúdo. Instâncias continuam isoladas no que é delas
(saves, mods, config), sem duplicar centenas de MB de jars entre elas.

## Fabric e Quilt

Ambos expõem um profile json pronto (meta .../versions/loader/<jogo>/
<loader>/profile/json) que já é um version json com inheritsFrom da
vanilla. O installer grava esse json na instância e baixa as libs.

## Forge

O installer é baixado e aberto como zip: install_profile.json e
version.json extraídos, bibliotecas listadas baixadas. Os processors
(pós-processamento que exige rodar jars numa JVM) ficam para a fase
de runtime, junto da ponte JNI — o ForgeResult avisa quando estão
pendentes.

## Runtime Java

JavaRuntimes.requiredMajor lê javaVersion.majorVersion do manifest com
fallback por faixa (até 1.16: 8; 1.17: 16; 1.18 a 1.20.4: 17; 1.20.5
em diante: 21). No Android o JRE vem empacotado com o APK (fase 0) e
é marcado em runtimes/jreN com o arquivo .zl3-jre.

## Dependência entre fases

LaunchArguments importa com.zalith.launcher.multiplayer.accounts.Account
(da fase 1). As branchs são fluxos paralelos de trabalho: num checkout
combinado — ou depois do merge para a main — tudo compila junto.

## Como a UI consome (fase 0)

1. InstanceManager.create("nome", "1.20.1", FABRIC, "0.92.2")
2. FabricLikeInstaller.install(...) ou ForgeInstaller.install(...)
3. GameSetup.prepare(instance) — baixa tudo que falta (incremental)
4. SessionManager.startSession() da fase 1
5. LaunchArguments(...).jvmArgs() / gameArgs() / mainClass()
6. repassar ao runtime (ponte JNI da fase 0) com o jre resolvido

Rede bloqueante: sempre fora da main thread.
