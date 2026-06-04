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

    // --- MANAJEMEN DATA BUKU ---
    public void tambahBuku(Buku buku) {
        tabelBuku.put(buku.getId(), buku);
        simpanKeFile(); // Tulis otomatis ke CSV setiap ada perubahan buku
    }

    public Buku ambilBukuBerdasarkanId(String id) {
        return tabelBuku.get(id);
    }

    public List<Buku> ambilSemuaBuku() {
        return new ArrayList<>(tabelBuku.values());
    }

    // --- MANAJEMEN DATA TRANSAKSI ---
    public void tambahTransaksi(Transaksi trx) {
        daftarTransaksi.add(trx);
        simpanKeFile(); // Tulis otomatis ke CSV setiap ada transaksi baru
    }

    public List<Transaksi> ambilSemuaTransaksi() {
        return daftarTransaksi;
    }

    // --- CORE ENGINE: SAVE & WRITE PERMANEN KE CSV ---
    public void simpanKeFile() {
        try (PrintWriter outFisik = new PrintWriter(new FileWriter(FILE_FISIK));
             PrintWriter outEbook = new PrintWriter(new FileWriter(FILE_EBOOK));
             PrintWriter outTrx = new PrintWriter(new FileWriter(FILE_TRANSAKSI))) {
            
            // 1. Iterasi dan simpan semua objek buku di dalam Map Memory
            for (Buku buku : tabelBuku.values()) {
                String id = buku.getId();
                String judul = buku.getJudul();
                String pengarang = buku.getPengarang();
                String genre = buku.getGenre();
                String status = buku.getState().getStatusName();
                
                // Deteksi Decorator Pattern secara akurat 
                boolean isLangka = (buku instanceof BukuLangkaDecorator);
                
                // Kupas objek asli yang berada di dalam decorator jika memang didekorasi
                Buku objekAsli = buku;
                if (isLangka) {
                    // Karena kita butuh mengambil info spesifik (rak/ukuran) dari class konkret asli
                    objekAsli = membongkarDecorator(buku);
                }

                // Simpan berdasarkan tipe konkret internalnya masing-masing
                if (objekAsli instanceof BukuFisik) {
                    String info = objekAsli.tampilkanInfo();
                    // Ekstraksi posisi Rak dari teks format asli BukuFisik
                    String rak = "Rak-Umum";
                    if (info.contains("Rak: ")) {
                        rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                    }
                    // Format baru: ID,Judul,Pengarang,Genre,Rak,Status,isLangka
                    outFisik.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, rak, status, isLangka);
                    
                } else if (objekAsli instanceof EBook) {
                    String info = objekAsli.tampilkanInfo();
                    // Ekstraksi ukuran file dari teks format asli EBook
                    String ukuran = "Unknown-Size";
                    if (info.contains("Digital: ")) {
                        ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                    }
                    // Format baru: ID,Judul,Pengarang,Genre,UkuranFile,Status,isLangka
                    outEbook.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, ukuran, status, isLangka);
                }
            }

            // 2. Simpan Riwayat Transaksi Global
            for (Transaksi t : daftarTransaksi) {
                outTrx.printf("%s,%s,%s,%s,%s\n", t.getIdTransaksi(), t.getNamaAnggota(), t.getIdBuku(), t.getJudulBuku(), t.getStatusTransaksi());
            }

        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal melakukan sinkronisasi data permanen ke berkas CSV.");
        }
    }

    // Fungsi helper reflektif untuk mengambil referensi target di dalam decorator
    private Buku membongkarDecorator(Buku bukuDekor) {
        try {
            // Menggunakan teknik Java Reflection secara aman untuk mengambil field "bukuYangDekor" milik abstract class BukuDecorator
            java.lang.reflect.Field field = com.perpustakaan.model.BukuDecorator.class.getDeclaredField("bukuYangDekor");
            field.setAccessible(true);
            return (Buku) field.get(bukuDekor);
        } catch (Exception e) {
            return bukuDekor; // fallback jika gagal
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
                
                // 1. Instansiasi objek melalui Factory Method Pattern
                Buku buku = BukuFactory.buatBuku("FISIK", data[0], data[1], data[2], data[3], data[4]);
                
                // 2. Rekonstruksi State Internal Objek Buku
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                // 3. Rekonstruksi Bungkus Decorator Pattern jika di CSV bernilai true
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
                
                // 1. Instansiasi objek melalui Factory Method Pattern
                Buku buku = BukuFactory.buatBuku("EBOOK", data[0], data[1], data[2], data[3], data[4]);
                
                // 2. Rekonstruksi State Internal Objek Buku
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                // 3. Rekonstruksi Bungkus Decorator Pattern jika di CSV bernilai true
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