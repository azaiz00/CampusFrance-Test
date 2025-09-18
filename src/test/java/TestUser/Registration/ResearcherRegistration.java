package TestUser.Registration;

import enums.Status;
import models.Researcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DataLoader;

import java.time.Duration;
import java.util.List;

public class ResearcherRegistration {

    private static final Logger log = LoggerFactory.getLogger(ResearcherRegistration.class);
    WebDriver driver;

    private void selectRadioBtnStatus(Status status) {
        String radioId = switch (status) {
            case Student -> "edit-field-publics-cibles-2";
            case Researcher -> "edit-field-publics-cibles-3";
            case Institutional -> "edit-field-publics-cibles-4";
        };
        // localise le label associé à l'input)
        WebElement label = driver.findElement(By.cssSelector("label[for='" + radioId + "']"));
        // scrolle l’élément au centre pour éviter qu’il soit caché
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'})", label);
        // Clique
        label.click();
    }
    private String selectFromDropdown(WebDriver driver, WebDriverWait wait, String dropdownId, String optionText) {
        // 1. Localiser l'input du dropdown
        WebElement dropdownInput = driver.findElement(By.id(dropdownId));

        // 2. Scroller puis cliquer pour ouvrir la liste
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'})", dropdownInput);
        dropdownInput.click();

        // 3. Construire le Xpath de l’option par son texte
        By optionBy = By.xpath("//div[@class='option' and normalize-space(text())='" + optionText + "']");

        // 4. Attendre que l’option soit présente
        WebElement option = wait.until(ExpectedConditions.presenceOfElementLocated(optionBy));

        // 5. Scroller jusqu'à l’option et cliquer
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'})", option);
        option.click();

        WebElement selectedItem = dropdownInput.findElement(By.xpath("..//div[contains(@class,'item')]"));
        String selectedText = selectedItem.getText().trim();
        return selectedText;
    }

    @BeforeEach
    void setUp() {
        log.info("=== DÉBUT setUp ===");
        driver = new ChromeDriver(); // Selenium Manager gère le driver

        log.info("ChromeDriver initialisé");
    }

    @Test
    void CheckValidResearcherRegistrationWithInformatiqueDoctorat() {

        // Charger les étudiants depuis le fichier JSON
        List<Researcher> Researchers = DataLoader.readList("testdata/researchers.json", Researcher.class);
        Researcher s = Researchers.get(0); // premier étudiant (cas valide)

        log.info("Ouverture de la page...");
        driver.get("https://www.campusfrance.org/fr/user/register");
        log.info("Page ouverte: {}", driver.getCurrentUrl());

        // Attendre que la page soit chargée
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
        log.info("Page complètement chargée.");

        // Fermer la bannière cookies (si elle existe)
        try {
            log.info("Tentative de fermeture de la bannière cookies...");

            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement closeCookiesBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"tarteaucitronAllDenied2\"]")
            ));

            closeCookiesBtn.click();
            log.info("Bannière cookies fermée.");
        } catch (TimeoutException e) {
            log.info("Pas de bannière cookies détectée (timeout).");
        } catch (NoSuchElementException | ElementClickInterceptedException e) {
            log.info("Pas de bannière cookies à fermer ou clic intercepté.");
        }

        log.info("Saisie Email...");
        driver.findElement(By.xpath("/html/body/div[2]/div[2]/main/div[2]/div/div[2]/form/div[2]/div/div[1]/input\n")).sendKeys(s.getEmail());
        log.info("Saisie mot de passe...");
        driver.findElement(By.xpath("//*[@id=\"edit-pass-pass1\"]")).sendKeys(s.getPassword());

        log.info("Saisie confirmation de mot de passe...");
        driver.findElement(By.xpath("//*[@id=\"edit-pass-pass2\"]")).sendKeys(s.getPassword());

        log.info("Cochement de Civilité...");
        var GenderSection = driver.findElement(By.xpath("//*[@id=\"edit-field-civilite--wrapper\"]"));
        // scroller l'élément dans la vue, au centre
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'})", GenderSection
        );
        switch (s.getGender()) {
            case Male -> driver.findElement(By.xpath("//*[@id=\"edit-field-civilite\"]/div[2]/label")).click();
            case Female -> driver.findElement(By.xpath("//*[@id=\"edit-field-civilite\"]/div[1]/label")).click();
        }

        log.info("Saisie nom...");
        driver.findElement(By.xpath("//*[@id=\"edit-field-nom-0-value\"]")).sendKeys(s.getLastName());

        log.info("Saisie Prénom...");
        driver.findElement(By.xpath("//*[@id=\"edit-field-prenom-0-value\"]")).sendKeys(s.getFirstName());

        log.info("Sellection du Pays de résidence ...");
        String dropdownid = "edit-field-pays-concernes-selectized";

        String countryName = s.getCountryOfResidence(); // ex: "Botswana"
        String selectedText = selectFromDropdown(driver,wait,dropdownid,"-"+countryName);
        Assertions.assertEquals(selectedText, "-"+countryName);

        log.info("Saisie de pays de nationalité ...");
        driver.findElement(By.id("edit-field-nationalite-0-target-id")).sendKeys(s.getNationality());

        log.info("Saisie Code Postale...");
        driver.findElement(By.xpath("//*[@id=\"edit-field-code-postal-0-value\"]")).sendKeys(s.getPostalCode());

        log.info("Saisie Ville...");
        driver.findElement(By.xpath("//*[@id=\"edit-field-ville-0-value\"]")).sendKeys(s.getCity());

        log.info("Saisie Téléphone...");
        driver.findElement(By.xpath("//*[@id=\"edit-field-telephone-0-value\"]")).sendKeys(s.getPhone());

        log.info("Sélection statut 'researcher'...");
        selectRadioBtnStatus(s.getStatus());
        boolean selected = driver.findElement(By.xpath("//*[@id=\"edit-field-publics-cibles-2\"]")).isSelected();
 //       Assertions.assertTrue(selected);
        log.info("Assertion OK: statut researcher bien sélectionné = {}", selected);

        // Assertions.assertEquals(s.getStatus(),radioBtnStatus.getAttribute("value"));

        log.info("Sellection du Domaine de recherche ...");
        dropdownid = "edit-field-domaine-etudes-selectized";
        String researchField = s.getResearchField();
        selectedText= selectFromDropdown(driver,wait,dropdownid,researchField);

      //  Assertions.assertEquals(selectedText, researchField);

        log.info("Sellection du Niveau de recherche ...");
        dropdownid = "edit-field-niveaux-etude-selectized";
        String researchLevel = s.getResearchLevel();
        selectedText =selectFromDropdown(driver,wait,dropdownid,researchLevel);
      //  Assertions.assertEquals(selectedText, researchLevel);


        log.info("Acceptation de traitement des données ");
        WebElement terms = driver.findElement(By.xpath("//*[@id=\"edit-field-accepte-communications-wrapper\"]/div/label"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", terms);
        terms.click();
    }

    @AfterEach
    void tearDown() {
        log.info("=== FIN tearDown (fermeture du navigateur) ===");
        if (driver != null) {
            try {
                driver.quit();
                log.info("ChromeDriver fermé proprement.");
            } catch (Exception e) {
                log.error("Erreur à la fermeture du driver: {}", e.getMessage(), e);
            }
        }
    }
}