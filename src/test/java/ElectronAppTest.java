import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ElectronAppTest {
    private static Browser browser;
    private static Process electronProcess;

    @BeforeAll
    static void setUp() throws IOException, InterruptedException {
        String electronAppPath = Paths.get("electron-app").toAbsolutePath().toString();
        ProcessBuilder builder = new ProcessBuilder("npm", "start");
        builder.directory(Paths.get(electronAppPath).toFile());
        electronProcess = builder.start();

        // Give the Electron app time to start up
        Thread.sleep(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(electronProcess.getErrorStream()));
        String line;
        String wsUrl = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("DevTools listening on")) {
                 wsUrl = line.substring("DevTools listening on".length()).trim();
                // Use wsUrl to connect to the Electron app using Playwright's connectOverCDP method
                break;
            }
        }
        System.out.println("Detected DevTools URL: " + wsUrl);

        Playwright playwright = Playwright.create();
        var cdpOptions = new BrowserType.ConnectOverCDPOptions()
                .setSlowMo(100);
        browser = playwright.chromium().connectOverCDP(wsUrl, cdpOptions);
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        electronProcess.destroy();
    }

    @Test
    void testHelloWorld() {
        Page page = browser.newPage();

        assertEquals("Hello World!", page.innerText("#displayText"));

        page.fill("#inputText", "IT WORKS");
        page.click("#submitButton");

        assertEquals("IT WORKS", page.innerText("#displayText"));
    }
}
