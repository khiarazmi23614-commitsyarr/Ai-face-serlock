# AI Face Sherlock

Aplikasi Android kamera langsung untuk mencocokkan wajah dengan database wajah yang pengguna miliki izin untuk gunakan.

## Fitur
- Kamera langsung tanpa memilih file foto.
- Deteksi wajah dari kamera.
- Pencocokan terhadap dataset lokal/berizin.
- Jika tidak memenuhi ambang kemiripan: **Gagal — tidak ditemukan kemiripan**.
- Tidak melakukan pencarian identitas orang secara bebas di internet.
- GitHub Actions untuk membangun APK.

## Struktur
- `app/` — aplikasi Android.
- `database/` — contoh format data wajah/profil.
- `.github/workflows/build-apk.yml` — build APK otomatis.

## Catatan
Untuk penggunaan nyata, dataset wajah harus dikumpulkan dengan izin yang sesuai. Nilai kemiripan tidak boleh dianggap sebagai bukti identitas.
