package helpers;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;

public class Playback {

    /**
     * Bind the text with the current timestamp of a file
     * @param timeText
     * @param mediaPlayer
     */
    public static void updateTimeText(Text timeText, MediaPlayer mediaPlayer) {
        timeText.textProperty().bind(Bindings.createStringBinding(() -> {

            return getTime(mediaPlayer.getCurrentTime());

        }, mediaPlayer.currentTimeProperty()));
    }

    /**
     * Updates the duration in the UI when a song/video starts
     * @param mediaPlayer
     * @param media
     * @param progressSlider
     * @param totalTimeText
     */
    public static void updateMediaLength(MediaPlayer mediaPlayer, Media media, Slider progressSlider, Text totalTimeText) {
        mediaPlayer.setOnReady(() -> {
            Duration total = media.getDuration();

            progressSlider.setMax(total.toSeconds());
            totalTimeText.setText(getTime(total));
        });
    }

    /**
     * Updates the media to the assigned volume
     * @param volumeButton speaker icon button
     * @param volumeSlider
     * @param mediaPlayer jfx player instance
     */
    public static void updateMediaVolume(ToggleButton volumeButton, Slider volumeSlider, MediaPlayer mediaPlayer) {
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

    /**
     * Enables or disables the Next and Previous tracks buttons
     * @param nextButton
     * @param previousButton
     * @param fileNumber current file index
     * @param filePlayList playlist's files routes
     */
    public static void updateNextPreviousButtonsState(Button nextButton, Button previousButton,
                                                      int fileNumber, Map<File, String> filePlayList) {

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

    /**
     * Get a timestamp with the format: H:MM:SS
     * @param time
     * @return
     */
    public static String getTime(Duration time) {
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

    /**
     * Keeps in syc the slider and the progress bar to look like it's filled with color
     *
     * @param slider
     * @param bar
     */
    public static void visualSyncProgressSliderBar(Slider slider, ProgressBar bar) {
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            bar.setProgress(newValue.doubleValue()/slider.getMax());
        });
    }

    /**
     * Updates the progress bar with the media time
     */
    public static void updateProgressBar(ProgressBar progressBar, Slider progressSlider, MediaPlayer mediaPlayer) {
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

    }

    /**
     * Show the video when a Video file is played
     * @param mediaView
     * @param mediaPlayer
     * @param smallAlbumArt
     * @param screenBackground
     */
    public static void showMediaVideo(MediaView mediaView, MediaPlayer mediaPlayer, ImageView smallAlbumArt, VBox screenBackground) {
        smallAlbumArt.setImage(new Image(Playback.class.getResource("/styles/img/Media-Player-icon.png").toExternalForm()));
        mediaView.setVisible(true);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitHeightProperty().bind(screenBackground.heightProperty());
        mediaView.fitWidthProperty().bind(screenBackground.widthProperty());
    }

}
