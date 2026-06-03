package com.perpustakaan.state;

import com.perpustakaan.model.Buku;

public class DipinjamState implements BukuState {

    @Override
    public void pinjam(Buku buku) {
        System.out.println("[ERROR]: Gagal! Buku \"" + buku.getJudul() + "\" sedang dipinjam oleh anggota lain.");
    }

    @Override
    public void kembalikan(Buku buku) {
        System.out.println("[SISTEM]: Buku \"" + buku.getJudul() + "\" telah berhasil dikembalikan ke rak.");
        buku.setState(new TersediaState());
    }

    @Override
    public String getStatusName() {
        return "Dipinjam";
    }
}