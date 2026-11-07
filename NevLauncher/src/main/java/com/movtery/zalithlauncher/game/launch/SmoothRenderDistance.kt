/*
 * Nev Launcher
 * Fitur "Render Distance Smooth" oleh NestMC.
 *
 * Tujuan: render distance tinggi tetap terasa lancar. Launcher tidak mengubah
 * nilai render distance pilihan pemain; yang dikurangi adalah beban yang tidak
 * terlihat (simulasi chunk, entity jauh, blending biome, partikel, GC JVM).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.game.launch

private const val DEFAULT_SIMULATION_DISTANCE = 5

/**
 * Terapkan opsi video/simulasi yang ringan ke options.txt sebelum game jalan.
 * Nilai renderDistance milik pemain tidak disentuh. Kunci yang tidak dikenal
 * oleh versi Minecraft tertentu akan diabaikan oleh game, jadi aman dipakai lintas versi.
 *
 * Hanya menurunkan beban, tidak pernah menaikkan kualitas di atas pilihan pemain.
 */
fun MCOptions.applySmoothRenderDistance(simulationDistance: Int) {
    // CPU: hanya chunk dekat yang di-tick (entity, mob, redstone). Ini penghemat terbesar.
    val renderDistance = get("renderDistance")?.toIntOrNull()
    val simulation = simulationDistance.coerceAtLeast(2).let { limit ->
        if (renderDistance != null) minOf(limit, renderDistance) else limit
    }
    set("simulationDistance", simulation.toString())

    // Entity jauh tidak digambar/diproses penuh
    val entityScaling = get("entityDistanceScaling")?.toFloatOrNull()
    if (entityScaling == null || entityScaling > 0.75f) {
        set("entityDistanceScaling", "0.75")
    }

    // Blending warna biome antar chunk mahal saat banyak chunk di-rebuild
    val biomeBlend = get("biomeBlendRadius")?.toIntOrNull()
    if (biomeBlend == null || biomeBlend > 1) {
        set("biomeBlendRadius", "1")
    }

    // Partikel: maksimal "Decreased" (0 = semua, 1 = dikurangi, 2 = minimal)
    val particles = get("particles")?.toIntOrNull()
    if (particles == null || particles < 1) {
        set("particles", "1")
    }

    // Grafis cepat: 1.16+ memakai graphicsMode (0 = fast), versi lama memakai fancyGraphics
    set("graphicsMode", "0")
    set("fancyGraphics", "false")

    // Mipmap mahal untuk GPU mobile dan hampir tidak terlihat di layar HP
    set("mipmapLevels", "0")

    // Tanpa vsync agar frame tidak tertahan saat chunk dimuat
    set("enableVsync", "false")
}

/**
 * Argumen JVM untuk mengurangi stutter akibat GC saat banyak chunk dimuat.
 * Tidak menambah apa pun jika pemain sudah menentukan GC sendiri.
 */
fun MutableList<String>.addSmoothRenderDistanceJvmArgs(cpuCores: Int) {
    val userChoseGc = any { arg ->
        arg.startsWith("-XX:+Use") && arg.endsWith("GC")
    }
    if (userChoseGc) return

    // Opsi yang tidak dikenal runtime tertentu diabaikan, bukan membuat JVM gagal start
    if (none { it == "-XX:+IgnoreUnrecognizedVMOptions" }) {
        add("-XX:+IgnoreUnrecognizedVMOptions")
    }

    val gcThreads = (cpuCores / 2).coerceIn(1, 4)

    add("-XX:+UseG1GC")
    add("-XX:MaxGCPauseMillis=40")
    add("-XX:ParallelGCThreads=$gcThreads")
    add("-XX:ConcGCThreads=${(gcThreads / 2).coerceAtLeast(1)}")
    add("-XX:+ParallelRefProcEnabled")
}
