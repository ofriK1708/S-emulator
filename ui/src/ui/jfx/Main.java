package ui.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class Main extends Application {
    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL resource = getClass().getResource("app.fxml");
        fxmlLoader.setLocation(resource);
        if (resource != null) {
            Parent root = fxmlLoader.load(resource.openStream());
            Scene scene = new Scene(root, 850, 500);
            primaryStage.setTitle("S-emulator");
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            System.out.println("Resource is null");
        }

    }
}
