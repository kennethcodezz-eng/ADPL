package com.perpustakaan.service;

import com.perpustakaan.model.Buku;
import com.perpustakaan.strategy.PencarianStrategy;

public interface LibraryService {
    void lihatSemuaBuku();
    void cariBuku(PencarianStrategy strategy, String keyword);
    void daftarkanBukuBaru(Buku buku);
    
    // Mengubah return type menjadi boolean untuk pelaporan status ke Main & HistoryService
    boolean pinjamBukuLayanan(String idBuku, String namaAnggota);
    boolean kembalikanBukuLayanan(String idBuku, String namaAnggota);
}