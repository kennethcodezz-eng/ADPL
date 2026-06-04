package com.perpustakaan.model;

public abstract class BukuDecorator extends Buku {
    protected Buku bukuYangDekor;

    public BukuDecorator(Buku buku) {
        // Meneruskan atribut dasar ke superclass Buku
        super(buku.getId(), buku.getJudul(), buku.getPengarang(), buku.getGenre());
        this.bukuYangDekor = buku;
    }

    // Override getter agar selalu sinkron dengan state buku asli di dalamnya
    @Override
    public com.perpustakaan.state.BukuState getState() {
        return bukuYangDekor.getState();
    }

    @Override
    public void setState(com.perpustakaan.state.BukuState state) {
        bukuYangDekor.setState(state);
    }
}