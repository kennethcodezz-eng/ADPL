package com.perpustakaan.model;

import com.perpustakaan.state.BukuState;
import com.perpustakaan.state.TersediaState;

public abstract class Buku {
    protected String id;
    protected String judul;
    protected String pengarang;
    protected String genre;
    protected BukuState state;

    public Buku(String id, String judul, String pengarang, String genre) {
        this.id = id;
        this.judul = judul;
        this.pengarang = pengarang;
        this.genre = genre;
        this.state = new TersediaState(); // Default awal saat instansiasi objek
    }

    public String getId() { return id; }
    public String getJudul() { return judul; }
    public String getPengarang() { return pengarang; }
    public String getGenre() { return genre; }
    
    public BukuState getState() { return state; }
    public void setState(BukuState state) { this.state = state; }

    public void pinjamBuku() {
        state.pinjam(this);
    }

    public void kembalikanBuku() {
        state.kembalikan(this);
    }

    public abstract String tampilkanInfo();
}