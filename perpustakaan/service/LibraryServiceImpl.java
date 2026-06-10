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
        // 1. Ambil semua data buku dari database RAM
        List<com.perpustakaan.model.Buku> daftarBuku = db.ambilSemuaBuku();

        if (daftarBuku.isEmpty()) {
            System.out.println("[INFO]: Belum ada koleksi buku di perpustakaan.");
            return;
        }

        // 2. PROSES SORTING BERDASARKAN ID BUKU (A-Z)
        // Kita bandingkan ID Buku 1 dengan ID Buku 2 secara alfabetis/numerik
        daftarBuku.sort((b1, b2) -> b1.getId().compareToIgnoreCase(b2.getId()));

        // 3. Cetak daftar buku yang sudah rapi dan berurutan
        System.out.println("\n=================================================================================");
        System.out.println("                         DAFTAR LENGKAP KOLEKSI BUKU                             ");
        System.out.println("=================================================================================");
        for (com.perpustakaan.model.Buku buku : daftarBuku) {
            System.out.println(buku.tampilkanInfo());
        }
        System.out.println("=================================================================================");
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

    // Mengembalikan status kelayakan (Boolean) agar kelas Main tahu transaksinya sukses/gagal
    @Override
    public boolean pinjamBukuLayanan(String idBuku, String namaAnggota) {
        Buku buku = db.ambilBukuBerdasarkanId(idBuku);
        if (buku == null) {
            System.out.println("[ERROR]: Buku dengan ID \"" + idBuku + "\" tidak ditemukan!");
            return false;
        }

        String stateAwal = buku.getState().getStatusName();
        buku.pinjamBuku(); 
        String stateAkhir = buku.getState().getStatusName();

        return !stateAwal.equals(stateAkhir) && stateAkhir.equalsIgnoreCase("Dipinjam");
    }

    @Override
    public boolean kembalikanBukuLayanan(String idBuku, String namaAnggota) {
        Buku buku = db.ambilBukuBerdasarkanId(idBuku);
        if (buku == null) {
            System.out.println("[ERROR]: Buku dengan ID \"" + idBuku + "\" tidak ditemukan!");
            return false;
        }

        String stateAwal = buku.getState().getStatusName();
        buku.kembalikanBuku(); 
        String stateAkhir = buku.getState().getStatusName();

        return !stateAwal.equals(stateAkhir) && stateAkhir.equalsIgnoreCase("Tersedia");
    }

    @Override
    public boolean hapusBukuLayanan(String idBuku) {
        return db.hapusBukuBerdasarkanId(idBuku);
    }
}