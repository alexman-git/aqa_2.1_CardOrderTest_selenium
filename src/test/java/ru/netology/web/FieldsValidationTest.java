package ru.netology.web;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FieldsValidationTest {
    private WebDriver driver;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.get("http://localhost:9999");
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // 1) тестируем валидацию поля "Фамилия и имя",
    // в поле "Мобильный телефон" - всегда валидное значение, чек-бокс на согласие всегда нажат
    // Для критериев валидации использовались ТОЛЬКО требования в ДЗ,
    // хотя в приложении-заглушке валидация данного поля настроена для "галочки" (очень плохо).
    // Например, считаются валидными: имена и фамилии в одну букву с пробелом, наличие дефиса в начале и/или в конце имени,
    // нет минимального и максимального предела на ввод, имя фамилия "--  --" также валидное и т.д.
    @ParameterizedTest
    @CsvSource(value = {"emptyName,''",
            "oneLetterNameLatin,q",
            "nameWithDigits,Павел0",
            "nameWithSpecialSymbols,Павел_Павел_@#$"})
    void shouldNotValidateInputName(String testcase, String name) {
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+79991234567");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='name'].input_invalid .input__sub")));
    }

    @ParameterizedTest
    @CsvSource(value = {"oneLetterNameCyrillic(minBoundaryTestInField),а",
            "oneLetterNameSurnameWithSpaceCyrillic(minBoundaryTestInField),а д",
            "extremelyLongNameCyrillic(maxBoundaryTestInField),Аввропроролртоптиполрплрплрпрплрплрплрплрпрпрпппрплоооооооооооооо",
            "nameWithOneHyphenCyrillic,Анна-Мария Ремарк",
            "nameWithTwoHyphensCyrillic,Анна-Мария-Ремарк Иванова",
            "twoLetterNameCyrillic,Ян",
            "regularNameSurnameWithSpaceCyrillic,Иван Петров",
            "regularNameSecondNameSurnameWithSpaceCyrillic,Иван Николаевич Петров",
            "nameWithUpperCaseErrorsCyrillic,иВаН петров",
            "nameWithTwoHyphensInRowCyrillic,Анна--Мария",
            "nameWithHyphenAtTheBeginning,-Анна",
            "nameWithHyphenAtTheEnd,Анна-",
            "nameWithOnlySpacesAndHyphens,--  --"})
    void shouldValidateInputName(String testcase, String name) {
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+79991234567");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='order-success']")));
    }

    // 2) тестируем валидацию поля "Мобильный телефон",
    // в поле "Фамилия и имя" - всегда валидное значение, чек-бокс на согласие всегда нажат.
    // Для критериев валидации также использовались ТОЛЬКО требования, обозначенные в ДЗ.
    @ParameterizedTest
    @CsvSource(value = {"emptyPhoneNumber,''",
            "phoneNumberWithNonDigitSymbols,+7898989898q",
            "tooShortNumber,+7",
            "numberOfTenDigits,+7898989899",
            "numberOfTwelveDigits(tooLongNumber),+789898989999",
            "numberWithoutPlus,78989898998",
            "numberWithRestrictedSymbols,-78989898999",
            "elevenDigitsPlusInTheMiddle,789898+98998",
            "elevenDigitsPlusAtTheEnd,78989898998+"})
    void shouldNotValidateInputPhoneNumber(String testcase, String phone) {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys(phone);
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='phone'].input_invalid .input__sub")));
    }

    @ParameterizedTest
    @CsvSource(value = {"regularNumberElevenDigitsPlusAtFirst,+71234567891",
            "nonRegularNumberElevenDigitsAllNullsPlusAtFirst,+00000000000"})
    void shouldValidateInputPhoneNumber(String testcase, String phone) {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys(phone);
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='order-success']")));
    }

    // 3) тестируем валидацию чек-бокса "Согласие с условиями обработки персональных данных".
    // В полях "Фамилия и имя", "Мобильный телефон" - валидные значения.
    @Test
    void shouldValidateFormIfCheckBoxClicked() {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+75656565656");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='order-success']")));
    }

    @Test
    void shouldNotValidateFormIfCheckBoxNotClicked() {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+75656565656");
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='agreement'].input_invalid")));
    }
}
