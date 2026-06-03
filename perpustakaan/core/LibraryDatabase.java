package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryDatabase {
    private static LibraryDatabase instance;
    private Map<String, Buku> tabelBuku;

    private LibraryDatabase() {
        tabelBuku = new HashMap<>();
    }

    public static LibraryDatabase getInstance() {
        if (instance == null) {
            instance = new LibraryDatabase();
        }
        return instance;
    }

    public void tambahBuku(Buku buku) {
        tabelBuku.put(buku.getId(), buku);
    }

    public Buku ambilBukuBerdasarkanId(String id) {
        return tabelBuku.get(id);
    }

    public List<Buku> ambilSemuaBuku() {
        return new ArrayList<>(tabelBuku.values());
    }
}