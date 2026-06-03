package com.perpustakaan.strategy;

import com.perpustakaan.model.Buku;
import java.util.ArrayList;
import java.util.List;

public class CariBerdasarkanGenre implements PencarianStrategy {
    
    public static final String[] GENRE_LIST = {"Teknologi", "Fiksi", "Sains", "Sejarah"};

    @Override
    public List<Buku> eksekusiCari(List<Buku> daftarBuku, String keyword) {
        List<Buku> hasilCari = new ArrayList<>();
        for (Buku buku : daftarBuku) {
            if (buku.getGenre().equalsIgnoreCase(keyword)) {
                hasilCari.add(buku);
            }
        }
        return hasilCari;
    }
}