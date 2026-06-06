package com.perpustakaan.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private static UserDatabase instance;
    // Map untuk menyimpan Username sebagai Key, dan [Password, Role] sebagai Value
    private Map<String, String[]> tabelUser;
    private static final String FILE_USER = "users.csv";

    private UserDatabase() {
        tabelUser = new HashMap<>();
        muatDataUser();
    }

    public static UserDatabase getInstance() {
        if (instance == null) {
            instance = new UserDatabase();
        }
        return instance;
    }

    // Mengambil alih proses membaca data akun dari users.csv
    private void muatDataUser() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_USER))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 3) continue;
                
                String username = data[0].trim();
                String password = data[1].trim();
                String role = data[2].trim().toUpperCase();
                
                tabelUser.put(username, new String[]{password, role});
            }
        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal memuat berkas akun data user.");
        }
    }

    // Fungsi inti untuk memvalidasi login
    public String[] validasiLogin(String username, String password) {
        if (tabelUser.containsKey(username)) {
            String[] kredensial = tabelUser.get(username);
            if (kredensial[0].equals(password)) {
                return kredensial; // Mengembalikan [Password, Role] jika sukses
            }
        }
        return null; // Mengembalikan null jika username atau password salah
    }

    // Fungsi baru untuk mendaftarkan anggota baru ke file users.csv
    public boolean registrasiUserBaru(String username, String password) {
        String userClean = username.trim();
        String passClean = password.trim();

        // 1. Validasi agar tidak ada username yang kembar
        if (tabelUser.containsKey(userClean)) {
            System.out.println("[PERINGATAN]: Username \"" + userClean + "\" sudah terdaftar!");
            return false;
        }

        // 2. Simpan ke dalam memory RAM (tabelUser)
        String[] dataBaru = {passClean, "ANGGOTA"}; // Default role saat daftar adalah ANGGOTA
        tabelUser.put(userClean, dataBaru);

        // 3. Tulis permanen (Append) ke dalam file users.csv
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter("users.csv", true))) {
            out.printf("%s,%s,%s\n", userClean, passClean, "ANGGOTA");
            System.out.println("[SISTEM]: Akun berhasil dibuat dan disimpan ke CSV.");
            return true;
        } catch (java.io.IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal menyimpan akun baru ke berkas CSV.");
            return false;
        }
    }
}