# Uji performa NusaLauncher (frame time)

NusaLauncher mencatat waktu tiap frame dan mencetak satu baris laporan tiap 5 detik ke log game:

```
[NusaFrameStats] frames=300 avg=60.0fps low1pct=45.2fps median=16.7ms max=48.0ms jitter=3.1ms stutters=2
```

| Kolom | Arti |
|---|---|
| `avg` | Rata-rata FPS dalam 5 detik terakhir. |
| `low1pct` | FPS dari 1% frame yang paling lambat. Ini yang menentukan rasa "patah-patah". |
| `median` | Waktu frame yang umum (ms). |
| `max` | Frame paling lambat (ms). |
| `jitter` | Simpangan baku waktu frame (ms). Makin kecil makin rata. |
| `stutters` | Jumlah frame yang lambat sekali: minimal 25 ms dan lebih dari 2x median. |

Aturan baca: **`low1pct` yang mendekati `avg` berarti mulus.** Jika `avg` tinggi tetapi
`low1pct` rendah, game terasa tidak stabil walaupun angka FPS-nya besar.

## Kondisi tetap (samakan di setiap uji)

- Minecraft **1.21.1**, Fabric, dunia baru dengan seed yang sama.
- Render distance **8**, simulation distance **5**, grafik **Fancy**.
- RAM game **3072 MB**, Java: Otomatis.
- HP dingin di awal, tidak sedang dicas, baterai di atas 50%, layar 120 Hz, mode Performa HyperOS aktif.
- Main 5 menit: berjalan, terbang di atas hutan, masuk area penuh entitas.

## Matriks uji

Catat hasil tiap baris (ambil beberapa baris `[NusaFrameStats]` dari log game, atau kirim log lengkapnya).

| # | Renderer | Skenario | Batas FPS | Catatan rasa (halus / patah-patah) |
|---|---|---|---|---|
| 1 | MobileGlues | A: vanilla | tanpa batas | |
| 2 | MobileGlues | A: vanilla | 60 | |
| 3 | MobileGlues | B: Sodium + Lithium + FerriteCore + ModernFix | tanpa batas | |
| 4 | MobileGlues | B | 60 | |
| 5 | GL4ES | A: vanilla | tanpa batas | |
| 6 | GL4ES | A: vanilla | 60 | |
| 7 | Zink | A: vanilla | tanpa batas | |
| 8 | Zink | A: vanilla | 60 | |

Skenario B tidak dijalankan di renderer yang tidak mendukung Sodium.

## Yang dikirim ke pengembang

1. Isi tabel di atas.
2. Log game (menu log, lalu Bagikan Tautan atau Bagikan file).
3. Info HP: tipe, chipset, RAM, suhu awal dan akhir kalau tahu.
