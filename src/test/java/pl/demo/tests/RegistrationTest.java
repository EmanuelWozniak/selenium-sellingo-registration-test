package pl.demo.tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import pl.demo.DriverFactory;
import pl.demo.pages.RegistrationPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationTest {

    private WebDriver driver;
    private String baseUrl;

    private String testEmail;
    private final String testPassword = "Qaz!2345Qaz!2345";

    @BeforeEach
    void setUp() {
        driver = DriverFactory.create();
        driver.manage().window().maximize();
        baseUrl = System.getProperty("baseUrl", "https://demo.sellingo.pl");

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        testEmail = "auto+" + stamp + "@example.com";
        System.out.printf("Używam danych: email=%s, hasło=%s%n", testEmail, testPassword);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Rejestracja → komunikat sukcesu → wylogowanie")
    void registerThenLogout() {
        RegistrationPage page = new RegistrationPage(driver, baseUrl)
                .open()
                .typeEmail(testEmail)
                .typePassword(testPassword)
                .acceptTerms();
        page.submit();
        assertTrue(
                page.isSuccessMessageVisible(),
                "Nie znaleziono komunikatu o pomyślnej rejestracji i autologowaniu."
        );
        page.tryLogout();

    }
}
