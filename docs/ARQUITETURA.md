# Arquitetura — ZalithLauncher3

## Princípios

1. Desempenho primeiro: toda decisão passa pelo custo em FPS com mods.
2. Instâncias isoladas: cada perfil tem seu .minecraft, JVM e bibliotecas.
3. Interface leve: nenhuma webview no caminho crítico.

## Camadas previstas

- app/ — UI Android (Kotlin): telas, navegação, tema
- core/ — contas, instâncias, downloads, verificação de assets
- runtime/ — ponte JNI com a JVM e bootstrap do cliente
- java/ — lado Java: hooks de render e controles touch

## Metas de otimização (com mods)

- Flags JVM escolhidas por preset de memória do dispositivo
- Reuso de classloader entre reinícios da mesma instância
- Verificação incremental para atualizações de mods
