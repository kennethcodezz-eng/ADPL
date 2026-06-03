package com.perpustakaan.service;

import com.perpustakaan.model.Buku;
import com.perpustakaan.strategy.PencarianStrategy;

public interface LibraryService {
    void lihatSemuaBuku();
    void cariBuku(PencarianStrategy strategy, String keyword);
    void daftarkanBukuBaru(Buku buku);
    void pinjamBukuLayanan(String idBuku);
    void kembalikanBukuLayanan(String idBuku);
}