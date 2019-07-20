import com.jfoenix.assets.JFoenixResources;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Setting setting;
    private static Application instance = null;
    Controller controller;

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

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


        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);
        decorator.setGraphic(new SVGGlyph(""));

        Scene scene = new Scene(decorator);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(JFoenixResources.load("css/jfoenix-fonts.css").toExternalForm(),
                JFoenixResources.load("css/jfoenix-design.css").toExternalForm(),
                getClass().getClassLoader().getResource("css/main.css").toExternalForm());

        primaryStage.setScene(scene);
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
