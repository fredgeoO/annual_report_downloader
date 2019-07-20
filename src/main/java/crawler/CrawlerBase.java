package crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CrawlerBase {
    protected ChromeOptions options;
    protected String download_folderpath = "";

    public CrawlerBase() {
        options = new ChromeOptions();
    }

    protected static void switchToNewWindow(WebDriver d) {
        for (String winHandle : d.getWindowHandles()) {
            d.switchTo().window(winHandle);
        }
    }

    protected static boolean click_btn(WebDriver d, By b) {
        WebElement btn = d.findElement(b);
        if (btn != null) {

            btn.click();
        }

        return btn != null;
    }

    public CrawlerBase set_download_folder(String folderpath) {
        download_folderpath = folderpath;
        Path download_folder = Paths.get(download_folderpath);


        // define the preferences
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", download_folderpath);
        chromePrefs.put("download.prompt_for_download", false);
        chromePrefs.put("download.directory_upgrade", true);
        chromePrefs.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
        chromePrefs.put("plugins.always_open_pdf_externally", true);

        chromePrefs.put("safebrowsing_for_trusted_sources_enabled", false);
        chromePrefs.put("safebrowsing.enabled", false);

        options.addArguments("--test-type");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");
        options.addArguments("--verbose");

        options.setExperimentalOption("prefs", chromePrefs);


        return this;
    }


    public ChromeDriver get_headlessdriver() {
        options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);


        driver.manage().timeouts().implicitlyWait(1l, TimeUnit.SECONDS);

        return driver;
    }

    public ChromeDriver get_chromedriver() {
        ChromeDriver driver = new ChromeDriver(options);


        return driver;
    }
}
