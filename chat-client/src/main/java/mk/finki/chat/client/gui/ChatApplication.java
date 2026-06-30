package mk.finki.chat.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ChatApplication extends Application {

    private static ChatController mainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL location = getClass().getResource("chat.fxml");
        if (location == null) {
            System.err.println("Грешка: Не може да се најде chat.fxml");
            System.exit(1);
        }
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = loader.load();
        
        mainController = loader.getController();

        primaryStage.setTitle("Java Chat App");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        
        primaryStage.setOnCloseRequest(e -> {
            if (mainController != null) {
                mainController.shutdown();
            }
        });
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
