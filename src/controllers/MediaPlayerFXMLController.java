package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author ronny12301
 */
public class MediaPlayerFXMLController implements Initializable {

    @FXML
    private Slider progressSlider;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Slider volumeSlider;

    @FXML
    private ProgressBar volumeBar;
    
    @FXML
    private MediaView mediaView;
    
    @FXML
    private VBox screenBackground;

    private String path;
    private MediaPlayer mediaPlayer;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        visualProgression(progressSlider, progressBar);
        visualProgression(volumeSlider, volumeBar);
    }
    
    
    public void chooseFileClicked(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        path = file.toURI().toString();
        
        if (path != null) {
            Media media = new Media(path);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            mediaView.fitHeightProperty().bind(screenBackground.widthProperty());
            mediaView.fitWidthProperty().bind(screenBackground.heightProperty());
      
            mediaPlayer.play();
        }
    }
    
    
    
    
    
    
    
    
    private void visualProgression(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
}
