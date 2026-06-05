package com.perpustakaan.strategy;

import com.perpustakaan.model.Buku;
import java.util.ArrayList;
import java.util.List;

public class CariBerdasarkanGenre implements PencarianStrategy {
    
    // Sekarang penampung list genre dibuat dinamis tanpa hard-code data awal
    public static final List<String> GENRE_LIST = new ArrayList<>();

    // Fungsi pembantu untuk menambah genre baru ke dalam list RAM (Runtime)
    public static void tambahGenreBaruKeList(String genreBaru) {
        for (String g : GENRE_LIST) {
            if (g.equalsIgnoreCase(genreBaru.trim())) {
                return; // Jika sudah ada, abaikan agar tidak duplikat
            }
        }
        GENRE_LIST.add(genreBaru.trim());
    }

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