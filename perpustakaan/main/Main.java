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
    
    // Proxy tetap dideklarasikan
    private static LibraryServiceProxy libraryProxy;
    private static HistoryServiceProxy historyProxy;

    public static void main(String[] args) {
        LibraryServiceImpl realLibService = new LibraryServiceImpl();
        libraryProxy = new LibraryServiceProxy(realLibService, currentRole);
        
        HistoryServiceImpl realHistService = new HistoryServiceImpl();
        historyProxy = new HistoryServiceProxy(realHistService, currentRole);

        LibraryDatabase db = LibraryDatabase.getInstance();

        System.out.println("Memuat sistem perpustakaan...");

        boolean berjalan = true;
        while (berjalan) {
            System.out.println("\n==================================================");
            System.out.println("  PORTAL LOGIN PERPUSTAKAAN (AUTENTIKASI)");
            System.out.println("==================================================");
            System.out.println("1. Login");
            System.out.println("2. Buat Akun Baru (Register)");
            System.out.println("3. Keluar Aplikasi");
            System.out.print("Pilihan Anda (1-3): ");
            
            int pilihan = membacaInputAngka();
            switch (pilihan) {
                case 1:
                    System.out.println("\n--- MENU LOGIN ---");
                    System.out.print("Masukkan Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Masukkan Password: ");
                    String password = scanner.nextLine();

                    // Mengirim input ke LibraryDatabase untuk divalidasi
                    String role = db.loginUser(username, password);

                    if (role == null) {
                        System.out.println("[ERROR]: Username tidak ditemukan atau Password salah!");
                    } else if (role.equals("ADMIN")) {
                        namaUserAktif = username;
                        currentRole = "PUSTAKAWAN";
                        libraryProxy.setUserRoleSaatIni(currentRole);
                        historyProxy.setUserRoleSaatIni(currentRole);
                        System.out.println("\n[SISTEM]: Login Berhasil sebagai PUSTAKAWAN: " + username.toUpperCase());
                        tampilkanMenuPustakawan(); 
                    } else if (role.equals("ANGGOTA")) {
                        namaUserAktif = username;
                        currentRole = "ANGGOTA";
                        libraryProxy.setUserRoleSaatIni(currentRole);
                        historyProxy.setUserRoleSaatIni(currentRole);
                        System.out.println("\n[SISTEM]: Login Berhasil sebagai Anggota: " + username.toUpperCase());
                        tampilkanMenuAnggota(); 
                    }
                    break;

                case 2:
                    System.out.println("\n--- PENDAFTARAN ANGGOTA BARU ---");
                    System.out.print("Masukkan Username Baru: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("Masukkan Password Baru: ");
                    String newPassword = scanner.nextLine();

                    // Proses registrasi via LibraryDatabase
                    boolean sukses = db.registerUser(newUsername, newPassword, "ANGGOTA");
                    
                    if (sukses) {
                        System.out.println("[SISTEM]: Registrasi berhasil! Silakan Login menggunakan akun baru Anda.");
                    } else {
                        System.out.println("[ERROR]: Username sudah dipakai, silakan cari username lain.");
                    }
                    break;

                case 3:
                    berjalan = false;
                    System.out.println("\n[SISTEM]: Menutup sistem... Terima kasih!");
                    break;

                default:
                    System.out.println("[PERINGATAN]: Pilihan tidak valid!");
            }
        }
    }

    // =========================================================================
    // FUNGSI MENU DAN INPUT (Tidak banyak berubah dari aslinya, hanya penyesuaian)
    // =========================================================================

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
                        LibraryDatabase.getInstance().simpanKeFile(); // Paksa update CSV saat buku kembali
                        System.out.println("[SISTEM]: Buku berhasil dikembalikan!");
                    } else {
                        System.out.println("[ERROR]: Gagal mengembalikan buku.");
                    }
                    break;
                case 6:
                    historyProxy.lihatBukuDipinjamAnggota(namaUserAktif); 
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
                case 3: historyProxy.lihatSemuaRiwayatTransaksi(); break; 
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
        for (int i = 0; i < CariBerdasarkanGenre.GENRE_LIST.size(); i++) {
            System.out.println((i + 1) + ". " + CariBerdasarkanGenre.GENRE_LIST.get(i));
        }
        System.out.print("Pilih angka genre: ");
        int indexGenre = membacaInputAngka() - 1;
        if (indexGenre >= 0 && indexGenre < CariBerdasarkanGenre.GENRE_LIST.size()) {
            String genreDipilih = CariBerdasarkanGenre.GENRE_LIST.get(indexGenre);
            libraryProxy.cariBuku(new CariBerdasarkanGenre(), genreDipilih);
        } else {
            System.out.println("[ERROR]: Pilihan genre tidak valid.");
        }
    }

    private static void prosesInputBukuBaru() {
        System.out.println("\n--- FORM TAMBAH BUKU BARU ---");
        System.out.print("Masukkan Tipe Buku (FISIK / EBOOK): ");
        String tipe = scanner.nextLine().toUpperCase();
        
        // Peringatan: Pastikan method generateNextBookId() ada di LibraryDatabase milikmu!
        // Jika belum ada, ganti id ini dengan manual input seperti: String id = scanner.nextLine();
        String id = LibraryDatabase.getInstance().generateNextBookId();
        System.out.println("ID Buku Otomatis Dibuat: " + id);

        System.out.print("Masukkan Judul Buku: ");
        String judul = scanner.nextLine();
        System.out.print("Masukkan Pengarang: ");
        String pengarang = scanner.nextLine();
        
        System.out.println("Pilih Genre:");
        for (int i = 0; i < CariBerdasarkanGenre.GENRE_LIST.size(); i++) {
            System.out.println((i + 1) + ". " + CariBerdasarkanGenre.GENRE_LIST.get(i));
        }
        System.out.println((CariBerdasarkanGenre.GENRE_LIST.size() + 1) + ". [Tambah / Ketik Genre Baru]");
        System.out.print("Pilihan angka genre: ");
        int indexGenre = membacaInputAngka() - 1;
        
        String genre = "";
        if (indexGenre == CariBerdasarkanGenre.GENRE_LIST.size()) {
            System.out.print("Masukkan nama Genre Baru yang ingin ditambahkan: ");
            String genreBaruInput = scanner.nextLine();
            if (!genreBaruInput.trim().isEmpty()) {
                LibraryDatabase.getInstance().simpanGenreBaruKeCSV(genreBaruInput); // Pastikan method ini ada di LibraryDatabase!
                genre = genreBaruInput.trim();
            } else {
                genre = "Umum";
            }
        } else if (indexGenre >= 0 && indexGenre < CariBerdasarkanGenre.GENRE_LIST.size()) {
            genre = CariBerdasarkanGenre.GENRE_LIST.get(indexGenre);
        } else {
            genre = "Umum";
        }

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