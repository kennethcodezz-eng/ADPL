package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
import com.perpustakaan.model.Transaksi; // Perlu di-import karena mengelola model Transaksi
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryDatabase {
    private static LibraryDatabase instance;
    private Map<String, Buku> tabelBuku;
    
    // --- PERUBAHAN: Penampung Baru Khusus Riwayat Transaksi ---
    private List<Transaksi> daftarTransaksi; 

    private LibraryDatabase() {
        tabelBuku = new HashMap<>();
        
        // --- PERUBAHAN: Inisialisasi list transaksi saat database pertama kali aktif ---
        daftarTransaksi = new ArrayList<>(); 
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
    }

    public Buku ambilBukuBerdasarkanId(String id) {
        return tabelBuku.get(id);
    }

    public List<Buku> ambilSemuaBuku() {
        return new ArrayList<>(tabelBuku.values());
    }

    // --- PERUBAHAN: METHOD BARU UNTUK MANAJEMEN RIWAYAT ---
    
    // Dipanggil oleh HistoryServiceImpl untuk menyimpan log transaksi baru
    public void tambahTransaksi(Transaksi transaksi) {
        daftarTransaksi.add(transaksi);
    }

    // Dipanggil oleh HistoryServiceImpl untuk melihat/memfilter riwayat transaksi
    public List<Transaksi> ambilSemuaTransaksi() {
        return daftarTransaksi;
    }
}