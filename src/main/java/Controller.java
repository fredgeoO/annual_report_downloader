import crawler.CrawlerAnnualReport;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    Label label_folder_location;
    @FXML
    Label label_stocklist;
    @FXML
    Label label_setting;
    @FXML
    Label label_thread_count;


    @FXML
    ToggleButton toggle_hidden_browser;
    @FXML
    ToggleGroup toggle_thread_count;

    @FXML
    TabPane stage_tabpane;

    @FXML
    ComboBox combobox_annual_report_location;
    @FXML
    ComboBox combobox_stocklist_location;

    @FXML
    TextArea textArea_commandline_output;
    @FXML
    Button btn_download;

    Stage stage;
    StringBuffer sb_commandline;

    ByteArrayOutputStream baos;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        baos = new ByteArrayOutputStream();

        sb_commandline = new StringBuffer(1000);

        label_folder_location.setTooltip(new Tooltip("年报的存放文件夹"));
        label_stocklist.setTooltip(new Tooltip("港股代码列表，格式为txt，每行一个代码，详细格式可参考自动生成的代码列表。"));
        label_thread_count.setTooltip(new Tooltip("开启浏览器的数量，在网速与电脑快的情况下，越多越快。"));
        toggle_hidden_browser.setTooltip(new Tooltip("把浏览器的位置移动到屏幕外，或者移动回原来位置。"));

        textArea_commandline_output.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                textArea_commandline_output.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });


        combobox_annual_report_location.setItems(Main.setting.getAnnualReportLocations());

        SingleSelectionModel ssm_annual_report = combobox_annual_report_location.getSelectionModel();

        StringProperty selected_annual_report_location = Main.setting.getString("selected_annual_report_location");
        ssm_annual_report.select(selected_annual_report_location.get());

        ssm_annual_report.selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                selected_annual_report_location.setValue((String) newValue);


            }
        });


        combobox_stocklist_location.setItems(Main.setting.getStockCodeList());


        SingleSelectionModel ssm_stocklist = combobox_stocklist_location.getSelectionModel();

        StringProperty selected_stocklist = Main.setting.getString("selected_stock_list");
        ssm_stocklist.select(selected_stocklist.get());
        ssm_stocklist.selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                ObservableList items = combobox_stocklist_location.getItems();
                selected_stocklist.setValue((String) newValue);

            }
        });


    }

    public void OnMouseClicked_btn_download(MouseEvent mouseEvent) {
        Button btn = (Button) mouseEvent.getSource();
        btn.setText("正在下载");
        btn.setDisable(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {


                int thread_count = Integer.parseInt((
                        (ToggleButton) toggle_thread_count.getSelectedToggle()).getText());
                String report_location = combobox_annual_report_location.
                        getSelectionModel().getSelectedItem().toString();
                String list_location = combobox_stocklist_location.
                        getSelectionModel().getSelectedItem().toString();

                //async method
                CrawlerAnnualReport.download(report_location, list_location, thread_count);


                PrintStream ps_old = System.out;

                CrawlerAnnualReport.set_commandline_output(new PrintStream(baos));

                // wait when crawler is downloading
                while (CrawlerAnnualReport.is_running()) {


                    sb_commandline.append(baos.toString());


                    baos.reset();

                    textArea_commandline_output.setText(sb_commandline.toString());
                    textArea_commandline_output.setScrollTop(Double.MAX_VALUE);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                CrawlerAnnualReport.set_commandline_output(ps_old);
                btn.setText("下载");
                btn.setDisable(false);


            }
        });

        t.start();


    }

    public void OnMouseClicked_link(MouseEvent mouseEvent) {
        Hyperlink link = (Hyperlink) mouseEvent.getSource();

        Main.showDocument(link.getText());

    }

    public void OnMouseClicked_btn_cancel(MouseEvent mouseEvent) {

        CrawlerAnnualReport.shutdown();
        btn_download.setText("下载");
        btn_download.setDisable(false);
    }

    public void OnMouseClicked_choose_output_folder(MouseEvent mouseEvent) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File("."));
        File output_folder = dc.showDialog(stage);

        combobox_annual_report_location.getItems().add(output_folder.getAbsolutePath());
        combobox_annual_report_location.getSelectionModel().selectLast();


    }

    public void OnMouseClicked_choose_stockcode_txt(MouseEvent mouseEvent) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (Stockcode List)", "*.txt"));
        File stocklist_txt = fc.showOpenDialog(stage);

        combobox_stocklist_location.getItems().add(stocklist_txt.getAbsolutePath());
        combobox_stocklist_location.getSelectionModel().selectLast();
    }

    public void OnStart(Stage primaryStage) {
        stage = primaryStage;
    }

    public void stop() {

    }

    public void OnMouseClicked_toggle_hidden_browser(MouseEvent mouseEvent) {

        ToggleButton tb = (ToggleButton) mouseEvent.getSource();
        CrawlerAnnualReport.setVisible(!tb.isSelected());
    }
}
