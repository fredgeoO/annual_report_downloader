package crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CrawlerUtil {
    public static ArrayList<String> get_linefromtxt(String filepath) {
        ArrayList<String> lines = new ArrayList<>();


        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(filepath));
            String s = null;
            while ((s = br.readLine()) != null) {


                lines.add(s);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}
