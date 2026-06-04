package com.perpustakaan.model;

public class Transaksi {
    private String idTransaksi;
    private String namaAnggota;
    private String idBuku;
    private String judulBuku;
    private String statusTransaksi; // "DIPINJAM" atau "DIKEMBALIKAN"

    public Transaksi(String idTransaksi, String namaAnggota, String idBuku, String judulBuku) {
        this.idTransaksi = idTransaksi;
        this.namaAnggota = namaAnggota;
        this.idBuku = idBuku;
        this.judulBuku = judulBuku;
        this.statusTransaksi = "DIPINJAM";
    }

    public String getIdTransaksi() { return idTransaksi; }
    public String getNamaAnggota() { return namaAnggota; }
    public String getIdBuku() { return idBuku; }
    public String getJudulBuku() { return judulBuku; }
    public String getStatusTransaksi() { return statusTransaksi; }
    
    public void setStatusTransaksi(String statusTransaksi) { 
        this.statusTransaksi = statusTransaksi; 
    }

    public String tampilkanInfoTransaksi() {
        return String.format("[%s] %s meminjam \"%s\" (%s) -> Status: %s", 
                idTransaksi, namaAnggota, judulBuku, idBuku, statusTransaksi);
    }
}