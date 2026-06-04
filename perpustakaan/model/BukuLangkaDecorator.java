package com.perpustakaan.model;

public class BukuLangkaDecorator extends BukuDecorator {
    private double tarifDendaKhusus;

    public BukuLangkaDecorator(Buku buku, double tarifDendaKhusus) {
        super(buku);
        this.tarifDendaKhusus = tarifDendaKhusus;
    }

    @Override
    public String tampilkanInfo() {
        // Menambahkan teks dekorasi [KOLEKSI LANGKA] di depan info asli buku
        return "⭐ [KOLEKSI LANGKA] " + bukuYangDekor.tampilkanInfo() + " (Tarif Denda: Rp " + tarifDendaKhusus + "/hari)";
    }
}