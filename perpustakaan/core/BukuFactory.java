package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
import com.perpustakaan.model.BukuFisik;
import com.perpustakaan.model.EBook;

public class BukuFactory {
    
    public static Buku buatBuku(String tipe, String id, String judul, String pengarang, String genre, String infoSpesifik) {
        if (tipe.equalsIgnoreCase("FISIK")) {
            return new BukuFisik(id, judul, pengarang, genre, infoSpesifik);
        } else if (tipe.equalsIgnoreCase("EBOOK")) {
            return new EBook(id, judul, pengarang, genre, infoSpesifik);
        }
        return null;
    }
}