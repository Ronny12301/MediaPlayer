package controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TreeMap;

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

import helpers.Playback;
import helpers.Playlist;

import static helpers.Misc.setStageName;

/**
 * FXML Controller class
 *
 * @author ronny12301
 */
public class MediaPlayerController implements Initializable {

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
    private ListView<String> playList;
    @FXML
    private Label lblPlaylist;
    @FXML
    private TextField searchPlaylist;
    
    @FXML
    private ToggleButton nowPlayingButton;

    private Map<File, String> filePlayList;
    
    private int fileNumber;

    private MediaPlayer mediaPlayer;
    
    private String currentArtist;
    private String currentTrack;
    
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Playback.visualSyncProgressSliderBar(progressSlider, progressBar);
        Playback.visualSyncProgressSliderBar(volumeSlider, volumeBar);
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
    public void playSelectedItem() {
        fileNumber = playList.getSelectionModel().getSelectedIndex();

        Playback.updateNextPreviousButtonsState(nextButton, previousButton, fileNumber, filePlayList);
        initiateMediaPlayer(filePlayList.keySet().toArray()[fileNumber].toString());
    }
    
    public void nextButtonClicked(ActionEvent e) {
        playNextMedia();
    }
    public void previousButtonClicked(ActionEvent e) {
        if (nextButton.isDisable()) {
            nextButton.setDisable(false);
        }
        
        // verify if it's the first song
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

        String path = file.toString();
        
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        
        initiateMediaPlayer(path);
    }

    public void setRootFolderButtonClicked(ActionEvent e) {
        Map<File,String> oldPlayList = new TreeMap<>();
        
        // if the playList isn't empty save a copy if the user cancels the change
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
        
        Playlist.getAllFiles(rootDirectory, filePlayList);
        
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
    
    public void nowPlayingButtonClicked(ActionEvent e) {
        if (!nowPlayingButton.isSelected()) {
            nowPlayingButton.setSelected(true);
        }
    }

    private void searchPlaylistItems() {

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

            // ignoring backspace avoids lag when removing all text, while holding backspace
            if (!playList.getItems().isEmpty() && !t.getCode().equals(KeyCode.BACK_SPACE)) {
                int index = Playlist.getSearchedFileIndex(filePlayList, searchPlaylist.getText());
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

        Media media;
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

        if (Playlist.isVideoFile(path)) {
            fileNameText.setText(fileName);
            setStageName(fileName);
            Playback.showMediaVideo(mediaView, mediaPlayer, smallAlbumArt, screenBackground);
        } //audio file
        else {
            // set the file names
            setStageName(fileName);
            fileNameText.setText(fileName);

            imageView.setVisible(true);

            // if it has metadata it will overwrite the file names, if not, it will stay the same
            media.getMetadata().addListener((MapChangeListener.Change<? extends String, ?> ch) -> {
                if (ch.wasAdded()) {
                    handleMetadata(ch.getKey(), ch.getValueAdded());

                    if (currentArtist != null && currentTrack != null) {
                        setStageName(currentTrack + " - " + currentArtist);
                        fileNameText.setText(currentTrack);
                    }
                }
            });
        }

        Playback.updateTimeText(timeText, mediaPlayer);
        Playback.updateProgressBar(progressBar, progressSlider, mediaPlayer);
        Playback.updateMediaLength(mediaPlayer, media, progressSlider, totalTimeText);
        Playback.updateMediaVolume(volumeButton, volumeSlider, mediaPlayer);
        mediaPlayer.setOnEndOfMedia(this::endOfMedia);

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

        // verify if it's the last song
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

}
