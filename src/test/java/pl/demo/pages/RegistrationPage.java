package pl.demo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.List;

public class RegistrationPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String url;
    private final By emailInput = By.cssSelector("form.js-register-form input[name='email']");
    private final By passwordInput = By.cssSelector("form.js-register-form input[name='password']");
    private final By passwordConfirmInput = By.cssSelector("form.js-register-form input[name='password_confirm']");
    private final By submitBtn = By.cssSelector("form.js-register-form button.js-submit-registry");
    private final By acceptContainer = By.cssSelector("form.js-register-form label.c-checkbox-field__checkbox-container");
    private final By acceptCheckmark = By.cssSelector("form.js-register-form label.c-checkbox-field__checkbox-container .c-checkbox-field__checkmark");
    private final By acceptCheckbox = By.cssSelector("form.js-register-form input[name='accept']");
    private final By successCloseX = By.cssSelector(".l-popup__message-close.at-message-close.js-close-popup");


    public RegistrationPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.url = baseUrl.endsWith("/") ? baseUrl + "rejestracja" : baseUrl + "/rejestracja";
    }

    public RegistrationPage open() {
        driver.get(url);
        waitForPageReady();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitBtn));
        wait.until(ExpectedConditions.visibilityOfElementLocated(acceptContainer));
        dismissCookieBannerIfPresent();
        return this;
    }

    public RegistrationPage typeEmail(String email) {
        wait.until(ExpectedConditions.elementToBeClickable(emailInput)).sendKeys(email);
        return this;
    }

    public RegistrationPage typePassword(String pwd) {
        wait.until(ExpectedConditions.elementToBeClickable(passwordInput)).sendKeys(pwd);
        wait.until(ExpectedConditions.elementToBeClickable(passwordConfirmInput)).sendKeys(pwd);
        return this;
    }

    public RegistrationPage acceptTerms() {
        try {
            dismissCookieBannerIfPresent();
            WebElement container = wait.until(ExpectedConditions.visibilityOfElementLocated(acceptContainer));
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(acceptCheckbox));

            scrollIntoView(container);

            try {
                WebElement checkmark = wait.until(ExpectedConditions.elementToBeClickable(acceptCheckmark));
                new Actions(driver).moveToElement(checkmark).click().perform();
                if (input.isSelected()) return this;
            } catch (Exception ignored) { }

            try {
                container = wait.until(ExpectedConditions.elementToBeClickable(acceptContainer));
                new Actions(driver).moveToElement(container).click().perform();
                if (input.isSelected()) return this;
            } catch (Exception ignored) { }

            ((JavascriptExecutor) driver).executeScript(
                    "var cb=document.querySelector(\"form.js-register-form input[name='accept']\");" +
                            "if(cb){cb.checked=true;" +
                            "cb.dispatchEvent(new Event('input',{bubbles:true}));" +
                            "cb.dispatchEvent(new Event('change',{bubbles:true}));}"
            );
            ((JavascriptExecutor) driver).executeScript(
                    "var mk=document.querySelector(\"form.js-register-form label.c-checkbox-field__checkbox-container .c-checkbox-field__checkmark\");" +
                            "if(mk){['mousedown','mouseup','click'].forEach(function(t){" +
                            "mk.dispatchEvent(new MouseEvent(t,{bubbles:true}));});}"
            );

            wait.until(d -> {
                WebElement cb = (WebElement)((JavascriptExecutor)d)
                        .executeScript("return document.querySelector(\"form.js-register-form input[name='accept']\");");
                return cb != null && cb.isSelected();
            });
            return this;

        } catch (Exception ex) {
            dumpDebug("acceptTerms_error", acceptContainer);
            throw ex;
        }
    }

    public void submit() {
        scrollIntoView(wait.until(ExpectedConditions.presenceOfElementLocated(submitBtn)));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }

    public boolean isSuccessMessageVisible() {
        try {
            By successMsg = By.xpath("//*[contains(normalize-space(.),'Dziękujemy, zostałeś automatycznie zalogowany na swoje konto.')]");
            wait.until(ExpectedConditions.presenceOfElementLocated(successMsg));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean tryLogout() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        try {
            List<WebElement> closeXs = driver.findElements(successCloseX);
            if (!closeXs.isEmpty()) {
                WebElement x = wait.until(ExpectedConditions.elementToBeClickable(successCloseX));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", x);
                x.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(successCloseX));
            }

            By accountBtn = By.id("header-account");
            wait.until(ExpectedConditions.elementToBeClickable(accountBtn)).click();

            By logoutLocator = By.xpath("//a[normalize-space()='Wyloguj' or contains(.,'Wyloguj')]");
            wait.until(ExpectedConditions.elementToBeClickable(logoutLocator)).click();

            String expectedUrl = "https://demo.sellingo.pl/";
            wait.until(ExpectedConditions.urlToBe(expectedUrl));

            return driver.getCurrentUrl().equals(expectedUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void waitForPageReady() {
        ExpectedCondition<Boolean> jsLoad = wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete");
        wait.until(jsLoad);
        try {
            wait.until(wd -> (Long) ((JavascriptExecutor) wd)
                    .executeScript("return (window.jQuery ? jQuery.active : 0)") == 0L);
        } catch (Exception ignored) { }
    }

    private void dismissCookieBannerIfPresent() {
        List<By> candidates = List.of(
                By.xpath("//button[normalize-space()='Akceptuję' or normalize-space()='Akceptuj']"),
                By.xpath("//button[contains(.,'Zgadzam') or contains(.,'Zgoda')]"),
                By.xpath("//a[contains(.,'Akceptuj') or contains(.,'Zgadzam')]"),
                By.cssSelector("div[id*='cookie'], div[class*='cookie'] button")
        );
        for (By by : candidates) {
            try {
                WebElement el = new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.elementToBeClickable(by));
                el.click();
                break;
            } catch (Exception ignored) { }
        }
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('[class*=cookie],[id*=cookie],[class*=consent]')" +
                            ".forEach(e=>e.style.display='none');");
        } catch (Exception ignored) { }
    }

    private void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }
    private void dumpDebug(String tag, By elementToHighlight) {
        try { highlight(elementToHighlight); } catch (Exception ignored) {}
        try { takeScreenshot(tag); } catch (Exception ignored) {}
        try { savePageSource(tag); } catch (Exception ignored) {}

        try {
            WebElement el = driver.findElement(elementToHighlight);
            String outer = (String)((JavascriptExecutor)driver)
                    .executeScript("return arguments[0].outerHTML;", el);
            System.out.println("=== DEBUG outerHTML ["+ tag +"] ===\n" + outer + "\n==============================");
        } catch (Exception e) {
            System.out.println("outerHTML not available: " + e.getMessage());
        }
    }

    private void highlight(By by) {
        WebElement el = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.outline='3px solid magenta'; arguments[0].scrollIntoView({block:'center'})", el);
    }

    private void takeScreenshot(String name) throws Exception {
        File dir = new File("target/artifacts");
        if (!dir.exists()) dir.mkdirs();
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        java.nio.file.Files.copy(src.toPath(), new File(dir, name + ".png").toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Screenshot saved: target/artifacts/" + name + ".png");
    }

    private void savePageSource(String name) throws Exception {
        File dir = new File("target/artifacts");
        if (!dir.exists()) dir.mkdirs();
        java.nio.file.Files.writeString(new File(dir, name + ".html").toPath(), driver.getPageSource());
        System.out.println("PageSource saved: target/artifacts/" + name + ".html");
    }
}
