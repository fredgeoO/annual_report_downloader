import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.Properties;

public class Setting {

    Properties saveProps;
    String key_base_report_location = "output_path";
    String key_base_stockcodes = "stockcode_path";
    String xml_file_setting = "settings.xml";
    String folderpath = "";

    public Setting() {
        folderpath = new File(".").getAbsolutePath();
        folderpath = folderpath.substring(0, folderpath.length() - 1);


        // Make default list
        File Stockcode_txt = new File(folderpath + "/big_list.txt");


        if (!Stockcode_txt.exists()) {

            copyFileFromResource("/hsi_fractional_stockcode.txt", Stockcode_txt);

        }

        Stockcode_txt = new File(folderpath + "/small_list.txt");

        if (!Stockcode_txt.exists()) {

            copyFileFromResource("/small_list.txt", Stockcode_txt);

        }

        // Make default output folder
        File annual_report_folder = new File(folderpath + "\\annual_report\\");

        if (!annual_report_folder.exists()) {
            annual_report_folder.mkdir();
        }

        File setting_xml = new File(folderpath + "/" + xml_file_setting);

        saveProps = new Properties();

        if (!setting_xml.exists()) {

            // Create first Settings
            saveProps.setProperty("stockcode_path0", Stockcode_txt.getAbsolutePath());
            saveProps.setProperty("output_path0", annual_report_folder.getAbsolutePath() + "\\");
            save();

        } else {
            load();
        }
    }

    public ObservableList<String> getAnnualReportLocations() {

        return getStringList(key_base_report_location, 10);


    }

    public ObservableList<String> getStockCodeList() {
        return getStringList(key_base_stockcodes, 10);
    }

    public StringProperty getString(String key) {
        StringProperty value = new SimpleStringProperty();

        String result = saveProps.getProperty(key);
        value.setValue(result);

        value.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                saveProps.setProperty(key, newValue);
            }
        });

        return value;
    }

    public ObservableList<String> getStringList(String key_base, int max_size) {

        ObservableList<String> list = FXCollections.observableArrayList();

        String result = null;

        for (int i = 0; i < max_size; i++) {
            result = saveProps.getProperty(key_base + i);
            if (result != null) {
                list.add(result);
            } else {
                break;
            }

        }

        // when list size > 5, remove the first one. refresh properties
        list.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                //System.out.println(list.size());
                while (c.next()) {
                    if (c.wasAdded()) {
                        if (list.size() > max_size) {
                            list.remove(0);

                        }

                        for (int i = 0; i < list.size(); i++) {
                            saveProps.setProperty(key_base + i, list.get(i));
                        }
                    }


                }

            }
        });

        return list;

    }

    public void save() {
        try {
            saveProps.storeToXML(
                    new FileOutputStream(xml_file_setting), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            saveProps.loadFromXML(
                    new FileInputStream(xml_file_setting));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void copyFileFromResource(String res, File dest) {

        InputStream is = null;
        OutputStream os = null;
        try {
            is = getClass().getResourceAsStream(res);

            os = new FileOutputStream(dest);


            // the size of the buffer doesn't have to be exactly 1024 bytes, try playing around with this number and see what effect it will have on the performance
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
