package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
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
        simpanKeFile(); // Tulis ke CSV setiap ada buku baru
    }

    public Buku ambilBukuBerdasarkanId(String id) { return tabelBuku.get(id); }
    public List<Buku> ambilSemuaBuku() { return new ArrayList<>(tabelBuku.values()); }

    // --- MANAJEMEN RIWAYAT ---
    public void tambahTransaksi(Transaksi transaksi) {
        daftarTransaksi.add(transaksi);
        simpanKeFile(); // Tulis ke CSV setiap ada transaksi baru
    }
    public List<Transaksi> ambilSemuaTransaksi() { return daftarTransaksi; }

    
    // ==========================================================
    // LOGIKA PENYIMPANAN KE CSV (WRITER)
    // ==========================================================
    public void simpanKeFile() {
        try (PrintWriter outFisik = new PrintWriter(new FileWriter(FILE_FISIK));
             PrintWriter outEbook = new PrintWriter(new FileWriter(FILE_EBOOK));
             PrintWriter outTrx = new PrintWriter(new FileWriter(FILE_TRANSAKSI))) {
            
            // 1. Simpan Data Buku
            for (Buku buku : tabelBuku.values()) {
                String id = buku.getId();
                String judul = buku.getJudul();
                String pengarang = buku.getPengarang();
                String genre = buku.getGenre();
                String status = buku.getState().getStatusName();
                String info = buku.tampilkanInfo();

                // Trik membaca info spesifik tanpa mengubah class model milikmu
                if (info.contains("Rak: ")) {
                    String rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                    // Format: ID,Judul,Pengarang,Genre,Rak,Status
                    outFisik.printf("%s,%s,%s,%s,%s,%s\n", id, judul, pengarang, genre, rak, status);
                } else if (info.contains("Digital: ")) {
                    String ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                    // Format: ID,Judul,Pengarang,Genre,UkuranMB,Status
                    outEbook.printf("%s,%s,%s,%s,%s,%s\n", id, judul, pengarang, genre, ukuran, status);
                }
            }

            // 2. Simpan Data Transaksi
            for (Transaksi t : daftarTransaksi) {
                // Format: ID_Trx,Nama,ID_Buku,Judul_Buku,Status_Trx
                outTrx.printf("%s,%s,%s,%s,%s\n", t.getIdTransaksi(), t.getNamaAnggota(), t.getIdBuku(), t.getJudulBuku(), t.getStatusTransaksi());
            }

        } catch (IOException e) {
            System.out.println("[ERROR]: Gagal menulis ke file CSV.");
        }
    }
    
    // ==========================================================
    // LOGIKA PEMBACAAN DARI CSV (READER)
    // ==========================================================
    private void muatDariFile() {
        muatBukuFisik();
        muatEbook();
        muatTransaksi();
    }

    private void muatBukuFisik() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_FISIK))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 👇 BARIS PELINDUNG: Abaikan jika baris kosong
                if (line.trim().isEmpty()) continue; 
                
                String[] data = line.split(","); 
                // Pelindung tambahan: pastikan kolomnya pas 6
                if (data.length < 6) continue; 
                
                Buku buku = BukuFactory.buatBuku("FISIK", data[0], data[1], data[2], data[3], data[4]);
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) buku.setState(new DipinjamState());
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { /* Abaikan diam-diam jika file belum ada */ }
    }

    private void muatEbook() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_EBOOK))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 👇 BARIS PELINDUNG
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 6) continue;
                
                Buku buku = BukuFactory.buatBuku("EBOOK", data[0], data[1], data[2], data[3], data[4]);
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) buku.setState(new DipinjamState());
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { /* Abaikan diam-diam jika file belum ada */ }
    }

    private void muatTransaksi() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_TRANSAKSI))) {
            String line;
            while ((line = br.readLine()) != null) {
                // 👇 BARIS PELINDUNG (Ini yang menyelesaikan error-mu)
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 5) continue;
                
                Transaksi trx = new Transaksi(data[0], data[1], data[2], data[3]);
                trx.setStatusTransaksi(data[4].trim());
                daftarTransaksi.add(trx);
            }
        } catch (IOException e) { /* Abaikan diam-diam jika file belum ada */ }
    }
}