package crawler;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CrawlerAnnualReport extends crawler.CrawlerBase {

    // static variable for threading
    static ArrayList<Thread> threads;
    static ArrayList<CrawlerAnnualReport> instances = new ArrayList<>();
    static int thread_count;
    static boolean is_running = true;
    static boolean visible = false;

    ChromeDriver driver = null;
    DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    Calendar cal = Calendar.getInstance();
    String company_name;
    String folderpath;

    public CrawlerAnnualReport() {

    }

    public CrawlerAnnualReport(String folderpath) {

        super();
        set_download_folder(folderpath);
        driver = get_chromedriver();
        driver.manage().window().setSize(new Dimension(1300, 900));
        if (!visible) {
            driver.manage().window().setPosition(new Point(10000, 10000));
        }

    }

    public static Thread create_crawler_thread(int total_thread, int idx_thread,
                                               ArrayList<String> fractional_stockcodes_info, String folderpath) {


        int size = fractional_stockcodes_info.size();

        Thread t = new Thread(() -> {

            CrawlerAnnualReport crwaler = new CrawlerAnnualReport(folderpath);
            instances.add(crwaler);

            for (int i = 0; i < size; i++) {

                if (i % total_thread == idx_thread) {

                    String info = fractional_stockcodes_info.get(i);
                    String stock_code = info.substring(0, 5);

                    try {
                        crwaler.download_annual_report(stock_code, folderpath);
                    } catch (InterruptedException e) {
                        System.out.println("线程关闭中");
                        crwaler.driver.close();
                        continue;
                    } catch (NoSuchSessionException e) {
                        System.out.println("线程关闭中");
                        crwaler.driver.close();
                        continue;
                    }
                }

            }

            instances.remove(crwaler);
            crwaler.finalize();
        });
        return t;
    }

    public static void set_commandline_output(PrintStream ps) {
        System.setOut(ps);
    }

    public static boolean is_running() {
        for (int i = 0; i < threads.size(); i++) {

            if (threads.get(i).isAlive()) return true;

        }

        return false;
    }

    public static void join() {
        if (!is_running) {
            System.out.println("thread is not running.");
            return;
        }
        for (int i = 0; i < threads.size(); i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        is_running = false;
    }

    public static void shutdown() {
        if (!is_running) {
            System.out.println("thread is not running.");
            return;
        }
        for (int i = 0; i < instances.size(); i++) {
            try {

                instances.get(i).driver.close();
            } catch (NoSuchSessionException e) {
                System.out.println("线程已经关闭");
            }

        }
        is_running = false;

    }

    public static void download(String folderpath, String listpath, int thread_count) {

        is_running = true;

        if (folderpath.lastIndexOf("/") != folderpath.length() - 1) {
            folderpath += "/";
        }


        // stock code with additional data , need to process to get real stock code
        ArrayList<String> stockcode_info = CrawlerUtil.get_linefromtxt(
                listpath);

        // threading
        CrawlerAnnualReport.thread_count = thread_count;
        threads = new ArrayList<>();
        instances = new ArrayList<>();

        for (int i = 0; i < thread_count; i++) {
            Thread t = create_crawler_thread(thread_count, i,
                    stockcode_info, folderpath);
            t.start();
            threads.add(t);
        }


    }

    public static void setVisible(boolean visible) {
        CrawlerAnnualReport.visible = visible;
        int i = 0;
        for (CrawlerAnnualReport instance : instances) {
            if (visible) {
                instance.driver.manage().window().setPosition(new Point(50 + i * 25, 50 + i * 25));
            } else {
                instance.driver.manage().window().setPosition(new Point(10000, 10000));
            }
            i++;
        }

    }

    protected void finalize() {
        driver.quit();
    }


    public void download_annual_report(String stock_code, String folderpath) throws InterruptedException {

        this.folderpath = folderpath;

        driver.get("https://www1.hkexnews.hk/search/titlesearch.xhtml?lang=zh");
        company_name = select_fromcombobox(stock_code);

        System.out.println("公司名稱: " + company_name);

        WebElement we_dataframe = get_dataframe();

        // key: old filename value: new filename
        HashMap<String, String> filename_pairs = download_pdf(we_dataframe);

        rename_pdf(filename_pairs);

    }

    /**
     * @param stock_code
     * @return company_name
     */
    protected String select_fromcombobox(String stock_code) throws InterruptedException {
        WebElement searchStockCode = driver.findElement(By.id("searchStockCode"));
        searchStockCode.sendKeys(stock_code);
        searchStockCode.click();


        Thread.sleep(1500);


        Actions action = new Actions(driver);
        WebElement search_dropdown = driver.findElement(By.xpath("//*[@id=\"autocomplete-list-0\"]/div[1]/div[1]/table/tbody/tr[1]"));
        action.moveToElement(search_dropdown).perform();

        String company_name = search_dropdown.findElement(By.xpath("//td[2]/span")).getText();

        action.moveToElement(search_dropdown).click().perform();

        return company_name;
    }

    protected WebElement get_dataframe() {

        click_btn(driver, By.xpath("//*[@id=\"tier1-select\"]/div/div/a"));
        click_btn(driver, By.xpath("//*[@id=\"hkex_news_header_section\"]/section/div[1]/div/div[2]/ul/li[4]/div/div[2]/div[1]/div[2]/div/div/div/div[1]/div[2]/a"));
        click_btn(driver, By.xpath("//*[@id=\"rbAfter2006\"]/div[1]/div/div/a"));

        // 財務報表/環境、社會及管治資料
        click_btn(driver, By.xpath("//*[@id=\"rbAfter2006\"]/div[2]/div/div/div/ul/li[5]/a"));

        // 年報 (combo-box drop down item)
        click_btn(driver, By.linkText("年報"));

        // 搜索按钮
        click_btn(driver, By.xpath("//*[@id=\"hkex_news_header_section\"]/section/div[1]/div/div[3]/a[2]"));

        WebElement tbody_polite = driver.findElement(By.xpath("//*[@id=\"titleSearchResultPanel\"]/div/div[1]/div[3]/div[2]/table/tbody"));

        return tbody_polite;
    }

    protected int get_year(WebElement we_time) {


        int year = -1;
        try {
            Date t = sdf.parse(we_time.getText());
            cal.setTime(t);
            year = cal.get(Calendar.YEAR);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return year;
    }

    protected HashMap<String, String> download_pdf(WebElement we_dataframe) throws InterruptedException {
        List<WebElement> doc_links = we_dataframe.findElements(By.tagName("a"));
        List<WebElement> wes_info_release_time = we_dataframe.findElements(By.cssSelector("[class='text-right release-time']"));


        // key: old filename value: new filename
        HashMap<String, String> filename_pairs = new HashMap<>();

        for (int i = 0; i < doc_links.size(); i++) {
            WebElement we_link = doc_links.get(i);
            String link_title = we_link.getText();

            // skip some pdf that is not annual report
            if (link_title.contains("精要") ||
                    !(link_title.contains("年度報告") || link_title.contains("年報"))) {
                continue;
            }

            // info time from website
            WebElement we_time = wes_info_release_time.get(i);
            int year = get_year(we_time);


            String link = we_link.getAttribute("href");
            if (link.toLowerCase().endsWith(".pdf")) {

                String original_filename = link.substring(link.lastIndexOf("/") + 1);
                String new_filename = (year - 1) + "-" + company_name + "年報.pdf";

                File annual_report = new File(folderpath + "\\" + company_name + "\\" + new_filename);

                if (!annual_report.exists()) {

                    // download file
                    we_link.click();
                    System.out.println("[" + company_name + "] file is downloading: " + new_filename);
                    Thread.sleep(125);
                    filename_pairs.put(original_filename, new_filename);

                } else {
                    System.out.println("[" + company_name + "] file was downloaded: " + new_filename);
                }


            }

        }
        return filename_pairs;
    }

    protected void rename_pdf(HashMap<String, String> filename_pairs) throws InterruptedException {
        int total_file = filename_pairs.size();

        // folder for storing all annual report of specified company
        File company_folder = new File(folderpath + company_name + "\\");
        System.out.println("[" + company_name + "] folder saved: " + company_folder.getAbsolutePath());

        if (!company_folder.exists()) {
            company_folder.mkdirs();
        }

        // wait untial all file was downloaded and rename
        while (filename_pairs.size() != 0) {

            Iterator<Map.Entry<String, String>> itr = filename_pairs.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<String, String> entrySet = itr.next();

                //System.out.println("检查文件: " +folderpath + entrySet.getKey());
                File f = new File(folderpath + entrySet.getKey());

                if (f.exists() && f.canWrite()) {
                    f.renameTo(new File(company_folder.getAbsolutePath() + "\\" + entrySet.getValue()));
                    System.out.println("[" + company_name + "] file name has changed: " + entrySet.getKey() + " " + entrySet.getValue());
                    itr.remove();
                }
            }


            Thread.sleep(1000);

        }
        System.out.println("[" + company_name + "] " + total_file + " files was downloaded.");
    }

}
