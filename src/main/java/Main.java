import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Setting setting;
    private static Application instance = null;
    Controller controller;

    public static void showDocument(String link) {
        HostServicesDelegate hostServices = HostServicesFactory.getInstance(instance);
        hostServices.showDocument(link);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        setting = new Setting();


        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.OnStart(primaryStage);

        primaryStage.setTitle("港股年报下载器");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(480);
        primaryStage.show();


        instance = this;
    }

    @Override
    public void stop() {
        System.out.println("Stage is closing");
        controller.stop();
        // Save file
        setting.save();
    }
}
