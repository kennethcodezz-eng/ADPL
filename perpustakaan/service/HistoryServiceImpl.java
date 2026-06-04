package com.perpustakaan.service;

import com.perpustakaan.core.LibraryDatabase;
import com.perpustakaan.model.Transaksi;
import java.util.List;

public class HistoryServiceImpl implements HistoryService {
    private LibraryDatabase db;
    private int counterTransaksi = 1;

    public HistoryServiceImpl() {
        this.db = LibraryDatabase.getInstance();
    }

    @Override
    public void catatPeminjaman(String namaAnggota, String idBuku, String judulBuku) {
        String idTrx = "TRX" + String.format("%03d", counterTransaksi++);
        Transaksi trxNew = new Transaksi(idTrx, namaAnggota, idBuku, judulBuku);
        db.tambahTransaksi(trxNew);
    }

    @Override
    public void catatPengembalian(String namaAnggota, String idBuku) {
        for (Transaksi t : db.ambilSemuaTransaksi()) {
            if (t.getIdBuku().equals(idBuku) && t.getNamaAnggota().equalsIgnoreCase(namaAnggota) 
                    && t.getStatusTransaksi().equals("DIPINJAM")) {
                t.setStatusTransaksi("DIKEMBALIKAN");
                break;
            }
        }
    }

    @Override
    public void lihatBukuDipinjamAnggota(String namaAnggota) {
        System.out.println("\n=== BUKU YANG SEDANG DIPINJAM OLEH: " + namaAnggota.toUpperCase() + " ===");
        boolean adaBuku = false;
        for (Transaksi t : db.ambilSemuaTransaksi()) {
            if (t.getNamaAnggota().equalsIgnoreCase(namaAnggota) && t.getStatusTransaksi().equals("DIPINJAM")) {
                com.perpustakaan.model.Buku b = db.ambilBukuBerdasarkanId(t.getIdBuku());
                if (b != null) {
                    System.out.println("- " + b.tampilkanInfo());
                    adaBuku = true;
                }
            }
        }
        if (!adaBuku) {
            System.out.println("[INFO]: Anda tidak memiliki pinjaman buku aktif saat ini.");
        }
    }

    @Override
    public void lihatSemuaRiwayatTransaksi() {
        System.out.println("\n=== SEMUA RIWAYAT TRANSAKSI PERPUSTAKAAN ===");
        List<Transaksi> semuaTrx = db.ambilSemuaTransaksi();
        if (semuaTrx.isEmpty()) {
            System.out.println("[INFO]: Belum ada riwayat transaksi apapun di sistem.");
            return;
        }
        for (Transaksi t : semuaTrx) {
            System.out.println(t.tampilkanInfoTransaksi());
        }
    }
}