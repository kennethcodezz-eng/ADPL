package com.perpustakaan.service;

import com.perpustakaan.core.LibraryDatabase;
import com.perpustakaan.model.Buku;
import com.perpustakaan.strategy.PencarianStrategy;
import java.util.List;

public class LibraryServiceImpl implements LibraryService {
    private LibraryDatabase db;

    public LibraryServiceImpl() {
        this.db = LibraryDatabase.getInstance();
    }

    @Override
    public void lihatSemuaBuku() {
        List<Buku> daftarBuku = db.ambilSemuaBuku();
        if (daftarBuku.isEmpty()) {
            System.out.println("[INFO]: Koleksi perpustakaan masih kosong.");
            return;
        }
        System.out.println("\n=== DAFTAR KOLEKSI BUKU ===");
        for (Buku buku : daftarBuku) {
            System.out.println(buku.tampilkanInfo());
        }
    }

    @Override
    public void cariBuku(PencarianStrategy strategy, String keyword) {
        List<Buku> semuaBuku = db.ambilSemuaBuku();
        List<Buku> hasilCari = strategy.eksekusiCari(semuaBuku, keyword);

        if (hasilCari.isEmpty()) {
            System.out.println("[INFO]: Buku tidak ditemukan dengan kata kunci \"" + keyword + "\".");
            return;
        }
        System.out.println("\n=== HASIL PENCARIAN ===");
        for (Buku buku : hasilCari) {
            System.out.println(buku.tampilkanInfo());
        }
    }

    @Override
    public void daftarkanBukuBaru(Buku buku) {
        db.tambahBuku(buku);
        System.out.println("[SISTEM]: Berhasil mendaftarkan buku \"" + buku.getJudul() + "\" ke database.");
    }

    @Override
    public void pinjamBukuLayanan(String idBuku) {
        Buku buku = db.ambilBukuBerdasarkanId(idBuku);
        if (buku == null) {
            System.out.println("[ERROR]: Buku dengan ID \"" + idBuku + "\" tidak ditemukan!");
            return;
        }
        buku.pinjamBuku();
    }

    @Override
    public void kembalikanBukuLayanan(String idBuku) {
        Buku buku = db.ambilBukuBerdasarkanId(idBuku);
        if (buku == null) {
            System.out.println("[ERROR]: Buku dengan ID \"" + idBuku + "\" tidak ditemukan!");
            return;
        }
        buku.kembalikanBuku();
    }
}