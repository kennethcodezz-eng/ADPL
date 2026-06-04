package com.perpustakaan.main;

import com.perpustakaan.core.BukuFactory;
import com.perpustakaan.core.LibraryDatabase;
import com.perpustakaan.model.Buku;
import com.perpustakaan.service.*;
import com.perpustakaan.strategy.CariBerdasarkanGenre;
import com.perpustakaan.strategy.CariBerdasarkanJudul;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static String currentRole = "GUEST"; 
    private static String namaUserAktif = ""; 
    
    // Kita deklarasikan dua tameng proxy terpisah
    private static LibraryServiceProxy libraryProxy;
    private static HistoryServiceProxy historyProxy;

    public static void main(String[] args) {
        // Inisialisasi Kelompok Layanan Buku
        LibraryServiceImpl realLibService = new LibraryServiceImpl();
        libraryProxy = new LibraryServiceProxy(realLibService, currentRole);

        // Inisialisasi Kelompok Layanan Riwayat (KELAS BARU)
        HistoryServiceImpl realHistService = new HistoryServiceImpl();
        historyProxy = new HistoryServiceProxy(realHistService, currentRole);

        // Data Seeding
        LibraryDatabase db = LibraryDatabase.getInstance();
        db.tambahBuku(BukuFactory.buatBuku("FISIK", "B01", "Java Coding Dasar", "Robert", "Teknologi", "Rak-A1"));
        db.tambahBuku(BukuFactory.buatBuku("EBOOK", "B02", "Laskar Pelangi", "Andrea Hirata", "Fiksi", "12MB"));
        Buku bSains = BukuFactory.buatBuku("FISIK", "B03", "Asal Usul Semesta", "Stephen H.", "Sains", "Rak-B3");
        db.tambahBuku(new com.perpustakaan.model.BukuLangkaDecorator(bSains, 15000.0));

        boolean berjalan = true;
        while (berjalan) {
            System.out.println("\n==================================================");
            System.out.println("  SELAMAT DATANG DI SISTEM MANAJEMEN PERPUSTAKAAN  ");
            System.out.println("==================================================");
            System.out.println("1. Masuk sebagai ANGGOTA PERPUSTAKAAN");
            System.out.println("2. Masuk sebagai PUSTAKAWAN (ADMIN)");
            System.out.println("3. Keluar Aplikasi");
            System.out.print("Pilihan Anda (1-3): ");
            
            int pilihan = membacaInputAngka();
            switch (pilihan) {
                case 1:
                    System.out.print("Masukkan Nama Anda: ");
                    namaUserAktif = scanner.nextLine();
                    currentRole = "ANGGOTA";
                    libraryProxy.setUserRoleSaatIni(currentRole);
                    historyProxy.setUserRoleSaatIni(currentRole); // Sinkronisasi role ke proxy riwayat
                    System.out.println("\n[SISTEM]: Login Berhasil sebagai Anggota: " + namaUserAktif.toUpperCase());
                    tampilkanMenuAnggota(); 
                    break;
                case 2:
                    namaUserAktif = "Pustakawan Admin";
                    currentRole = "PUSTAKAWAN";
                    libraryProxy.setUserRoleSaatIni(currentRole);
                    historyProxy.setUserRoleSaatIni(currentRole); // Sinkronisasi role ke proxy riwayat
                    System.out.println("\n[SISTEM]: Login Berhasil sebagai PUSTAKAWAN.");
                    tampilkanMenuPustakawan(); 
                    break;
                case 3:
                    berjalan = false;
                    System.out.println("\n[SISTEM]: Terima kasih!");
                    break;
                default:
                    System.out.println("[PERINGATAN]: Pilihan tidak valid!");
            }
        }
    }

    private static void tampilkanMenuAnggota() {
        boolean diMenuAnggota = true;
        while (diMenuAnggota) {
            System.out.println("\n--- DASHBOARD ANGGOTA ---");
            System.out.println("1. Lihat Semua Koleksi Buku");
            System.out.println("2. Cari Buku Berdasarkan Judul");
            System.out.println("3. Cari Buku Berdasarkan Pilihan Genre");
            System.out.println("4. Pinjam Buku");
            System.out.println("5. Kembalikan Buku");
            System.out.println("6. Lihat Daftar Buku yang Sedang Saya Pinjam");
            System.out.println("7. Logout");
            System.out.print("Pilih menu (1-7): ");

            int opsi = membacaInputAngka();
            switch (opsi) {
                case 1: libraryProxy.lihatSemuaBuku(); break;
                case 2:
                    System.out.print("Masukkan kata kunci judul: ");
                    String keywordJudul = scanner.nextLine();
                    libraryProxy.cariBuku(new CariBerdasarkanJudul(), keywordJudul);
                    break;
                case 3: prosesCariGenre(); break;
                case 4:
                    System.out.print("Masukkan ID Buku yang ingin dipinjam: ");
                    String idPinjam = scanner.nextLine();
                    // Jalankan layanan buku, jika sukses ganti state, catat ke history service
                    boolean suksesPinjam = libraryProxy.pinjamBukuLayanan(idPinjam, namaUserAktif);
                    if (suksesPinjam) {
                        Buku b = LibraryDatabase.getInstance().ambilBukuBerdasarkanId(idPinjam);
                        historyProxy.catatPeminjaman(namaUserAktif, idPinjam, b.getJudul());
                    }
                    break;
                case 5:
                    System.out.print("Masukkan ID Buku yang dikembalikan: ");
                    String idKembali = scanner.nextLine();
                    boolean suksesKembali = libraryProxy.kembalikanBukuLayanan(idKembali, namaUserAktif);
                    if (suksesKembali) {
                        historyProxy.catatPengembalian(namaUserAktif, idKembali);
                    }
                    break;
                case 6:
                    historyProxy.lihatBukuDipinjamAnggota(namaUserAktif); // Pindah tanggung jawab ke historyProxy
                    break;
                case 7:
                    diMenuAnggota = false;
                    currentRole = "GUEST";
                    namaUserAktif = "";
                    libraryProxy.setUserRoleSaatIni(currentRole);
                    historyProxy.setUserRoleSaatIni(currentRole);
                    System.out.println("[SISTEM]: Berhasil logout.");
                    break;
                default:
                    System.out.println("[PERINGATAN]: Menu tidak tersedia!");
            }
        }
    }

    private static void tampilkanMenuPustakawan() {
        boolean diMenuPustakawan = true;
        while (diMenuPustakawan) {
            System.out.println("\n--- DASHBOARD PUSTAKAWAN ---");
            System.out.println("1. Lihat Semua Koleksi Buku");
            System.out.println("2. Tambah / Daftarkan Buku Baru");
            System.out.println("3. Lihat Semua Riwayat Transaksi Global");
            System.out.println("4. Logout");
            System.out.print("Pilih menu (1-4): ");

            int opsi = membacaInputAngka();
            switch (opsi) {
                case 1: libraryProxy.lihatSemuaBuku(); break;
                case 2: prosesInputBukuBaru(); break;
                case 3: historyProxy.lihatSemuaRiwayatTransaksi(); break; // Diambil alih historyProxy
                case 4:
                    diMenuPustakawan = false;
                    currentRole = "GUEST";
                    namaUserAktif = "";
                    libraryProxy.setUserRoleSaatIni(currentRole);
                    historyProxy.setUserRoleSaatIni(currentRole);
                    System.out.println("[SISTEM]: Berhasil logout.");
                    break;
                default:
                    System.out.println("[PERINGATAN]: Menu tidak tersedia!");
            }
        }
    }

    private static void prosesCariGenre() {
        System.out.println("\nPilih Genre yang Tersedia:");
        for (int i = 0; i < CariBerdasarkanGenre.GENRE_LIST.length; i++) {
            System.out.println((i + 1) + ". " + CariBerdasarkanGenre.GENRE_LIST[i]);
        }
        System.out.print("Pilih angka genre: ");
        int indexGenre = membacaInputAngka() - 1;
        if (indexGenre >= 0 && indexGenre < CariBerdasarkanGenre.GENRE_LIST.length) {
            String genreDipilih = CariBerdasarkanGenre.GENRE_LIST[indexGenre];
            libraryProxy.cariBuku(new CariBerdasarkanGenre(), genreDipilih);
        } else {
            System.out.println("[ERROR]: Pilihan genre tidak valid.");
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
            System.out.print("Masukkan Nomor Rak: ");
            infoSpesifik = scanner.nextLine();
        } else if (tipe.equals("EBOOK")) {
            System.out.print("Masukkan Ukuran File: ");
            infoSpesifik = scanner.nextLine();
        }

        Buku bukuBaru = BukuFactory.buatBuku(tipe, id, judul, pengarang, genre, infoSpesifik);
        if (bukuBaru != null) {
            System.out.print("Apakah buku ini koleksi langka? (Y/N): ");
            String jawabLangka = scanner.nextLine();
            if (jawabLangka.equalsIgnoreCase("Y")) {
                bukuBaru = new com.perpustakaan.model.BukuLangkaDecorator(bukuBaru, 20000.0);
            }
            libraryProxy.daftarkanBukuBaru(bukuBaru);
        } else {
            System.out.println("[ERROR]: Gagal membuat buku.");
        }
    }

    private static int membacaInputAngka() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}