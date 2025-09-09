package ui.jfx.cycles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("cycles.fxml"));
        HBox root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Cycles Randomizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

