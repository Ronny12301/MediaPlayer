package controllers;

import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;

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
import javafx.scene.input.KeyCode;
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
    private ToggleButton volumeButton;
    
    @FXML
    private Text timeText;
    @FXML
    private Text totalTimeText;
    
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
        
        // Play tracks in the playlist List using the mouse
        playList.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2 && !playList.getItems().isEmpty()) {
                //Use ListView's getSelected Item
                fileNumber = playList.getSelectionModel().getSelectedIndex();

                updateNextPreviousButtonsState();
                initiateMediaPlayer(filesPlayList.keySet().toArray()[fileNumber].toString());
            }
        });
        // same but with Enter key
        playList.setOnKeyPressed((key) -> {
            if (key.getCode() == KeyCode.ENTER && !playList.getItems().isEmpty()) {
                fileNumber = playList.getSelectionModel().getSelectedIndex();

                updateNextPreviousButtonsState();
                initiateMediaPlayer(filesPlayList.keySet().toArray()[fileNumber].toString());

            }
        });  
    }
    
    
    public void nextButtonClicked(ActionEvent e) {
        
        playNextMedia();
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
    
    public void stopButtonClicked(ActionEvent e) {
        
        timeText.setText("00:00");
        totalTimeText.setText("- -:- -");
        
        if (mediaPlayer == null) {
            return;
        }
        
        
        mediaPlayer.stop();
        playButton.setSelected(false);
        
        progressBar.setDisable(true);
        progressSlider.setDisable(true);
        
    }
    
    public void playButtonClicked(ActionEvent e) {
        if (progressBar.isDisabled() || progressSlider.isDisabled()) {
            progressBar.setDisable(false);
            progressSlider.setDisable(false);
        }
        
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }
    
    public void chooseFileClicked(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        
        if (file == null) {
            return;
        }
        
        path = file.toString();
        
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        
        initiateMediaPlayer(path);
    }
    
    
    
    @SuppressWarnings("unchecked")
    public void setRootFolderButtonClicked(ActionEvent e) {
        Map<File,String> oldPlayList = new TreeMap<>();
        
        // if the playList ist empty save a copy if the user cancels the change
        if (!playList.getItems().isEmpty()) {
            oldPlayList = filesPlayList;
            playList.getItems().clear();
        }

        filesPlayList = new TreeMap<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        rootDirectory = directoryChooser.showDialog(null);

        if (rootDirectory == null) {
            filesPlayList = oldPlayList;
            playList.getItems().addAll(filesPlayList);
            return;
        }
        
        getAllFiles(rootDirectory);
 
        playList.getItems().addAll(filesPlayList.values());
        
        if (playList.getItems().isEmpty()) {
            nextButton.setDisable(true);
        }
    }

    public void volumeButtonClicked(ActionEvent e) {
        if (volumeButton.isSelected()) {
            mediaPlayer.volumeProperty().set(0);
        }
        else if (volumeSlider.getValue() == 0) {
            volumeSlider.setValue(100);
        }
        else {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
    }
    
    
    private void visualProgression(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
    
    public void nowPlayingButtonClicked(ActionEvent e) {
        if (!nowPlayingButton.isSelected()) {
            nowPlayingButton.setSelected(true);
        }
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
            
            textMatchEndVideo(timeText.getText(), totalTimeText.getText());
            
        });
            
        progressSlider.setOnMousePressed((MouseEvent t) -> {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            
            textMatchEndVideo(timeText.getText(), totalTimeText.getText());
            
        });
            
        progressSlider.setOnMouseDragged((MouseEvent t) -> {
            mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            
            textMatchEndVideo(timeText.getText(), totalTimeText.getText());
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
        
        currentArtist = null;
        currentTrack = null;
        
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
                        }
                    }
                });
                

            }
            
            volumeBar.setDisable(false);
            volumeSlider.setDisable(false);
            volumeButton.setDisable(false);
            
            updateProgressBar();
            updateTimeText();
            updateMediaLenght();
            updateMediaVolume();
            
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
   
    private void updateMediaLenght() {
        mediaPlayer.setOnReady(() -> {
            Duration total = media.getDuration();
            progressSlider.setMax(total.toSeconds());
        });
    }
    private void updateMediaVolume() {
        
        volumeSlider.setValue(mediaPlayer.getVolume()*100);
        
        volumeSlider.valueProperty().addListener((Observable o) -> {
            mediaPlayer.setVolume(volumeSlider.getValue()/100);
            
            if (mediaPlayer.getVolume() == 0) {
                volumeButton.setSelected(true);
            }
            else {
                volumeButton.setSelected(false);
            }
            
        });
    }
    
    private void updateTimeText() {
        timeText.textProperty().bind(Bindings.createStringBinding(() -> {
            
            totalTimeText.setText(getTime(media.getDuration()));
            
            return getTime(mediaPlayer.getCurrentTime());
        }, mediaPlayer.currentTimeProperty()));
    }
    
    private String getTime(Duration time) {
        
        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes();
        int seconds = (int) time.toSeconds();
        
        if (seconds > 59)  {
            seconds %= 60;
        }
        
        if (minutes > 59) {
            minutes %= 60;
        }
        
        if (hours > 59) {
            hours %= 60;
        }
        
        if (hours>0) {
            return String.format("%d:%02d:%02d",
                    hours, minutes, seconds);
        }

        
        return String.format("%02d:%02d",
                 minutes, seconds);
    }
    
    
    // Arreglar bug con Skindred Nobody, posiblemente con el for loop con cada Char del String
    private void textMatchEndVideo(String currentTime, String totalTime) {
        if (!currentTime.equals(totalTime) || totalTime.equals("00:00")) {
            return;
        }
        mediaPlayer.stop();
        
        if (playList.getItems().isEmpty()) {
            playButton.setSelected(false);
            progressBar.setDisable(true);
            progressSlider.setDisable(true);
        }
        else {
            playNextMedia();
        }
    }
    
    private void playNextMedia() {
        if (previousButton.isDisable()) {
            previousButton.setDisable(false);
        }

        // verify if its the last song
        if (fileNumber >= filesPlayList.size() - 2) {
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
}
