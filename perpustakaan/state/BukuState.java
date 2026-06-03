package com.perpustakaan.state;

import com.perpustakaan.model.Buku;

public interface BukuState {
    void pinjam(Buku buku);
    void kembalikan(Buku buku);
    String getStatusName();
}