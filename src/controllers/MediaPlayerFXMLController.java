package controllers;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    private Button nextButton;
    
    @FXML
    private Button previousButton;
    
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
    private ImageView smallAlbumArt;
    @FXML
    private HBox smallAlbumArtBackground;
    
    @FXML
    private VBox screenBackground;

    @FXML
    private ListView playList;
    
    @FXML
    private ToggleButton nowPlayingButton;
    
    private ArrayList<File> fileList;           // List of the files' full paths 
    private ArrayList<String> fileNameList;     // List used to display names in ViewList
    
    private Map<File, String> filesPlayList;
    
    private int fileNumber = 0;
    
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

        smallAlbumArt.fitHeightProperty().bind(smallAlbumArtBackground.heightProperty());
        smallAlbumArt.fitWidthProperty().bind(smallAlbumArtBackground.widthProperty());
        
        // Play tracks in the playlist List
        playList.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2) {
                //Use ListView's getSelected Item
                fileNumber = playList.getSelectionModel().getSelectedIndex();

                updateNextPreviousButtonsState();
                initiateMediaPlayer(filesPlayList.keySet().toArray()[fileNumber].toString());
            }
        });
        
    }
    
    
    public void nextButtonClicked(ActionEvent e) {
        if (previousButton.isDisable()) {
            previousButton.setDisable(false);
        }
        
        // verify if its the last song
        if (fileNumber>=filesPlayList.size()-2) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
        if (fileNumber > filesPlayList.size() - 2) {
            return;
        }
        
        int oldValue = fileNumber;
        playList.requestFocus();
        fileNumber++;
        initiateMediaPlayer(filesPlayList.keySet().toArray()[fileNumber].toString());
        playList.getSelectionModel().select(fileNumber);
        playList.getFocusModel().focus(oldValue);
        
    }
    public void previousButtonClicked(ActionEvent e) {
        if (nextButton.isDisable()) {
            nextButton.setDisable(false);
        }
        
        // verify if its the first song
        if (fileNumber <= 1) {
            previousButton.setDisable(true);
        }
        else {
            previousButton.setDisable(false);
        }
        if (fileNumber <1) {
            return;
        }

        int oldValue = fileNumber;
        playList.requestFocus();
        fileNumber--;

        initiateMediaPlayer(filesPlayList.keySet().toArray()[fileNumber].toString());
        playList.getSelectionModel().select(fileNumber);
        playList.getFocusModel().focus(oldValue);
        
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
        
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        initiateMediaPlayer(path);
    }
    
    
    
    public void setRootFolderButtonClicked(ActionEvent e) {
        if (!playList.getItems().isEmpty()) {
            playList.getItems().clear();
        }

        filesPlayList = new LinkedHashMap<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(null);

        getAllFiles(rootDirectory);
 
        playList.getItems().addAll(filesPlayList.values());
        
        if (!playList.getItems().isEmpty()) {
            nextButton.setDisable(false);
        }
        else {
            nextButton.setDisable(true);
        }
    }

    
    private void visualProgression(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
    
    
    public void getAllFiles(File directory) {
        files = directory.listFiles();
        
        for (File file : files) { 
            
            // Recursive call to find files inside directories
            if (file.isDirectory()) {
                getAllFiles(file);
            } 
            // if is a media file, add to the full paths file list
            else if (file.isFile() && (isVideoFile(file.toString()) || isAudioFile(file.toString()))) {
                
                filesPlayList.put(file, file.getName());
            }
        }        
    }
    
    private void updateProgressBar() {
        progressBar.setDisable(false);
        progressSlider.setDisable(false);
        
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
     
    private void showMediaVideo() {
        smallAlbumArt.setImage(new Image("styles/img/Media-Player-icon.png"));
        mediaView.setVisible(true);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitHeightProperty().bind(screenBackground.widthProperty());
        mediaView.fitWidthProperty().bind(screenBackground.heightProperty());
    }
    
    private void initiateMediaPlayer(String path) {
        fileNameText.wrappingWidthProperty().bind(smallAlbumArtBackground.widthProperty());
        
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaView.setVisible(false);
            imageView.setVisible(false);
        }

        if (path != null) {
            media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            playButton.setDisable(false);
            playButton.setSelected(true);

            String fileName = (new File(path)).getName();
            
            if (isVideoFile(path)) {
                fileNameText.setText(fileName);
                setStageName(fileName);
                showMediaVideo();
            } 
            //audio file
            else {
                // set the file names
                setStageName(fileName);
                fileNameText.setText(fileName);
                
                imageView.setVisible(true);
                
                // if it has metadata it will overwrite the file names, if dont, it will stay the same
                media.getMetadata().addListener((MapChangeListener.Change<? extends String, ? extends Object> ch) -> {
                    if (ch.wasAdded()) {
                        handleMetadata(ch.getKey(), ch.getValueAdded());
                                                
                        if (currentArtist != null && currentTrack != null) {
                            setStageName(currentTrack + " - " + currentArtist);
                            fileNameText.setText(currentTrack);
                            
                            currentArtist = null;
                            currentTrack = null;
                        }
                    }
                });

            }
            
            updateProgressBar();
            mediaPlayer.play();
        }
    }
    
    private void setStageName(String track) {
        wmediaplayer.WMediaPlayer.getStage().setTitle(track + " - Media Player 11");
    }
    
    private void handleMetadata(String key, Object value) {
        
        switch (key) {
//            case "album":
//                //album.setText(value.toString());
//                break;
                
            case "artist":
                currentArtist = (value.toString());
                break;
                
            case "title":
                currentTrack = (value.toString());
                break;
                
        
//            case "year":
//                //year.setText(value.toString());
//                break;
                
            case "image":
                imageView.setImage((Image) value);
                smallAlbumArt.setImage((Image) value);
                
                imageView.fitHeightProperty().bind(screenBackground.heightProperty());
                imageView.fitWidthProperty().bind(screenBackground.widthProperty());
                
                break;
                
            default:
                break;
        }
    }
    
    private void updateNextPreviousButtonsState() {
        System.out.println(fileNumber);
        if (fileNumber >= filesPlayList.size() - 1 || filesPlayList.isEmpty()) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }

        if (fileNumber <= 0 || filesPlayList.isEmpty()) {
            previousButton.setDisable(true);
        } 
        else {
            previousButton.setDisable(false);
        }
    }
    
    
    private static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }
    private static boolean isAudioFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }
    

    public void nowPlayingButtonClicked(ActionEvent e) {
        if (!nowPlayingButton.isSelected()) {
            nowPlayingButton.setSelected(true);
        }
    }
    
}
