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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Test
    void shouldNotValidateEmptyName() {
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+79991234567");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("");
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='name'].input_invalid .input__sub")).getText().trim();
        String expected = "Поле обязательно для заполнения";
        assertEquals(expected, message);
    }

    @ParameterizedTest
    @CsvSource(value = {"oneLetterNameCyrillic(minBoundaryTestInField),а",
            "oneLetterNameSurnameWithSpaceCyrillic(minBoundaryTestInField2),а д",
            "extremelyLongNameCyrillic(maxBoundaryTestInField),Аввропроролртоптиполрплрплрпрпл Рплрплрплрпрпрпппрплоооооооооооооо",
            "oneLetterNameSurnameLatin,q a",
            "nameWithDigits,Павел Иванов0",
            "nameWithSpecialSymbols,Павел Иванов_@#$",
            "nameWithTwoHyphensInRowCyrillic,Анна--Мария Петрова",
            "nameWithHyphenAtTheBeginning,-Анна Петрова",
            "nameWithHyphenAtTheEnd,Анна- Петрова-",
            "nameWithOnlySpacesAndHyphensWithoutLetters,--  --",
            "nameSurnameWithUpperOrLowerCaseErrorsCyrillic,иВаН пЕтров"})
    void shouldNotValidateImproperInputName(String testcase, String name) {
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+79991234567");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='name'].input_invalid .input__sub")).getText().trim();
        String expected = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";
        assertEquals(expected, message);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "nameWithOneHyphenCyrillic,Анна-Мария Ремарк",
            "nameWithTwoHyphensCyrillic,Анна-Мария-Ремарк Иванова",
            "twoLetterNameAndSurnameCyrillic,Ян Ян",
            "regularNameSurnameWithSpaceCyrillic,Иван Петров",
            "regularNameSecondNameSurnameWithSpaceCyrillic,Иван Николаевич Петров",
            "nameSurnameAllUpperCaseCyrillic,ИВАН ПЕТРОВ",
            "nameSurnameAllLowerCaseCyrillic,иван петров"})
    void shouldValidateProperInputName(String testcase, String name) {
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+79991234567");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='order-success']")).getText().trim();
        String expected = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";
        assertEquals(expected, message);
    }

    // 2) тестируем валидацию поля "Мобильный телефон",
    // в поле "Фамилия и имя" - всегда валидное значение, чек-бокс на согласие всегда нажат.
    @Test
    void shouldNotValidateEmptyPhoneNumber() {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("");
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='phone'].input_invalid .input__sub")).getText().trim();
        String expected = "Поле обязательно для заполнения";
        assertEquals(expected, message);
    }

    @ParameterizedTest
    @CsvSource(value = {"phoneNumberElevenDigitsWithLetter,+78989898989q",
            "tooShortNumber,+7",
            "numberOfTenDigits,+7898989899",
            "numberOfTwelveDigits(tooLongNumber or MaxBoundaryTestInField),+789898989999",
            "numberElevenDigitsWithoutPlus,78989898998",
            "numberWithRestrictedSymbols,-78989898999",
            "numberWithRestrictedSymbols2,-78989898_@#$",
            "elevenDigitsPlusInTheMiddle,789898+98998",
            "elevenDigitsPlusAtTheEnd,78989898998+",
            "nonExistentNumberElevenDigitsAllNullsPlusAtFirst,+00000000000"})
    void shouldNotValidateImproperPhoneNumber(String testcase, String phone) {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys(phone);
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='phone'].input_invalid .input__sub")).getText().trim();
        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";
        assertEquals(expected, message);
    }

    @Test
    void shouldValidateProperPhoneNumber() {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='agreement']")).click();
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+71234567891");
        driver.findElement(By.cssSelector("[type='button']")).click();
        String message = driver.findElement(By.cssSelector("[data-test-id='order-success']")).getText().trim();
        String expected = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";
        assertEquals(expected, message);
    }

    // 3) тестируем валидацию нажатия чек-бокса "Согласие с условиями обработки персональных данных".
    // В полях "Фамилия и имя", "Мобильный телефон" - валидные значения.
    @Test
    void shouldNotValidateFormIfCheckBoxNotClicked() {
        driver.findElement(By.cssSelector("[data-test-id='name'] input")).sendKeys("Иван Иванов");
        driver.findElement(By.cssSelector("[data-test-id='phone'] input")).sendKeys("+75656565656");
        driver.findElement(By.cssSelector("[type='button']")).click();
        assertNotNull(driver.findElement(By.cssSelector("[data-test-id='agreement'].input_invalid")));
    }
}
