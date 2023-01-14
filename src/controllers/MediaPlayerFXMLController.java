package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;

/**
 * FXML Controller class
 *
 * @author ronny12301
 */
public class MediaPlayerFXMLController implements Initializable {

    @FXML
    Slider progressSlider;
    
    @FXML
    ProgressBar progressBar;
    
    @FXML
    Slider volumeSlider;

    @FXML
    ProgressBar volumeBar;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        visualProgression(progressSlider, progressBar);
        visualProgression(volumeSlider, volumeBar);
    }
    
    
    
    private void visualProgression(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
}
