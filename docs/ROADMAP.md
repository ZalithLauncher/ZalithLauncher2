# Roadmap — ZalithLauncher3

Cada fase vive na sua branch; a principal recebe merges estabilizados.

## Fase 0 — APK principal (branch main)
- [ ] Projeto Android (Gradle + Kotlin) mínimo viável
- [ ] Estrutura de telas e navegação base
- [ ] Permissões, armazenamento scoped e diretório de dados
- [ ] Build debug/release assinado

## Fase 1 — Multiplayer (branch multiplayer)
- [ ] Gerenciador de contas: offline (nome/UUID/skin local) e Microsoft
- [ ] Login offline sem depender de serviço externo
- [ ] Lista de servidores com ping, versão e favoritos
- [ ] Sessão persistente e troca rápida de perfil

## Fase 2 — Minecraft Java (branch minecraft-java)
- [ ] Leitura do version manifest (Mojang) e download de versões
- [ ] Loaders por instância: Fabric, Forge, Quilt
- [ ] .minecraft isolado por instância (libs e config separados)
- [ ] Runtime Java por versão do jogo (8 / 17 / 21)

## Fase 3 — Melhorias e otimização (branch melhorias)
- [ ] Presets de flags JVM conforme a RAM do aparelho
- [ ] Ajuste automático de -Xmx e GC (G1GC / ZGC)
- [ ] Download paralelo + verificação incremental de bibliotecas
- [ ] Cache de assets/shaders e diagnóstico local de FPS/latência
