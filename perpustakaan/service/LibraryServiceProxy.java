package com.perpustakaan.service;

import com.perpustakaan.model.Buku;
import com.perpustakaan.strategy.PencarianStrategy;

public class LibraryServiceProxy implements LibraryService {
    private LibraryService realService;
    private String userRoleSaatIni;

    public LibraryServiceProxy(LibraryService realService, String userRoleSaatIni) {
        this.realService = realService;
        this.userRoleSaatIni = userRoleSaatIni;
    }

    public void setUserRoleSaatIni(String userRoleSaatIni) {
        this.userRoleSaatIni = userRoleSaatIni;
    }

    @Override
    public void lihatSemuaBuku() {
        realService.lihatSemuaBuku();
    }

    @Override
    public void cariBuku(PencarianStrategy strategy, String keyword) {
        realService.cariBuku(strategy, keyword);
    }

    @Override
    public void pinjamBukuLayanan(String idBuku) {
        realService.pinjamBukuLayanan(idBuku);
    }

    @Override
    public void kembalikanBukuLayanan(String idBuku) {
        realService.kembalikanBukuLayanan(idBuku);
    }

    @Override
    public void daftarkanBukuBaru(Buku buku) {
        if (userRoleSaatIni.equalsIgnoreCase("PUSTAKAWAN")) {
            realService.daftarkanBukuBaru(buku);
        } else {
            System.out.println("\n[PROXY ERROR]: Akses Ditolak! Peran Anda saat ini: " + userRoleSaatIni + ".");
            System.out.println("               Fitur 'Tambah Buku Baru' hanya diizinkan untuk PUSTAKAWAN.");
        }
    }
}