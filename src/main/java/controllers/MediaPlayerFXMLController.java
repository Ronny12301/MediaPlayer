package controllers;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
    private BorderPane mainScene;
    
    @FXML
    private Text fileNameText;
    
    @FXML
    private ToggleButton playButton;
    
    @FXML
    private Button nextButton;
    
    @FXML
    private Button previousButton;
    
    @FXML
    private ToggleButton repeatButton;
    
    @FXML
    private ToggleButton randomButton;
    
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
    private Label lblPlaylist;
    @FXML
    private TextField searchPlaylist;
    
    @FXML
    private ToggleButton nowPlayingButton;

    private Map<File, String> filePlayList;
    
    private int fileNumber;
    
    private String path;
    private Media media;
    private MediaPlayer mediaPlayer;
    
    private String currentArtist;
    private String currentTrack;
    
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        visualSyncProgressSliderBar(progressSlider, progressBar);
        visualSyncProgressSliderBar(volumeSlider, volumeBar); 
        searchPlaylistItems();

        smallAlbumArt.fitHeightProperty().bind(smallAlbumArtBackground.heightProperty());
        smallAlbumArt.fitWidthProperty().bind(smallAlbumArtBackground.widthProperty());
        
        // Play tracks in the playlist List using the mouse
        playList.setOnMouseClicked((MouseEvent click) -> {
                        
            if (click.getClickCount() == 2 && !playList.getItems().isEmpty()) {
                playSelectedItem();
            }
        });
        // same but with Enter key
        playList.setOnKeyPressed((key) -> {
            if (key.getCode() == KeyCode.ENTER && !playList.getItems().isEmpty()) {
                playSelectedItem();
            }
        }); 
    }
    private void playSelectedItem() {

        fileNumber = playList.getSelectionModel().getSelectedIndex();

        updateNextPreviousButtonsState();
        initiateMediaPlayer(filePlayList.keySet().toArray()[fileNumber].toString());
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
        
        if (randomButton.isSelected()) {
            fileNumber = new Random().nextInt(filePlayList.size());
        }
        else {
            fileNumber--;
        }
        initiateMediaPlayer(filePlayList.keySet().toArray()[fileNumber].toString());
        playList.getSelectionModel().select(fileNumber);
        playList.scrollTo(fileNumber);
        playList.getFocusModel().focus(oldValue);
        
    }
    
    public void stopButtonClicked(ActionEvent e) {
        progressSlider.setValue(0);
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
            playButton.setSelected(true);
            mediaPlayer.play();
        }
    }
    
    public void chooseFileButtonClicked(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        
        // avoid null pointer exception
        if (file == null) {
            return;
        }
        
        path = file.toString();
        
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        
        initiateMediaPlayer(path);
    }
    
    
    
    public void setRootFolderButtonClicked(ActionEvent e) {
        Map<File,String> oldPlayList = new TreeMap<>();
        
        // if the playList isnt empty save a copy if the user cancels the change
        if (!playList.getItems().isEmpty()) {
            oldPlayList = filePlayList;
            playList.getItems().clear();
        }

        filePlayList = new TreeMap<>();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File rootDirectory = directoryChooser.showDialog(null);

        if (rootDirectory == null) {
            filePlayList = oldPlayList;
            playList.getItems().addAll(filePlayList.values());
            return;
        }
        
        getAllFiles(rootDirectory);
        
        if (filePlayList.isEmpty()) {
            nextButton.setDisable(true);
            previousButton.setDisable(true);
            lblPlaylist.setVisible(false);
            return;
        }
        lblPlaylist.setVisible(true);
 
        playList.getItems().addAll(filePlayList.values());

    }

    public void volumeButtonClicked(ActionEvent e) {
        
        // avoid null pointer exception
        if (mediaPlayer==null) {
            return;
        }
        
        // mute
        if (volumeButton.isSelected()) {
            mediaPlayer.volumeProperty().set(0);
        }
        // if volume is dropped to 0, restore to 100
        else if (volumeSlider.getValue() == 0) {
            volumeSlider.setValue(100);
        }
        // if the button is unSelected restore to previous volume
        else {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
    }
 
    /**
     * Keeps in syc the slider and the progress bar to look like its filled with color
     * 
     * @param slider
     * @param bar
     */
    private void visualSyncProgressSliderBar(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }
    
    public void nowPlayingButtonClicked(ActionEvent e) {
        if (!nowPlayingButton.isSelected()) {
            nowPlayingButton.setSelected(true);
        }
    }
    
    public void searchPlaylistItems() {

        mainScene.setOnKeyTyped((t) -> {
            // escape, backspace, enter
            if (t.getCharacter().equals("\u001B") || t.getCharacter().equals("\b") || t.getCharacter().equals("\n") || t.getCharacter().equals("\r")) {
                
                if (searchPlaylist.isVisible()) {
                    searchPlaylist.setVisible(false);
                }
                return;
            }
            
            if (searchPlaylist.getText().isBlank()) {
                searchPlaylist.setVisible(true);
            }
            
            searchPlaylist.setText(t.getCharacter());
            searchPlaylist.requestFocus();
            searchPlaylist.positionCaret(searchPlaylist.getLength());
        });

        searchPlaylist.setOnKeyPressed((t) -> {
            if (t.getCode().equals(KeyCode.ESCAPE) || t.getCode().equals(KeyCode.ENTER)) {
                searchPlaylist.clear();
                searchPlaylist.setVisible(false);
            }
            
            
            // not being backspace avoids lag when removing al text, holding backspace
            if (!playList.getItems().isEmpty() && !t.getCode().equals(KeyCode.BACK_SPACE)) {
                int index = findKeyIndex(filePlayList, searchPlaylist.getText());
                if (index != -1) {
                    playList.requestFocus();
                    playList.getSelectionModel().select(index);
                    playList.scrollTo(index);
                    searchPlaylist.requestFocus();
                    searchPlaylist.positionCaret(searchPlaylist.getLength());
                }
                
                if (t.getCode().equals(KeyCode.ENTER)) {
                    playSelectedItem();
                    searchPlaylist.setVisible(false);
                }
            }
            
            
        });
    }
    
    private void getAllFiles(File directory) {
        File[] files = directory.listFiles();
        
        for (File file : files) { 
            
            // Recursive call to find files inside directories
            if (file.isDirectory()) {
                getAllFiles(file);
            } 
            // if is a media file, add to the full path as the key, and name to display the ListView
            else if (isVideoFile(file.toString()) || isAudioFile(file.toString())) {
                filePlayList.put(file, file.getName());
            }
        }        
    }
    
    /**
     * Updates the progress bar with the media time
     */
    private void updateProgressBar() {
        progressBar.setDisable(false);
        progressSlider.setDisable(false);
        
        // click-drag bar  (abs value to change in both ways)
        progressSlider.valueProperty().addListener((ov, oldValue, newValue) -> {
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            
            if (Math.abs( currentTime - newValue.doubleValue() ) > 0.1) {
                mediaPlayer.seek( Duration.seconds(newValue.doubleValue()) );
            }
        });
        
        // update visual progress
        mediaPlayer.currentTimeProperty().addListener((ObservableValue<? extends Duration> ov, Duration oldValue, Duration newValue) -> {
            if(!progressSlider.isValueChanging()) {
                progressSlider.setValue(newValue.toSeconds() );
            }
        });
        
        mediaPlayer.setOnEndOfMedia(() -> endOfMedia());
                           
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
        
        if (path == null) {
                return;
        }
        
        try {
            media = new Media(Paths.get(path).toUri().toString());
        } 
        catch (Exception er) {
            System.err.println("Error creating Media: " + er.getMessage());
            return;
        }

        try {
            mediaPlayer = new MediaPlayer(media);
        } catch (Exception er) {
            System.err.println("Error creating Player: " + er.getMessage());
            return;
        }
        
        playButton.setDisable(false);
        playButton.setSelected(true);

        String fileName = (new File(path)).getName();

        if (isVideoFile(path)) {
            fileNameText.setText(fileName);
            setStageName(fileName);
            showMediaVideo();
        } //audio file
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

        updateProgressBar();
        updateTimeText();
        updateMediaLenght();
        updateMediaVolume();

        mediaPlayer.play();
    }

    // todo: use a dedicated metadata handler since not every file format is supported
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
        if (fileNumber >= filePlayList.size() - 1 || filePlayList.isEmpty()) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }

        if (fileNumber <= 0 || filePlayList.isEmpty()) {
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
            totalTimeText.setText(getTime(total));
        });
    }
    private void updateMediaVolume() {
        if (!volumeButton.isSelected()) {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
        else {
            mediaPlayer.setVolume(0);
        }
                
        volumeSlider.valueProperty().addListener((Observable o) -> {
            mediaPlayer.setVolume(volumeSlider.getValue()/100);
            
            // if you set the volume to 0, activate the mute button
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
    
    
    private void endOfMedia() {
        progressSlider.setValue(0);
        
        if (repeatButton.isSelected()){
            return;
        }
        mediaPlayer.stop();
        if (playList.getItems().isEmpty() || nextButton.isDisabled()) {
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
        if (fileNumber >= filePlayList.size() - 2) {
            nextButton.setDisable(true);
        } else {
            nextButton.setDisable(false);
        }
        if (fileNumber > filePlayList.size() - 2) {
            return;
        }

        playList.requestFocus();
        int oldValue = fileNumber;
        
        if (randomButton.isSelected()) {
            fileNumber = new Random().nextInt(filePlayList.size());
            playList.scrollTo(fileNumber);
        }
        else {
            playList.scrollTo(oldValue);
            fileNumber++;
        }
        initiateMediaPlayer(filePlayList.keySet().toArray()[fileNumber].toString());
        playList.getSelectionModel().select(fileNumber);
        playList.getFocusModel().focus(oldValue);
    }
    
    
    public static int findKeyIndex(Map<File, String> map, String searchString) {
        ArrayList<File> keys = new ArrayList<>(map.keySet());
        if (searchString == null || searchString.isEmpty()) {
            return -1;
        }
        System.out.println("Keys list size: " + keys.size());
        for (File key : keys) {
            System.out.println("Checking key: " + key.toString());
            System.out.println("Search string: " + searchString);
            if (key.toString().toLowerCase().contains(searchString.toLowerCase())) {
                System.out.println("Found match: " + key.toString());
                return keys.indexOf(key);
            }
        }
        System.out.println("No match found");
        return -1;
    }

    
    private void setStageName(String track) {
        wmediaplayer.WMediaPlayer.getStage().setTitle(track + " - Media Player 11");
    }
}
