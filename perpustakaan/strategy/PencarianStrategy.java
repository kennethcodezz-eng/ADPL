package com.perpustakaan.strategy;

import com.perpustakaan.model.Buku;
import java.util.List;

public interface PencarianStrategy {
    List<Buku> eksekusiCari(List<Buku> daftarBuku, String keyword);
}