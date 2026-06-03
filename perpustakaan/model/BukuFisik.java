package com.perpustakaan.model;

public class BukuFisik extends Buku {
    private String nomorRak;

    public BukuFisik(String id, String judul, String pengarang, String genre, String nomorRak) {
        super(id, judul, pengarang, genre);
        this.nomorRak = nomorRak;
    }

    @Override
    public String tampilkanInfo() {
        return String.format("[%s] %s - %s | Genre: %s | Rak: %s -> [%s]", 
                id, judul, pengarang, genre, nomorRak, state.getStatusName());
    }
}