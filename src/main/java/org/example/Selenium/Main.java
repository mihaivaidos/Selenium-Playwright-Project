package org.example.Selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        List<Runner> runners = extractFromWebSite();
        writeResultInFile(runners);
        searchBib(runners);

    }

    /**
     * Creates a ChromeDriver and extracts all the runners from the 10km non-competitive race and creates a list of runners
     *
     * @return the list of extracted runners
     */
    public static List<Runner> extractFromWebSite() {

        List<Runner> runners = new ArrayList<>();

        // System.setProperty("webdriver.chrome.driver", "C:\\Users\\Mihai\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe\"");

        WebDriver driver = new ChromeDriver();
        driver.get("https://www.endu.net/en/events/bergamo-city-run/results/2025");

        //WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement raceSelectButton = driver.findElement(By.xpath("//select[@ng-model='activeRace']"));
        Select select  = new Select(raceSelectButton);
        select.selectByValue("number:52275"); //select.selectByVisibleText("10 Km non competitiva");

        try {
            WebElement closeButton = driver.findElement(By.xpath("//button[contains(text(),'Accetta')]"));
            closeButton.click();
        } catch (Exception e) {
            System.out.println("No cookie banner found, continuing...");
        }

        while(true) {
            try {

                WebElement activePage = driver.findElement(By.xpath("//li[@class='page-number active']/a"));
                int currentPage = Integer.parseInt(activePage.getText().trim());
                System.out.println("Scraping page: " + currentPage);

                //wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fixed-table-body")));

                WebElement resultsTable = driver.findElement(By.className("fixed-table-body"));
                List<WebElement> rows = resultsTable.findElements(By.xpath(".//tbody/tr"));

                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td")); // Get all columns in row

                    int bib = Integer.parseInt(cells.get(0).getText().trim());

                    String runnerNameExtracted = cells.get(1).getText().trim();
                    String runnerName = runnerNameExtracted.split("\n")[0].trim();
                    String runnerCountry = runnerNameExtracted.split("\n")[1].trim();

                    String finisherTimeExtracted = cells.get(2).getText().trim();
                    String finisherTime = finisherTimeExtracted.split("\n")[0].trim();

                    runners.add(new Runner(bib, runnerName, runnerCountry, finisherTime));

                }

                List<WebElement> allPages = driver.findElements(By.xpath("//ul[@class='pagination']//li/a"));
                int lastPage = Integer.parseInt(allPages.get(allPages.size() - 2).getText().trim()); // Second to last item is last page

                if (currentPage >= lastPage) {
                    System.out.println("Reached the last page.");
                    break;
                }

                // Locate the ">" button
                WebElement nextButton = driver.findElement(By.xpath("//*[@id='contenitore']/div[1]/div[1]/div[1]/div[13]/div[1]/div[2]/div[4]/div[2]/ul/li[9]/a"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextButton);

            } catch (Exception e) {
                System.out.println("An error occurred.");
                break;
            }
        }

        driver.quit();

        return runners;
    }

    /**
     * Writes the sorted results of the 10Km non-competitive race in the txt file
     *
     * @param runners the list of runners extracted
     */
    public static void writeResultInFile(List<Runner> runners) {
        List<Runner> sortedRunners = runners.stream().sorted(Comparator.comparing(Runner::getFinisherTime)).toList();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/java/org/example/Selenium/results.txt"))) {
            int place = 1;
            bw.write("place - name - finish time - bib - country");
            bw.newLine();
            for (Runner runner : sortedRunners) {
                bw.write(place++ + ". " + runner.getRunnerName() + " - " + runner.getFinisherTime() + " - " + runner.getBib() + " - " + runner.getCountry());
                bw.newLine();
            }
            System.out.println("\nRace results saved to 'results.txt'.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void searchBib(List<Runner> runners) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your bib number: ");
        int bib = Integer.parseInt(scanner.nextLine());
        for (Runner runner : runners) {
            if (runner.getBib() == bib) {
                System.out.println(runner.getRunnerName() + " - " + runner.getFinisherTime() + " - " + runner.getBib() + " - " + runner.getCountry());
                break;
            }
        }
    }

}