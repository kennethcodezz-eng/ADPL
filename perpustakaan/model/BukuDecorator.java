package com.perpustakaan.model;

import com.perpustakaan.state.BukuState;

public abstract class BukuDecorator extends Buku {
    protected Buku bukuYangDekor;

    public BukuDecorator(Buku buku) {
        super(buku.getId(), buku.getJudul(), buku.getPengarang(), buku.getGenre());
        this.bukuYangDekor = buku;
    }

    @Override
    public String getId() {
        return bukuYangDekor.getId();
    }

    @Override
    public String getJudul() {
        return bukuYangDekor.getJudul();
    }

    @Override
    public String getPengarang() {
        return bukuYangDekor.getPengarang();
    }

    @Override
    public String getGenre() {
        return bukuYangDekor.getGenre();
    }

    @Override
    public BukuState getState() {
        return bukuYangDekor.getState();
    }

    @Override
    public void setState(BukuState state) {
        bukuYangDekor.setState(state);
    }

    @Override
    public void pinjamBuku() {
        bukuYangDekor.pinjamBuku();
    }

    @Override
    public void kembalikanBuku() {
        bukuYangDekor.kembalikanBuku();
    }
}