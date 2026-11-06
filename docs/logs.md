# Log NusaLauncher

Semua log ada di `Android/data/com.pickocore.nusalauncher/files/logs/`
(versi debug: `com.pickocore.nusalauncher.debug`).

| Lokasi | Isi | Disimpan |
|---|---|---|
| `logs/game/NusaLogs-<tanggal>-<jam>.<menit>.<Renderer>.log` | **Log game tiap sesi Minecraft.** Dibuat otomatis saat pemain keluar dari game. | **30 hari** |
| `logs/log_<waktu>_main.log` | Log internal launcher. | Sesuai pengaturan (1 sampai 14 hari) |
| `logs/native/` | Laporan crash native. | |
| `logs/file_manager/` | Log pengelola berkas. | |

## Format nama

`NusaLogs-2026-10-06-19.41.MobileGlues.log`

- `2026-10-06` tanggal, `19.41` jam dan menit saat pemain keluar dari game.
- Bagian terakhir adalah **renderer** yang dipakai sesi itu.
- Kalau ada dua sesi dengan menit dan renderer yang sama, yang kedua diberi akhiran `-2`.

## Isi file

Setiap arsip diawali blok ringkasan: versi Minecraft, renderer, kode keluar, versi launcher.
Di bawahnya log game aslinya. Log asli per versi (`latest_game.log`) tetap ada dan dipakai menu log.

## Batasan

Log hanya diarsipkan kalau game keluar lewat jalur normal atau crash yang tertangkap launcher.
Kalau Android mematikan paksa prosesnya (misalnya kehabisan memori), arsip untuk sesi itu bisa tidak terbentuk.
