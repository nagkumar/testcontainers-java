import com.example.DemoApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple example of plain Selenium usage.
 */
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = SeleniumContainerTest.Initializer.class)
public class SeleniumContainerTest {

    @LocalServerPort
    private int port;

    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer()
        .withCapabilities(new ChromeOptions())
        .withRecordingMode(VncRecordingMode.RECORD_ALL, new File("build"));

    @BeforeEach
    public void setUp() {
        chrome.start();
    }

    @AfterEach
    public void tearDown() {
        chrome.stop();
    }

    @Test
    public void simplePlainSeleniumTest() {
        RemoteWebDriver driver = new RemoteWebDriver(chrome.getSeleniumAddress(), new ChromeOptions());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        driver.get("http://host.testcontainers.internal:" + port + "/foo.html");
        List<WebElement> hElement = driver.findElements(By.tagName("h"));

        assertThat(hElement).as("The h element is found").isNotEmpty();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(
                (ApplicationListener<WebServerInitializedEvent>) event -> {
                    Testcontainers.exposeHostPorts(event.getWebServer().getPort());
                }
            );
        }
    }
}
