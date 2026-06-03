package com.perpustakaan.state;

import com.perpustakaan.model.Buku;

public class TersediaState implements BukuState {

    @Override
    public void pinjam(Buku buku) {
        System.out.println("[SISTEM]: Berhasil meminjam buku \"" + buku.getJudul() + "\".");
        buku.setState(new DipinjamState());
    }

    @Override
    public void kembalikan(Buku buku) {
        System.out.println("[PERINGATAN]: Buku \"" + buku.getJudul() + "\" sebenarnya sudah ada di rak (tidak sedang dipinjam).");
    }

    @Override
    public String getStatusName() {
        return "Tersedia";
    }
}