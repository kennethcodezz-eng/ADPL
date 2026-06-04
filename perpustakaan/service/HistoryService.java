package com.perpustakaan.service;

import com.perpustakaan.model.Transaksi;
import java.util.List;

public interface HistoryService {
    void catatPeminjaman(String namaAnggota, String idBuku, String judulBuku);
    void catatPengembalian(String namaAnggota, String idBuku);
    void lihatBukuDipinjamAnggota(String namaAnggota);
    void lihatSemuaRiwayatTransaksi();
}