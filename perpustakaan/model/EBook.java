package com.perpustakaan.model;

public class EBook extends Buku {
    private String ukuranFile;

    public EBook(String id, String judul, String pengarang, String genre, String ukuranFile) {
        super(id, judul, pengarang, genre);
        this.ukuranFile = ukuranFile;
    }

    @Override
    public String tampilkanInfo() {
        return String.format("[%s] %s - %s | Genre: %s | Digital: %s -> [%s]", 
                id, judul, pengarang, genre, ukuranFile, state.getStatusName());
    }
}