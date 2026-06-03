package com.perpustakaan.main;

import com.perpustakaan.core.BukuFactory;
import com.perpustakaan.core.LibraryDatabase;
import com.perpustakaan.model.Buku;
import com.perpustakaan.service.LibraryServiceImpl;
import com.perpustakaan.service.LibraryServiceProxy;
import com.perpustakaan.strategy.CariBerdasarkanGenre;
import com.perpustakaan.strategy.CariBerdasarkanJudul;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.util.Scanner(System.in));
    private static String currentRole = "ANGGOTA"; // Default role awal
    private static LibraryServiceProxy serviceProxy;

    public static void main(String[] args) {
        // 1. Inisialisasi Service Utama & Proxy Security
        LibraryServiceImpl realService = new LibraryServiceImpl();
        serviceProxy = new LibraryServiceProxy(realService, currentRole);

        // 2. Data Seeding Otomatis (Menggunakan Factory Method & Ditampung Singleton)
        LibraryDatabase db = LibraryDatabase.getInstance();
        db.tambahBuku(BukuFactory.buatBuku("FISIK", "B01", "Java coding Dasar", "Robert", "Teknologi", "Rak-A1"));
        db.tambahBuku(BukuFactory.buatBuku("EBOOK", "B02", "Laskar Pelangi", "Andrea Hirata", "Fiksi", "12MB"));
        db.tambahBuku(BukuFactory.buatBuku("FISIK", "B03", "Asal Usul Semesta", "Stephen H.", "Sains", "Rak-B3"));

        // 3. Loop Menu Aplikasi CLI
        boolean berjalan = true;
        while (berjalan) {
            System.out.println("\n==================================================");
            System.out.println("  SELAMAT DATANG DI SISTEM MANAJEMEN PERPUSTAKAAN  ");
            System.out.println("==================================================");
            System.out.println("Peran Anda Saat Ini: [" + currentRole + "]");
            System.out.println("1. Masuk sebagai Anggota");
            System.out.println("2. Masuk sebagai Pustakawan (Admin)");
            System.out.println("3. Menu Operasional Perpustakaan");
            System.out.println("4. Keluar Aplikasi");
            System.out.print("Pilih menu (1-4): ");
            
            int pilihan = membacaInputAngka();
            switch (pilihan) {
                case 1:
                    currentRole = "ANGGOTA";
                    serviceProxy.setUserRoleSaatIni(currentRole);
                    System.out.println("[SISTEM]: Peran berhasil diubah ke ANGGOTA.");
                    break;
                case 2:
                    currentRole = "PUSTAKAWAN";
                    serviceProxy.setUserRoleSaatIni(currentRole);
                    System.out.println("[SISTEM]: Peran berhasil diubah ke PUSTAKAWAN.");
                    break;
                case 3:
                    tampilkanMenuOperasional();
                    break;
                case 4:
                    berjalan = false;
                    System.out.println("[SISTEM]: Terima kasih telah menggunakan aplikasi perpustakaan!");
                    break;
                default:
                    System.out.println("[PERINGATAN]: Pilihan tidak valid!");
            }
        }
    }

    private static void tampilkanMenuOperasional() {
        boolean diMenuLayanan = true;
        while (diMenuLayanan) {
            System.out.println("\n--- MENU OPERASIONAL PERPUSTAKAAN ---");
            System.out.println("Status Login: [" + currentRole + "]");
            System.out.println("1. Lihat Semua Koleksi Buku");
            System.out.println("2. Cari Buku Berdasarkan Judul");
            System.out.println("3. Cari Buku Berdasarkan Pilihan Genre");
            System.out.println("4. Pinjam Buku (Semua Role)");
            System.out.println("5. Kembalikan Buku (Semua Role)");
            System.out.println("6. Tambah Buku Baru (Khusus Pustakawan)");
            System.out.println("7. Kembali ke Menu Utama");
            System.out.print("Pilih opsi (1-7): ");

            int opsi = membacaInputAngka();
            switch (opsi) {
                case 1:
                    serviceProxy.lihatSemuaBuku();
                    break;
                case 2:
                    System.out.print("Masukkan kata kunci judul: ");
                    String keywordJudul = scanner.nextLine();
                    serviceProxy.cariBuku(new CariBerdasarkanJudul(), keywordJudul);
                    break;
                case 3:
                    System.out.println("Pilih Genre yang Tersedia:");
                    for (int i = 0; i < CariBerdasarkanGenre.GENRE_LIST.length; i++) {
                        System.out.println((i + 1) + ". " + CariBerdasarkanGenre.GENRE_LIST[i]);
                    }
                    System.out.print("Pilih angka genre: ");
                    int indexGenre = membacaInputAngka() - 1;
                    if (indexGenre >= 0 && indexGenre < CariBerdasarkanGenre.GENRE_LIST.length) {
                        String genreDipilih = CariBerdasarkanGenre.GENRE_LIST[indexGenre];
                        serviceProxy.cariBuku(new CariBerdasarkanGenre(), genreDipilih);
                    } else {
                        System.out.println("[ERROR]: Pilihan genre tidak terdaftar.");
                    }
                    break;
                case 4:
                    System.out.print("Masukkan ID Buku yang mau dipinjam: ");
                    String idPinjam = scanner.nextLine();
                    serviceProxy.pinjamBukuLayanan(idPinjam);
                    break;
                case 5:
                    System.out.print("Masukkan ID Buku yang dikembalikan: ");
                    String idKembali = scanner.nextLine();
                    serviceProxy.kembalikanBukuLayanan(idKembali);
                    break;
                case 6:
                    prosesInputBukuBaru();
                    break;
                case 7:
                    diMenuLayanan = false;
                    break;
                default:
                    System.out.println("[PERINGATAN]: Opsi tidak valid!");
            }
        }
    }

    private static void prosesInputBukuBaru() {
        System.out.println("\n--- FORM TAMBAH BUKU BARU ---");
        System.out.print("Masukkan Tipe Buku (FISIK / EBOOK): ");
        String tipe = scanner.nextLine().toUpperCase();
        System.out.print("Masukkan ID Buku (misal: B04): ");
        String id = scanner.nextLine();
        System.out.print("Masukkan Judul Buku: ");
        String judul = scanner.nextLine();
        System.out.print("Masukkan Pengarang: ");
        String pengarang = scanner.nextLine();
        
        System.out.println("Pilih Genre:");
        for (int i = 0; i < CariBerdasarkanGenre.GENRE_LIST.length; i++) {
            System.out.println((i + 1) + ". " + CariBerdasarkanGenre.GENRE_LIST[i]);
        }
        System.out.print("Pilihan angka genre: ");
        int indexGenre = membacaInputAngka() - 1;
        String genre = (indexGenre >= 0 && indexGenre < CariBerdasarkanGenre.GENRE_LIST.length) 
                        ? CariBerdasarkanGenre.GENRE_LIST[indexGenre] : "Umum";

        String infoSpesifik = "";
        if (tipe.equals("FISIK")) {
            System.out.print("Masukkan Nomor Rak (misal: Rak-C2): ");
            infoSpesifik = scanner.nextLine();
        } else if (tipe.equals("EBOOK")) {
            System.out.print("Masukkan Ukuran File (misal: 20MB): ");
            infoSpesifik = scanner.nextLine();
        }

        // Bikin objek via Factory Method
        Buku bukuBaru = BukuFactory.buatBuku(tipe, id, judul, pengarang, genre, infoSpesifik);
        if (bukuBaru != null) {
            // Dicoba dimasukkan lewat Proxy Security
            serviceProxy.daftarkanBukuBaru(bukuBaru);
        } else {
            System.out.println("[ERROR]: Gagal membuat buku. Pastikan Tipe Buku benar!");
        }
    }

    private static int membacaInputAngka() {
        try {
            int angka = Integer.parseInt(scanner.nextLine());
            return angka;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}