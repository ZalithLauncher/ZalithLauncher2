# NusaLauncher: konteks produk

Dokumen ini dibaca sebelum merancang atau mengubah UI apa pun. Pasangannya adalah [DESIGN.md](DESIGN.md).

## Produk

Launcher Minecraft Java Edition untuk Android. Fokus: akun offline, stabilitas, dan performa yang bisa diukur.

## Pengguna

- Pemain Indonesia, mayoritas remaja sampai dewasa muda, dengan HP kelas menengah (banyak yang memakai chipset MediaTek).
- Biasa memakai akun offline, single-player, dan server komunitas.
- Tidak paham istilah teknis (renderer, GC, heap). Mereka paham "game saya patah-patah" dan "game saya crash".
- Dipakai dalam posisi **landscape**, dengan jempol di kedua sisi layar.

## Tugas utama (urut prioritas)

1. Mulai bermain dalam **dua ketukan** dari membuka aplikasi.
2. Tahu **kenapa** game crash atau patah-patah dan apa yang harus dilakukan.
3. Menemukan setelan yang paling stabil untuk HP-nya tanpa mencoba satu per satu.
4. Mengelola versi, mod, dan akun tanpa tersesat.

## Prinsip produk

- Kejelasan dan performa lebih penting daripada hiasan.
- Jelaskan hal teknis dengan bahasa sehari-hari, lalu sediakan detail bagi yang mau.
- Jujur soal batas: akun offline tidak bisa masuk server online-mode, dan itu dikatakan di tempat yang relevan.
- Angka yang ditampilkan harus berasal dari pengukuran nyata (frame time, log), bukan hiasan.

## Bukan tujuan

- Meniru tampilan launcher PC.
- Efek visual yang membebani GPU saat game belum berjalan.

## Batasan

- Hanya landscape, minSdk 26, Jetpack Compose dan Material 3.
- Lisensi GPL-3.0 beserta ketentuan tambahan dari proyek asli: layar awal atau antarmuka utama **wajib** menampilkan "Unofficial Modified Version", nama tidak boleh memuat "ZalithLauncher" atau "ZL", dan pemberitahuan hak cipta yang ditampilkan program tidak boleh dihapus.
- Aplikasi bukan produk resmi Minecraft.
