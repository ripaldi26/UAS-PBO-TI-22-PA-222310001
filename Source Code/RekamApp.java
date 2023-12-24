
package Application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.Optional;

public class RekamApp extends Application {
    private Audio audio;
    private Graphics graphics;
    private Stage primaryStage;
    private Button pauseResumeButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) { //Metode ini dijalankan oleh JavaFX untuk memulai eksekusi aplikasi
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Recorder");

        try {
            audio = new Audio();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }

        showStartRecordingPage();
    }

    private void showStartRecordingPage() { //Metode ini membuat tampilan awal aplikasi dengan tombol "Start Recording". Saat tombol ditekan, metode startRecording() dipanggil.
        Canvas canvas = new Canvas(400, 200);
        graphics = new Graphics(canvas);
        			
        Button startButton = new Button("Start Recording");
        startButton.setOnAction(e -> startRecording());

        VBox vbox = new VBox(5);
        vbox.getChildren().addAll(canvas, startButton);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox, 420, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPauseResumeStopPage() { //Metode ini menampilkan tampilan yang berisi tombol "Pause" dan "Stop" selama proses perekaman berlangsung. Jika tombol "Pause" ditekan, togglePauseResume() dipanggil untuk mengonfigurasi status perekaman.

        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setOnAction(e -> togglePauseResume());

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> stopRecording());

        Pane root = new Pane();
        pauseResumeButton.setLayoutX(160);
        pauseResumeButton.setLayoutY(240);
        root.getChildren().add(pauseResumeButton);

        stopButton.setLayoutX(230);
        stopButton.setLayoutY(240);
        root.getChildren().add(stopButton);

        Scene scene = new Scene(root, 420, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    
    private void recordAgain() { //Metode ini menampilkan dialog konfirmasi dan, jika pengguna menekan "Yes", panggilan ke resetAndStartOver() dilakukan.

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure you want to record again?");

      
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);


        Optional<ButtonType> result = alert.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.YES) {
            resetAndStartOver();
        }
    }


    private void showGraphPage() { //Metode ini menampilkan tampilan yang berisi grafik dari rekaman audio yang direkam sebelumnya, bersama dengan tombol "Record Again".

        Canvas canvas = new Canvas(400, 200);
        graphics = new Graphics(canvas);

        Button recordAgainButton = new Button("Record Again");
        recordAgainButton.setLayoutX(160);
        recordAgainButton.setLayoutY(240);
        recordAgainButton.setOnAction(e -> recordAgain());

        Pane root = new Pane();
        root.getChildren().addAll(canvas, recordAgainButton);

        Scene scene = new Scene(root, 420, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private void resetAndStartOver() { //Metode ini mengatur ulang status dan tampilan aplikasi untuk memulai proses perekaman kembali.

    	 audio.reset();
         graphics.clear();
         showStartRecordingPage();
    }

    private void startRecording() { //Memulai perekaman audio dan menggunakan objek Graphics untuk menampilkan visualisasi gelombang suara.

        try {
            audio.startRecording(graphics);
            System.out.println("Recording started...");
            showPauseResumeStopPage();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() { //Menghentikan perekaman audio, menyimpan rekaman ke file "output.wav", dan menampilkan grafik.

        try {
            audio.stopRecording();
            System.out.println("Recording stopped...");
            showGraphPage();
            audio.playRecording();  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePauseResume() { //Mengganti status perekaman antara "paused" dan "resumed".

        Platform.runLater(() -> {
            if (audio.isRecordingPaused()) {
               
                pauseResumeButton.setText("Resume");
            } else {
               
                pauseResumeButton.setText("Pause");
            }
        });

        audio.togglePauseResume();
    }
    
  
}
