package wmediaplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author ronny12301
 */
public class WMediaPlayer extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MediaPlayerFXML.fxml"));
        stage.setTitle("Media Player 11");
        Scene scene = new Scene(root);
        
        Image icon = new Image("styles/img/Media-Player-icon.png");
        stage.getIcons().add(icon);
        
        stage.setScene(scene);
        stage.show();
    }
    
}
