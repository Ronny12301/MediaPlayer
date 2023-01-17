package controllers;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

import java.io.File;

import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;


/**
 * FXML Controller class
 *
 * @author ronny12301
 */
public class MediaPlayerFXMLController implements Initializable {

    @FXML
    private Text fileNameText;
    
    @FXML
    private ToggleButton playButton;
    
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
    private ImageView imageView;
    
    @FXML
    private VBox screenBackground;

    @FXML
    public ListView playList;
    
    private ArrayList<File> fileList;           // List of the files' full paths 
    private ArrayList<String> fileNameList;     // List used to display names in ViewList
    private int fileNumber;
    
    private File rootDirectory;
    private File[] files;
    
    private String path;
    private Media media;
    private MediaPlayer mediaPlayer;
    
    private String currentArtist;
    private String currentTrack;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        visualProgression(progressSlider, progressBar);
        visualProgression(volumeSlider, volumeBar); 
           
        // Play tracks in the playlist List
        playList.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2) {
                //Use ListView's getSelected Item
                fileNumber = playList.getSelectionModel().getSelectedIndex();
                
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaView.setVisible(false);
                }
                media = new Media(fileList.get(fileNumber).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                playButton.setDisable(false);
                playButton.setSelected(true);
                
                updateProgressBar();
                mediaPlayer.play();
            }
        });
        
    }
    
    
    public void playButtonClicked(ActionEvent e) {
        
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
     
    }
    
    public void chooseFileClicked(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        path = file.toString();
                
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        
        if (path != null) {
            media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            playButton.setDisable(false);
            playButton.setSelected(true);
            
            if (isVideoFile(path)) {
                showMediaVideo();
            }
            else { 
                media.getMetadata().addListener(new MapChangeListener<String, Object>() {
                @Override
                public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> ch) {
                    if (ch.wasAdded()) {
                        getMetadata(ch.getKey(), ch.getValueAdded());
                        fileNameText.setText(currentTrack);
                        setStageName(currentTrack + " - " + currentArtist + " - Media Player 11");
                    }
                }
            });
                
            }
            
            
            
            updateProgressBar();
            mediaPlayer.play();
        }
        
    }
    
    
    
    public void setRootFolderButtonClicked(ActionEvent e) {
        fileList = new ArrayList<>();
        fileNameList = new ArrayList<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(null);

        getAllFiles(rootDirectory);
        
        playList.getItems().addAll(fileNameList);
        
    }

    
    private void visualProgression(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
    
    
    public void getAllFiles(File directory) {
        files = directory.listFiles();
        
        for (File file : files) {            
            if (file.isDirectory()) {
                getAllFiles(file);
            } 
            // if is video just get file name
            else if (file.isFile() && isVideoFile(file.toString())) {
                fileList.add(file);
                fileNameList.add(file.getName());
            }
            // if is music get metadata
            else if (file.isFile() && isAudioFile(file.toString())) {
                //getMetadata(new Media(file.toURI().toString()));
            }
        }        
    }
    
    private void updateProgressBar() {
            mediaPlayer.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
                progressSlider.setValue(newValue.toSeconds());
            });
            
            progressSlider.setOnMousePressed((MouseEvent t) -> {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            });
            
            progressSlider.setOnMouseDragged((MouseEvent t) -> {
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            });
    }
    
    private static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }
    private static boolean isAudioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }
    
    private void showMediaVideo() {
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitHeightProperty().bind(screenBackground.widthProperty());
        mediaView.fitWidthProperty().bind(screenBackground.heightProperty());
        mediaView.setVisible(true);
    }
    
    
    private void setStageName(String track) {
        wmediaplayer.WMediaPlayer.getStage().setTitle(track + " - Media Player 11");
    }
    
    private void getMetadata(String key, Object value) {
        
        if (key.equals("album")) {
            //album.setText(value.toString());
        } 
        if (key.equals("artist")) {
            currentArtist = (value.toString());
            System.out.println(currentArtist);
        }
        if (key.equals("title")) {
            currentTrack = (value.toString());
            System.out.println(currentTrack);
        }
        if (key.equals("year")) {
            //year.setText(value.toString());
        }
        if (key.equals("image")) {
            imageView.setImage((Image) value);
        }

        
    }
    
}
