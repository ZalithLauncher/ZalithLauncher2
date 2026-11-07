<p align="center">
  <img src="brand/banner.png" alt="NusaLauncher" width="720">
</p>

<p align="center">
  Launcher Minecraft Java Edition untuk Android dengan akun offline, analisis crash, dan pemantau performa.
</p>

[English](README_EN_US.md) | [简体中文 (dokumen upstream)](README_ZH_CN.md)

> [!NOTE]
> **NusaLauncher** adalah fork komunitas **tidak resmi** dari [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2) karya MovTery dan para kontributor. Proyek ini **tidak berafiliasi dengan dan tidak didukung oleh** tim Zalith Launcher. Dilisensikan GPL-3.0, sama seperti proyek aslinya.
>
> Bukan produk resmi Minecraft. Tidak disetujui atau dikaitkan dengan Mojang atau Microsoft. Minecraft adalah merek dagang milik Mojang Synergies AB.

## Status

NusaLauncher masih dalam **tahap uji**. Pembaruan otomatis belum tersedia: tombol "Periksa pembaruan" membuka halaman [Releases](https://github.com/PickoCore/NusaLauncher/releases). Belum ada rilis resmi, dan aplikasinya masih berupa build debug. Fitur bisa berubah, dan bug masih mungkin muncul.

## Fitur

### Dasar dari Zalith Launcher 2

- Menjalankan Minecraft Java Edition di perangkat Android (arm64, Android 8.0 ke atas).
- Beberapa renderer, antara lain GL4ES, MobileGlues, dan Zink.
- Pemasangan Fabric dan modpack, pengelola versi dan Java, serta kontrol layar sentuh.

### Ditambahkan oleh NusaLauncher

- **Akun offline** tanpa kewajiban login Microsoft lebih dulu.
- **Analisis crash lokal.** Setelah game crash, launcher mengenali 12 jenis masalah umum (RAM habis, Java salah versi, mod bentrok, shader gagal, crash native, dan lainnya) dan menampilkan penyebab serta saran dalam bahasa Indonesia atau Inggris. Berjalan tanpa internet.
- **Bagikan log ke mclo.gs** dengan penyensoran otomatis (UUID, token, email, dan alamat IP disamarkan sebelum diunggah).
- **Arsip log otomatis.** Log tiap sesi game disimpan saat kamu keluar dengan nama `NusaLogs-2026-10-06-19.41.MobileGlues.log` dan disimpan 30 hari. Lihat [docs/logs.md](docs/logs.md).
- **Pengukur frame time.** Setiap 5 detik log game memuat baris `[NusaFrameStats]` berisi FPS rata-rata, 1% low, jitter, dan jumlah sentakan, agar masalah "FPS tinggi tapi patah-patah" bisa diukur. Lihat [docs/performance-testing.md](docs/performance-testing.md).
- Identitas baru: nama, ikon, dan paket aplikasi sendiri (`com.pickocore.nusalauncher`).

### Dalam rencana (belum tersedia)

- Layar **Riwayat log** di dalam aplikasi (daftar, lihat, bagikan, hapus).
- **Main bareng** lewat jaringan lokal (satu WiFi), dengan opsi lintas versi.
- **Pusat renderer** dan benchmark untuk memilih renderer yang paling cocok di HP-mu.
- Tema antarmuka hitam dan oranye.
- Login Microsoft yang diuji penuh. Kode dasarnya ikut dari Zalith, tetapi belum kami uji.

## Unduh

Belum ada rilis. Untuk mencoba build uji:

1. Buka tab **Actions** repo ini dan pilih run **Build APK** terbaru yang hijau.
2. Di bagian **Artifacts**, unduh APK untuk arsitektur HP-mu (kebanyakan HP modern memakai **arm64**). Perlu login GitHub.
3. Ekstrak file zip-nya, lalu pasang APK di dalamnya (izinkan pemasangan dari sumber tidak dikenal).

## Catatan penggunaan

- **Akun offline** hanya untuk mode satu pemain dan server berstatus offline-mode. Server online-mode (kebanyakan server besar) menolaknya dan butuh akun Microsoft asli. Kalau kamu menyukai game ini, belilah secara resmi untuk mendukung Mojang.
- **Pilihan renderer.** Dalam uji awal di satu HP (Dimensity 8500, Mali-G720), GL4ES paling stabil untuk Minecraft vanilla, MobileGlues di bawahnya, dan Zink paling sering patah-patah. GL4ES tidak mendukung Sodium dan bisa crash jika dipakai bersamanya. Itu hasil satu perangkat, jadi anggap sebagai petunjuk, bukan kepastian.
- Kalau terjadi crash, buka menu log setelah game tertutup untuk melihat diagnosisnya, atau kirim file `NusaLogs-...` saat melapor.

## Dokumentasi

- [Sistem log](docs/logs.md)
- [Panduan uji performa](docs/performance-testing.md)
- [Aset merek](brand/README.md)

## Build dari source

Persyaratan (mengikuti [workflow CI](.github/workflows/build.yml)):

- JDK 21
- Android SDK dan NDK `27.0.12077973`
- Rust (stable) dan [cargo-ndk](https://github.com/bbqsrc/cargo-ndk), untuk modul `PetLib`
- Gradle wrapper bawaan repo (AGP 9.3, Kotlin 2.4)
- minSdk 26, targetSdk 34

```bash
git clone https://github.com/PickoCore/NusaLauncher.git
# Buka di Android Studio lalu build, atau ikuti langkah di .github/workflows/build.yml
```

## Melapor masalah

Buka [Issues](https://github.com/PickoCore/NusaLauncher/issues). Sertakan tipe HP, versi Minecraft, renderer, dan file log arsip (`NusaLogs-...log`). Jangan menempelkan token atau data pribadi.

Masalah yang berasal dari proyek asli (bukan fitur NusaLauncher) sebaiknya dilaporkan juga ke [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2/issues).

## Lisensi dan atribusi

NusaLauncher dilisensikan di bawah **[GPL-3.0](LICENSE)**. Ia dibangun di atas **Zalith Launcher 2** (hak cipta MovTery dan kontributor) dan sejumlah pustaka sumber terbuka lain.

Ringkasan **Ketentuan Tambahan** dari proyek asli (GPLv3 bagian 7). Teks lengkapnya ada di [README_EN_US.md](README_EN_US.md#additional-terms-pursuant-to-section-7-of-the-gplv3-license):

1. Versi modifikasi tidak boleh memakai nama "ZalithLauncher" atau singkatan "ZL", atau nama yang mirip sampai membingungkan, dan harus ditandai sebagai **"Unofficial Modified Version"** di layar awal atau antarmuka utama.
2. Pemberitahuan hak cipta yang ditampilkan program tidak boleh dihapus.

Font Chakra Petch dipakai di bawah lisensi SIL OFL 1.1 ([teks lisensi](docs/licenses/ChakraPetch-OFL.txt)).

Daftar pustaka sumber terbuka yang dipakai beserta lisensinya ada di [README_EN_US.md](README_EN_US.md#open-source-libraries-and-licenses).
