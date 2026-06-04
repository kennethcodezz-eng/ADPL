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
        // Semua role (Anggota, Pustakawan, Guest) diizinkan melihat katalog buku
        realService.lihatSemuaBuku();
    }

    @Override
    public void cariBuku(PencarianStrategy strategy, String keyword) {
        // Semua role diizinkan melakukan pencarian buku
        realService.cariBuku(strategy, keyword);
    }

    @Override
    public boolean pinjamBukuLayanan(String idBuku, String namaAnggota) {
        // Meneruskan eksekusi pinjam dan mengembalikan status sukses/gagal berupa boolean
        return realService.pinjamBukuLayanan(idBuku, namaAnggota);
    }

    @Override
    public boolean kembalikanBukuLayanan(String idBuku, String namaAnggota) {
        // Meneruskan eksekusi kembali dan mengembalikan status sukses/gagal berupa boolean
        return realService.kembalikanBukuLayanan(idBuku, namaAnggota);
    }

    @Override
    public void daftarkanBukuBaru(Buku buku) {
        // PROTEKSI KEAMANAN PROXY: Memblokir Anggota agar tidak bisa menambah koleksi buku
        if (userRoleSaatIni.equalsIgnoreCase("PUSTAKAWAN")) {
            realService.daftarkanBukuBaru(buku);
        } else {
            System.out.println("\n[PROXY ERROR]: Akses Ditolak! Peran Anda saat ini: " + userRoleSaatIni + ".");
            System.out.println("               Fitur 'Tambah Buku Baru' hanya diizinkan untuk PUSTAKAWAN.");
        }
    }
}