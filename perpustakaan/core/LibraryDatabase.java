package com.perpustakaan.core;

import com.perpustakaan.model.Buku;
import com.perpustakaan.model.BukuFisik;
import com.perpustakaan.model.EBook;
import com.perpustakaan.model.BukuLangkaDecorator;
import com.perpustakaan.model.Transaksi;
import com.perpustakaan.state.DipinjamState;
import com.perpustakaan.strategy.CariBerdasarkanGenre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryDatabase {
    private static LibraryDatabase instance;
    private Map<String, Buku> tabelBuku;
    private List<Transaksi> daftarTransaksi; 

    private int counterBuku = 1;

    // Lokasi berkas database CSV (Urusan USER sudah dihapus dari sini)
    private static final String FILE_FISIK = "buku_fisik.csv";
    private static final String FILE_EBOOK = "ebook.csv";
    private static final String FILE_TRANSAKSI = "transaksi.csv";
    private static final String FILE_GENRE = "genre.csv";

    private LibraryDatabase() {
        tabelBuku = new HashMap<>();
        daftarTransaksi = new ArrayList<>(); 
        
        // Membaca semua baris CSV perpustakaan saat aplikasi menyala
        muatDariFile();
    }

    public static LibraryDatabase getInstance() {
        if (instance == null) {
            instance = new LibraryDatabase();
        }
        return instance;
    }

    // Fungsi profesional untuk mencari ID terkecil yang kosong/bolong di tengah
    public String dapatkanIdBaruOtomatis() {
        int checkId = 1;
        while (true) {
            // Format angka menjadi format 2 digit, misal: B01, B02
            String kandidatId = "B" + String.format("%02d", checkId);
            
            // Jika ID ini BELUM dipake (berarti bolong atau ini batas akhir), pakai ID ini!
            if (!tabelBuku.containsKey(kandidatId)) {
                return kandidatId;
            }
            checkId++;
        }
    }

    private void updateCounterBuku(String id) {
        try {
            if (id.startsWith("B")) {
                int nomorId = Integer.parseInt(id.substring(1).trim());
                if (nomorId >= counterBuku) {
                    counterBuku = nomorId + 1;
                }
            }
        } catch (NumberFormatException e) {
            // Abaikan jika format ID tidak standar
        }
    }

    public void simpanGenreBaruKeCSV(String genreBaru) {
        String genreClean = genreBaru.trim();
        for (String g : CariBerdasarkanGenre.GENRE_LIST) {
            if (g.equalsIgnoreCase(genreClean)) {
                System.out.println("[PERINGATAN]: Genre \"" + genreClean + "\" sudah ada di dalam sistem.");
                return;
            }
        }
        
        CariBerdasarkanGenre.tambahGenreBaruKeList(genreClean);
        
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_GENRE, true))) {
            out.println(genreClean);
            System.out.println("[SISTEM]: Berhasil menambahkan genre baru ke CSV: " + genreClean);
        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal menyimpan genre baru ke berkas CSV.");
        }
    }

    public void tambahBuku(Buku buku) {
        tabelBuku.put(buku.getId(), buku);
        
        boolean isLangka = (buku instanceof BukuLangkaDecorator);
        Buku objekAsli = buku;
        if (isLangka) {
            objekAsli = membongkarDecorator(buku);
        }

        if (objekAsli instanceof BukuFisik) {
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_FISIK, true))) {
                String info = objekAsli.tampilkanInfo();
                String rak = "Rak-Umum";
                if (info.contains("Rak: ")) {
                    rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                }
                out.printf("%s,%s,%s,%s,%s,%s,%b\n", 
                    buku.getId(), buku.getJudul(), buku.getPengarang(), 
                    buku.getGenre(), rak, buku.getState().getStatusName(), isLangka);
            } catch (IOException e) {
                System.out.println("[ERROR DATABASE]: Gagal melakukan append data buku fisik.");
            }
            
        } else if (objekAsli instanceof EBook) {
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_EBOOK, true))) {
                String info = objekAsli.tampilkanInfo();
                String ukuran = "Unknown-Size";
                if (info.contains("Digital: ")) {
                    ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                }
                out.printf("%s,%s,%s,%s,%s,%s,%b\n", 
                    buku.getId(), buku.getJudul(), buku.getPengarang(), 
                    buku.getGenre(), ukuran, buku.getState().getStatusName(), isLangka);
            } catch (IOException e) {
                System.out.println("[ERROR DATABASE]: Gagal melakukan append data ebook.");
            }
        }
    }

    public Buku ambilBukuBerdasarkanId(String id) {
        return tabelBuku.get(id);
    }

    public List<Buku> ambilSemuaBuku() {
        return new ArrayList<>(tabelBuku.values());
    }

    public void tambahTransaksi(Transaksi trx) {
        daftarTransaksi.add(trx);
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_TRANSAKSI, true))) {
            out.printf("%s,%s,%s,%s,%s\n", 
                trx.getIdTransaksi(), trx.getNamaAnggota(), 
                trx.getIdBuku(), trx.getJudulBuku(), trx.getStatusTransaksi());
        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal melakukan append data transaksi.");
        }
    }

    public List<Transaksi> ambilSemuaTransaksi() {
        return daftarTransaksi;
    }

    public void simpanKeFile() {
        try (PrintWriter outFisik = new PrintWriter(new FileWriter(FILE_FISIK));
             PrintWriter outEbook = new PrintWriter(new FileWriter(FILE_EBOOK));
             PrintWriter outTrx = new PrintWriter(new FileWriter(FILE_TRANSAKSI))) {
            
            for (Buku buku : tabelBuku.values()) {
                String id = buku.getId();
                String judul = buku.getJudul();
                String pengarang = buku.getPengarang();
                String genre = buku.getGenre();
                String status = buku.getState().getStatusName();
                
                boolean isLangka = (buku instanceof BukuLangkaDecorator);
                Buku objekAsli = buku;
                if (isLangka) {
                    objekAsli = membongkarDecorator(buku);
                }

                if (objekAsli instanceof BukuFisik) {
                    String info = objekAsli.tampilkanInfo();
                    String rak = "Rak-Umum";
                    if (info.contains("Rak: ")) {
                        rak = info.substring(info.indexOf("Rak: ") + 5, info.indexOf(" ->"));
                    }
                    outFisik.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, rak, status, isLangka);
                } else if (objekAsli instanceof EBook) {
                    String info = objekAsli.tampilkanInfo();
                    String ukuran = "Unknown-Size";
                    if (info.contains("Digital: ")) {
                        ukuran = info.substring(info.indexOf("Digital: ") + 9, info.indexOf(" ->"));
                    }
                    outEbook.printf("%s,%s,%s,%s,%s,%s,%b\n", id, judul, pengarang, genre, ukuran, status, isLangka);
                }
            }

            for (Transaksi t : daftarTransaksi) {
                outTrx.printf("%s,%s,%s,%s,%s\n", t.getIdTransaksi(), t.getNamaAnggota(), t.getIdBuku(), t.getJudulBuku(), t.getStatusTransaksi());
            }

        } catch (IOException e) {
            System.out.println("[ERROR DATABASE]: Gagal memperbarui status data pada berkas CSV.");
        }
    }

    private Buku membongkarDecorator(Buku bukuDekor) {
        try {
            java.lang.reflect.Field field = com.perpustakaan.model.BukuDecorator.class.getDeclaredField("bukuYangDekor");
            field.setAccessible(true);
            return (Buku) field.get(bukuDekor);
        } catch (Exception e) {
            return bukuDekor; 
        }
    }

    private void muatDariFile() {
        muatGenre(); 
        muatBukuFisik();
        muatEbook();
        muatTransaksi();
    }

    private void muatGenre() {
        File file = new File(FILE_GENRE);
        if (!file.exists()) {
            try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                out.println("Teknologi");
                out.println("Fiksi");
                out.println("Sains");
                out.println("Sejarah");
            } catch (IOException e) {
                System.out.println("[ERROR]: Gagal menginisialisasi berkas genre.");
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_GENRE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                CariBerdasarkanGenre.tambahGenreBaruKeList(line.trim());
            }
        } catch (IOException e) { }
    }

    private void muatBukuFisik() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_FISIK))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 6) continue;
                
                updateCounterBuku(data[0]);

                Buku buku = BukuFactory.buatBuku("FISIK", data[0], data[1], data[2], data[3], data[4]);
                
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                if (data.length >= 7 && Boolean.parseBoolean(data[6].trim())) {
                    buku = new com.perpustakaan.model.BukuLangkaDecorator(buku, 20000.0);
                }
                
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { }
    }

    private void muatEbook() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_EBOOK))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 6) continue;
                
                updateCounterBuku(data[0]);

                Buku buku = BukuFactory.buatBuku("EBOOK", data[0], data[1], data[2], data[3], data[4]);
                
                if (data[5].trim().equalsIgnoreCase("Dipinjam")) {
                    buku.setState(new DipinjamState());
                }
                
                if (data.length >= 7 && Boolean.parseBoolean(data[6].trim())) {
                    buku = new com.perpustakaan.model.BukuLangkaDecorator(buku, 20000.0);
                }
                
                tabelBuku.put(buku.getId(), buku);
            }
        } catch (IOException e) { }
    }

    private void muatTransaksi() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_TRANSAKSI))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length < 5) continue;
                
                Transaksi trx = new Transaksi(data[0], data[1], data[2], data[3]);
                trx.setStatusTransaksi(data[4].trim());
                daftarTransaksi.add(trx);

                // Pastikan status buku konsisten dengan transaksi aktif.
                if (trx.getStatusTransaksi().equalsIgnoreCase("DIPINJAM")) {
                    Buku buku = tabelBuku.get(trx.getIdBuku());
                    if (buku != null && !buku.getState().getStatusName().equalsIgnoreCase("Dipinjam")) {
                        buku.setState(new DipinjamState());
                    }
                }
            }
        } catch (IOException e) { }
    }

    public boolean hapusBukuBerdasarkanId(String id) {
    if (tabelBuku.containsKey(id)) {
        tabelBuku.remove(id);
        simpanKeFile(); // Menulis ulang CSV tanpa buku yang dihapus
        return true;
    }
    return false;
}
}