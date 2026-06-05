package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
import com.perpustakaan.model.BukuFisik;
import com.perpustakaan.model.EBook;
import com.perpustakaan.model.BukuLangkaDecorator;
import com.perpustakaan.model.Transaksi;
import com.perpustakaan.state.DipinjamState;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryDatabase {
    private static LibraryDatabase instance;
    private Map<String, Buku> tabelBuku;
    private List<Transaksi> daftarTransaksi; 

    // Definisi 3 File CSV terpisah sebagai tabel database
    private static final String FILE_FISIK = "buku_fisik.csv";
    private static final String FILE_EBOOK = "ebook.csv";
    private static final String FILE_TRANSAKSI = "transaksi.csv";

    private LibraryDatabase() {
        tabelBuku = new HashMap<>();
        daftarTransaksi = new ArrayList<>(); 
        
        // Membaca semua baris CSV saat aplikasi menyala
        muatDariFile();
    }

    public static LibraryDatabase getInstance() {
        if (instance == null) {
            instance = new LibraryDatabase();
        }
        return instance;
    }

    // --- MANAJEMEN DATA BUKU (TRUE APPEND - OPSI 1) ---
    public void tambahBuku(Buku buku) {
        // 1. Masukkan ke dalam Map RAM agar sinkron saat aplikasi berjalan
        tabelBuku.put(buku.getId(), buku);
        
        // 2. Deteksi kelangkaan (Decorator Pattern)
        boolean isLangka = (buku instanceof BukuLangkaDecorator);
        Buku objekAsli = buku;
        if (isLangka) {
            objekAsli = membongkarDecorator(buku);
        }

        // 3. Lakukan Append ke file CSV yang sesuai (menggunakan parameter 'true' pada FileWriter)
        if (objekAsli instanceof BukuFisik) {
            // Parameter 'true' membuat data otomatis tertulis di BARIS PALING BAWAH
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_FISIK, true))) {
                String info = objekAsli.tampilkanInfo();
                String rak = "Rak-Umum";
                if (info.contains("Rak: ")) {
                    rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                }
                // Tulis satu baris baru di akhir file CSV fisik
                out.printf("%s,%s,%s,%s,%s,%s,%b\n", 
                    buku.getId(), buku.getJudul(), buku.getPengarang(), 
                    buku.getGenre(), rak, buku.getState().getStatusName(), isLangka);
            } catch (IOException e) {
                System.out.println("[ERROR DATABASE]: Gagal melakukan append data buku fisik.");
            }
            
        } else if (objekAsli instanceof EBook) {
            // Parameter 'true' membuat data otomatis tertulis di BARIS PALING BAWAH
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_EBOOK, true))) {
                String info = objekAsli.tampilkanInfo();
                String ukuran = "Unknown-Size";
                if (info.contains("Digital: ")) {
                    ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                }
                // Tulis satu baris baru di akhir file CSV ebook
                out.printf("%s,%s,%s,%s,%s,%s,%b\n", 
                    buku.getId(), buku.getJudul(), buku.getPengarang(), 
                    buku.getGenre(), ukuran, buku.getState().getStatusName(), isLangka);
            } catch (IOException e) {
                System.out.println("[ERROR DATABASE]: Gagal melakukan append data ebook.");
            }
        }
    }

    public Buku ambilBukuBerdasarkanId(String id) {
        return tabelBuku.get(id);
    }

    public List<Buku> ambilSemuaBuku() {
        return new ArrayList<>(tabelBuku.values());
    }

    // --- MANAJEMEN DATA TRANSAKSI (APPEND) ---
    public void tambahTransaksi(Transaksi trx) {
        daftarTransaksi.add(trx);
        
        // Menggunakan mode append 'true' agar transaksi baru menempel di baris paling bawah csv
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_TRANSAKSI, true))) {
            out.printf("%s,%s,%s,%s,%s\n", 
                trx.getIdTransaksi(), trx.getNamaAnggota(), 
                trx.getIdBuku(), trx.getJudulBuku(), trx.getStatusTransaksi());
        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal melakukan append data transaksi.");
        }
    }

    public List<Transaksi> ambilSemuaTransaksi() {
        return daftarTransaksi;
    }

    // --- MANAJEMEN UPDATE STATE (OVERWRITE KHUSUS UPDATE STATUS PINJAM/KEMBALI) ---
    // Method ini dipanggil saat state buku berubah atau status transaksi di-update (DIPINJAM -> DIKEMBALIKAN)
    public void simpanKeFile() {
        try (PrintWriter outFisik = new PrintWriter(new FileWriter(FILE_FISIK));
             PrintWriter outEbook = new PrintWriter(new FileWriter(FILE_EBOOK));
             PrintWriter outTrx = new PrintWriter(new FileWriter(FILE_TRANSAKSI))) {
            
            // 1. Tulis ulang status buku terbaru dari memory RAM ke CSV
            for (Buku buku : tabelBuku.values()) {
                String id = buku.getId();
                String judul = buku.getJudul();
                String pengarang = buku.getPengarang();
                String genre = buku.getGenre();
                String status = buku.getState().getStatusName();
                
                boolean isLangka = (buku instanceof BukuLangkaDecorator);
                Buku objekAsli = buku;
                if (isLangka) {
                    objekAsli = membongkarDecorator(buku);
                }

                if (objekAsli instanceof BukuFisik) {
                    String info = objekAsli.tampilkanInfo();
                    String rak = "Rak-Umum";
                    if (info.contains("Rak: ")) {
                        rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                    }
                    outFisik.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, rak, status, isLangka);
                } else if (objekAsli instanceof EBook) {
                    String info = objekAsli.tampilkanInfo();
                    String ukuran = "Unknown-Size";
                    if (info.contains("Digital: ")) {
                        ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                    }
                    outEbook.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, ukuran, status, isLangka);
                }
            }

            // 2. Tulis ulang semua status transaksi terbaru ke CSV
            for (Transaksi t : daftarTransaksi) {
                outTrx.printf("%s,%s,%s,%s,%s\n", t.getIdTransaksi(), t.getNamaAnggota(), t.getIdBuku(), t.getJudulBuku(), t.getStatusTransaksi());
            }

        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal memperbarui status data pada berkas CSV.");
        }
    }

    // Fungsi helper reflektif untuk mengambil referensi target di dalam decorator
    private Buku membongkarDecorator(Buku bukuDekor) {
        try {
            java.lang.reflect.Field field = com.perpustakaan.model.BukuDecorator.class.getDeclaredField("bukuYangDekor");
            field.setAccessible(true);
            return (Buku) field.get(bukuDekor);
        } catch (Exception e) {
            return bukuDekor; 
        }
    }

    // --- CORE ENGINE: READ & PARSE DARI DATA CSV ---
    private void muatDariFile() {
        muatBukuFisik();
        muatEbook();
        muatTransaksi();
    }

    private void muatBukuFisik() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_FISIK))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 6) continue;
                
                Buku buku = BukuFactory.buatBuku("FISIK", data[0], data[1], data[2], data[3], data[4]);
                
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                if (data.length >= 7 && Boolean.parseBoolean(data[6].trim())) {
                    buku = new BukuLangkaDecorator(buku, 20000.0);
                }
                
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { /* File belum ada, abaikan secara aman */ }
    }

    private void muatEbook() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_EBOOK))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 6) continue;
                
                Buku buku = BukuFactory.buatBuku("EBOOK", data[0], data[1], data[2], data[3], data[4]);
                
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                if (data.length >= 7 && Boolean.parseBoolean(data[6].trim())) {
                    buku = new BukuLangkaDecorator(buku, 20000.0);
                }
                
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { /* File belum ada, abaikan secara aman */ }
    }

    private void muatTransaksi() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_TRANSAKSI))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 5) continue;
                
                Transaksi trx = new Transaksi(data[0], data[1], data[2], data[3]);
                trx.setStatusTransaksi(data[4].trim());
                daftarTransaksi.add(trx);
            }
        } catch (IOException e) { /* File belum ada, abaikan secara aman */ }
    }
}