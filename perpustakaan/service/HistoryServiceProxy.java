package com.perpustakaan.service;

public class HistoryServiceProxy implements HistoryService {
    private HistoryService realHistoryService;
    private String userRoleSaatIni;

    public HistoryServiceProxy(HistoryService realHistoryService, String userRoleSaatIni) {
        this.realHistoryService = realHistoryService;
        this.userRoleSaatIni = userRoleSaatIni;
    }

    public void setUserRoleSaatIni(String userRoleSaatIni) {
        this.userRoleSaatIni = userRoleSaatIni;
    }

    @Override
    public void catatPeminjaman(String namaAnggota, String idBuku, String judulBuku) {
        // Otomatis diteruskan saat transaksi valid
        realHistoryService.catatPeminjaman(namaAnggota, idBuku, judulBuku);
    }

    @Override
    public void catatPengembalian(String namaAnggota, String idBuku) {
        realHistoryService.catatPengembalian(namaAnggota, idBuku);
    }

    @Override
    public void lihatBukuDipinjamAnggota(String namaAnggota) {
        // Hak akses anggota diizinkan melihat data pribadinya sendiri
        realHistoryService.lihatBukuDipinjamAnggota(namaAnggota);
    }

    @Override
    public void lihatSemuaRiwayatTransaksi() {
        // PROTEKSI: Hanya Pustakawan yang boleh mengaudit seluruh transaksi sistem
        if (userRoleSaatIni.equalsIgnoreCase("ADMIN")) {
            realHistoryService.lihatSemuaRiwayatTransaksi();
        } else {
            System.out.println("\n[PROXY ERROR]: Akses Ditolak! Riwayat global hanya bisa dibuka oleh ADMIN.");
        }
    }
}