# NusaLauncher: panduan desain ("Nusa Dark")

Baca [PRODUCT.md](PRODUCT.md) dulu. Kode dasarnya ada di
`ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ui/nusa/`.

## Arah visual

Gelap, tenang, dengan **satu hal yang diingat**: sudut kartu dan tombol yang dipotong miring
(kanan atas dan kiri bawah), meniru potongan pada logo NL. Satu aksen gradasi kuning ke oranye
dipakai hanya untuk tombol utama (Main), logo, dan penanda penting. Sisanya abu netral.

Latar hitam dan aksen oranye adalah permintaan pemilik proyek, jadi diikuti walaupun
"hitam dengan satu aksen terang" termasuk gaya yang umum.

## Token

| Nama | Nilai | Dipakai untuk |
|---|---|---|
| Background | `#0E0E10` | Latar layar |
| Surface | `#17181A` | Kartu |
| SurfaceRaised | `#212326` | Kartu saat ditekan, tombol sekunder |
| Border | `#2E3033` | Garis tipis, jarang |
| TextPrimary | `#E8E8EA` | Teks utama |
| TextSecondary | `#9A9CA1` | Teks pendukung (kontras di atas Surface sekitar 6:1) |
| Accent | `#FFB20D` ke `#FF4412` | Tombol utama, logo |
| AccentText | `#FF9A2E` | Teks atau tautan oranye di atas gelap |

- **Bentuk:** `NusaShapes.Large` (24 dp), `Medium` (16 dp), `Small` (10 dp). Ukuran potongan mengikuti ukuran elemen.
- **Huruf:** Chakra Petch untuk judul dan tombol (SIL OFL 1.1), huruf sistem untuk isi.
- **Spasi:** kelipatan 4 dp. Target sentuh minimal **48 dp**.
- **Sentuh landscape:** hormati inset layar (lubang kamera, tepi melengkung). Konten tidak boleh tertutup.

## Micro-interaction

Gerak hanya untuk **menjawab aksi** pengguna atau menandai **memuat**. Tidak ada animasi masuk
pada setiap kartu, dan tidak ada gerak dekoratif yang berjalan terus.

| Interaksi | Perilaku | Detail |
|---|---|---|
| **Tombol ditekan** | Mengecil dan menggelap sebentar, lalu kembali dengan pantulan kecil | Skala 0,96 dalam 80 ms; menggelap 14%; lepas dengan spring (damping 0,6). Komponen: `NusaButton` |
| **Kartu ditekan** | Mengecil sedikit dan naik satu tingkat warna | Skala 0,985; warna ke SurfaceRaised dalam 90 ms. Hanya kartu yang benar-benar bisa diketuk. Komponen: `NusaCard(onClick = ...)` |
| **Memuat (skeleton shimmer)** | Blok abu dengan pita terang menyapu kiri ke kanan | 1300 ms linear, berulang. Dipakai hanya jika memuat melebihi sekitar 150 ms, supaya tidak berkedip. Bentuk skeleton harus mirip konten asli. Komponen: `NusaSkeleton`, `Modifier.nusaShimmer()` |
| **Selesai memuat** | Skeleton diganti konten dengan crossfade | 180 ms |

**Animasi sistem dimatikan:** semua gerak di atas menjadi instan, dan shimmer menjadi blok abu statis
(`rememberReducedMotion()`).

## Aturan anti-generik

Diadaptasi dari panduan "anti-slop" yang dirujuk proyek ini. Periksa setiap layar:

1. Tidak ada label kecil huruf besar berjarak lebar di atas judul.
2. Tidak ada rangkaian teks dengan titik tengah ("A · B · C") dan tidak ada panah "→" yang ditempel di tautan.
3. Tidak ada deretan kartu identik dengan satu bentuk dan bayangan yang sama untuk segalanya. Hierarki ukuran dan bentuk harus ada.
4. Tidak ada angka besar dengan label kecil dan aksen gradasi sebagai satu-satunya cara menampilkan data, bila ada bentuk yang lebih tepat (misalnya grafik frame time).
5. Satu tempat saja yang berani: tombol Main dan potongan sudut. Semua lainnya tenang.
6. Struktur visual (garis, nomor, label) hanya dipakai jika menyampaikan informasi.
7. Salin teks sebenarnya. Tidak ada teks isian atau data palsu pada layar akhir.

## Bahasa antarmuka

- Huruf biasa (bukan kapital semua), kata kerja aktif, nama aksi sama di seluruh alur ("Hapus" lalu "Terhapus").
- Tombol menyebut apa yang terjadi: "Simpan perubahan", bukan "OK".
- Kesalahan menjelaskan apa yang terjadi dan cara memperbaikinya. Tidak minta maaf, tidak samar.
- Keadaan kosong mengajak bertindak ("Belum ada log. Main dulu, lognya tersimpan otomatis").
- Istilah teknis diberi penjelasan satu kalimat.

## Proses tiap layar

1. **Rencana singkat:** sketsa ASCII atau papan konsep, token yang dipakai, apa yang jadi satu hal yang diingat.
2. **Tinjau terhadap brief** (PRODUCT.md): adakah bagian yang akan sama di aplikasi mana pun? Ubah.
3. **Bangun** memakai komponen `ui/nusa`. Jangan membuat gaya baru per layar.
4. **Kritik diri dan audit**, dengan prioritas temuan:
   - **P0** memblokir: tidak terbaca, tidak bisa disentuh, crash, teks tertutup lubang kamera.
   - **P1** besar: kontras di bawah 4,5:1, target sentuh di bawah 48 dp, keadaan kosong atau memuat atau galat tidak ada.
   - **P2** sedang: teks panjang meluap, skala huruf sistem 130% merusak tata letak, animasi tidak mengikuti pengaturan sistem.
   - **P3** poles: spasi, ritme, salinan teks.
5. Ganti layar lama dan hapus kodenya pada commit yang sama. Tujuan akhirnya tidak ada UI lama yang tersisa.

## Atribusi

Font **Chakra Petch** © The Chakra Petch Project Authors, lisensi SIL OFL 1.1
([teks lisensi](../licenses/ChakraPetch-OFL.txt)).

Panduan proses dan aturan anti-generik terinspirasi dari
[taste-skill](https://github.com/leonxlnx/taste-skill) (MIT),
[impeccable](https://github.com/pbakaus/impeccable) (Apache-2.0), dan
[anthropics/skills, frontend-design](https://github.com/anthropics/skills/tree/main/skills/frontend-design).
Tidak ada kode atau teks yang disalin dari proyek-proyek itu.
