import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;



public class TestProxy {

    private static WebDriver driver;
    private static BrowserMobProxy server;
    private Logger logger = LogManager.getLogger(TestProxy.class);



    @Test
    public void testProxy(){

        driver.get("https://market.yandex.ru");

        List<HarEntry> entries = server.getHar().getLog().getEntries();
        for(HarEntry entry :entries) {
            HarRequest request = entry.getRequest();
            HarResponse response = entry.getResponse();
            logger.info(request.getHttpVersion() + " "
                        + request.getMethod()
                        + "(" + response.getStatus() + "): "
                        + request.getUrl() + " "
                        + ((request.getQueryString().size()>0) ? request.getQueryString().toString() : "")
            );
        }

    }





    @BeforeClass
    public static void setup() {

        /* start proxy server */
        server = new BrowserMobProxyServer();
        server.start();
        int port = server.getPort();
        server.newHar("testproxy");

        /* create proxy */
        Proxy proxy = new Proxy();
        proxy.setHttpProxy("127.0.0.1:".concat(Integer.toString(port)));
        proxy.setSslProxy("127.0.0.1:".concat(Integer.toString(port)));

        /* create webdriver */
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, proxy);
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(capabilities);

    }



    @AfterClass
    public static void teardown() {

        server.stop();

        if (driver != null) {
            driver.quit();
        }

    }
}
