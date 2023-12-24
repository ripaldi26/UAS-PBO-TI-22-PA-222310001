
package Application;

import javafx.application.Platform;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Audio {
    private AudioFormat audioFormat;  //untuk menyimpan format audio
    private TargetDataLine targetDataLine; //untuk merepresentasikan jalur data yang ditargetkan pada perangkat audio.
    private File audioFile; //untuk merepresentasikan file audio yang akan direkam.
    private boolean isRecordingPaused = false;
    private ByteArrayOutputStream audioOutputStream; //untuk menyimpan data audio yang direkam.
    private byte[] buffer = new byte[1024]; //Mendeklarasikan array byte buffer dengan ukuran 1024 untuk menyimpan data audio sementara.
    private BlockingQueue<byte[]> audioDataQueue; //Mendeklarasikan objek BlockingQueue yang berisi array byte untuk mengimplementasikan antrian data audio.


    public boolean isRecordingPaused() { //method untuk mendapatkan status rekaman
        return isRecordingPaused;
    }

    public Audio() throws LineUnavailableException, IOException { //Konstruktor kelas Audio yang melemparkan pengecualian LineUnavailableException dan IOException

        audioFormat = getAudioFormat(); 
        audioOutputStream = new ByteArrayOutputStream();
        audioDataQueue = new LinkedBlockingQueue<>();
    }

    public void startRecording(Graphics graphics) throws LineUnavailableException, IOException {//Metode untuk memulai proses rekaman dengan menerima objek Graphics sebagai parameter.
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat); // untuk mendapatkan informasi jalur data yang diperlukan.


        if (!AudioSystem.isLineSupported(dataLineInfo)) { // Memeriksa apakah jalur data didukung oleh sistem.


            System.out.println("Line not supported");
            return;
        }

        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo); // Mendapatkan jalur data sesuai dengan informasi yang dibutuhkan.
        targetDataLine.open(audioFormat);
        targetDataLine.start();  //Membuka dan memulai jalur data untuk proses rekaman.

        audioFile = new File("output.wav"); //Menyimpan file audio dengan nama "output.wav".

        Thread captureThread = new Thread(() -> { 
            try {
                while (true) {
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        if (!isRecordingPaused) {
                            audioOutputStream.write(buffer, 0, bytesRead);
                            audioDataQueue.put(buffer.clone());
                            Platform.runLater(() -> graphics.plotWaveform(audioDataQueue, audioFile, isRecordingPaused));
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }); //Membuat thread baru untuk menangani proses pembacaan data dari jalur data.


        captureThread.start(); //memulai thread 
    }

    public void stopRecording() throws IOException { //Metode untuk menghentikan proses rekaman dan menyimpan hasil rekaman ke dalam file.
        if (targetDataLine != null) { 
            targetDataLine.stop();
            targetDataLine.close();
        } //Menghentikan dan menutup jalur data jika sudah terbuka.



        try (AudioInputStream inputStream = new AudioInputStream( //Membuat objek AudioInputStream dari data audio yang direkam.
                new ByteArrayInputStream(audioOutputStream.toByteArray()), audioFormat,
                audioOutputStream.size() / audioFormat.getFrameSize())) {
            AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, audioFile);
        } // Menyimpan data audio ke dalam file dengan format WAV.



        audioOutputStream.close(); //Menutup output stream.

    }

    public void togglePauseResume() { //Metode untuk mengganti status jeda atau tidak jeda pada proses rekaman.

        if (targetDataLine != null) {
            isRecordingPaused = !isRecordingPaused; 
            if (isRecordingPaused) {
                targetDataLine.stop();
            } else {
                targetDataLine.start();
            }
        } //Memeriksa dan mengganti status jeda atau tidak jeda, serta menghentikan atau memulai kembali jalur data sesuai dengan status.

    }


    public void playRecording() { //Metode untuk memutar rekaman audio yang telah direkam.

        if (audioFile != null && audioFile.exists()) { //mengecek apakah ada rekamannya
            try {
                Clip clip = AudioSystem.getClip(); 
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                clip.open(audioInputStream);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No recording to play.");
        }
    }

    public void reset() { //Metode untuk mengatur ulang objek audioOutputStream dan audioDataQueue agar siap untuk proses rekaman baru.

        audioOutputStream = new ByteArrayOutputStream();
        audioDataQueue = new LinkedBlockingQueue<>();
    }

    private AudioFormat getAudioFormat() { //Metode untuk mendapatkan format audio 
        float sampleRate = 44100.0F;  // Laju sampel audio, dalam hertz (Hz)
        int sampleSizeInBits = 16; // Ukuran setiap sampel audio, dalam bit
        int channels = 1; // Jumlah saluran audio (mono)
        boolean bigEndian = false; // Urutan byte audio (little-endian)

     // Membuat dan mengembalikan objek AudioFormat dengan parameter yang telah ditentukan
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, channels, (sampleSizeInBits / 8) * channels, sampleRate, bigEndian);
    }
}
