
package Application;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Graphics {
    private static Canvas canvas; //canvas bertipe Canvas yang akan digunakan untuk menggambar.
    private static GraphicsContext gc; //Mendeklarasikan variabel statis gc bertipe GraphicsContext yang menyediakan metode untuk menggambar pada Canvas.
    private boolean wasPreviouslyPaused = false; //Mendeklarasikan variabel boolean wasPreviouslyPaused yang digunakan untuk melacak apakah proses rekaman sebelumnya dijeda atau tidak.

    public Graphics(Canvas canvas) { //Konstruktor kelas Graphics yang menerima objek Canvas sebagai parameter.

        Graphics.canvas = canvas;
        Graphics.gc = canvas.getGraphicsContext2D();
    } // Menyimpan objek Canvas dan mendapatkan objek GraphicsContext dari Canvas.




    public void clear() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    } //Membersihkan seluruh area Canvas.



    public void plotWaveform(BlockingQueue<byte[]> audioDataQueue, File audioFile, boolean isRecordingPaused) { // Metode untuk menggambar grafik gelombang audio pada Canvas.

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile); //Membuka AudioInputStream dari file audio.

            byte[] audioData = new byte[(int) audioFile.length()];
            audioInputStream.read(audioData); //Membaca data audio dari AudioInputStream ke dalam array audioData.


           double scaleFactor = 100.0; 
           double centerY = canvas.getHeight() / 2.0; //Menentukan faktor skala dan nilai tengah vertikal dari Canvas.

            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Menghapus gambar sebelumnya pada Canvas.
            gc.beginPath();
            gc.moveTo(0, centerY);

            for (int i = 0; i < audioData.length; i += 2) { 
                double scaledValue = (audioData[i] + audioData[i + 1] * 256) / scaleFactor; //Menghitung nilai yang akan digambar dengan menggunakan dua byte audio.
                double x = i / (double) audioData.length * canvas.getWidth(); 
                double y = centerY + scaledValue; //Menghitung koordinat x dan y untuk menggambar.

                gc.lineTo(x, y); //Menghubungkan garis ke titik (x, y).

            }

            gc.stroke();
            gc.closePath();	// menutup path.

           
            
            if (!isRecordingPaused && wasPreviouslyPaused) {
                Platform.runLater(() -> plotWaveform(audioDataQueue, audioFile, isRecordingPaused)); 
            }// Menjadwalkan penggambaran ulang menggunakan Platform.runLater untuk menghindari masalah thread.

            wasPreviouslyPaused = isRecordingPaused; //Memperbarui status jeda sebelumnya.


        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
