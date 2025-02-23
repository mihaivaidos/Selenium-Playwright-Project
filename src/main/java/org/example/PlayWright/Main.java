package org.example.PlayWright;

import com.microsoft.playwright.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Runner> runners = extractFromWebSite();
        writeResultInFile(runners);
        searchBib(runners);
    }

    public static List<Runner> extractFromWebSite() {
        List<Runner> runners = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://www.endu.net/en/events/bergamo-city-run/results/2025");

            // Handle cookies if present
            try {
                Locator acceptButton = page.locator("//button[contains(text(),'Accetta')]");
                if (acceptButton.count() > 0) {
                    acceptButton.click();
                }
            } catch (Exception ignored) {
                System.out.println("No cookie banner found, continuing...");
            }

            // Select the 10km non-competitive race
            Locator raceSelect = page.locator("//select[@ng-model='activeRace']");
            raceSelect.selectOption("number:52275");

            while (true) {
                try {
                    String currentPageText = page.locator("//li[@class='page-number active']/a").innerText().trim();
                    int currentPage = Integer.parseInt(currentPageText);
                    System.out.println("Scraping page: " + currentPage);

                    page.waitForSelector(".fixed-table-body tbody tr"); // Wait until the rows are visible

                    Locator rows = page.locator(".fixed-table-body tbody tr");

                    for (int i = 0; i < rows.count(); i++) {
                        Locator row = rows.nth(i);
                        List<String> cells = row.locator("td").allInnerTexts();

                        int bib = Integer.parseInt(cells.get(0).trim());
                        String[] runnerDetails = cells.get(1).split("\n");
                        String runnerName = runnerDetails[0].trim();
                        String runnerCountry = runnerDetails.length > 1 ? runnerDetails[1].trim() : "";
                        String finisherTime = cells.get(2).split("\n")[0].trim();

                        runners.add(new Runner(bib, runnerName, runnerCountry, finisherTime));
                    }

                    // Determine the last page
                    List<String> pageNumbers = page.locator("//ul[@class='pagination']//li/a").allInnerTexts();
                    int lastPage = Integer.parseInt(pageNumbers.get(pageNumbers.size() - 2).trim());

                    if (currentPage >= lastPage) {
                        System.out.println("Reached the last page.");
                        break;
                    }

                    // Click the next button
                    Locator nextButton = page.locator("//*[@id='contenitore']/div[1]/div[1]/div[1]/div[13]/div[1]/div[2]/div[4]/div[2]/ul/li[9]/a");
                    nextButton.scrollIntoViewIfNeeded();
                    nextButton.click();

                    // Wait for new page data to load
                    page.waitForTimeout(1000);

                } catch (Exception e) {
                    System.out.println("An error occurred.");
                    break;
                }
            }

            browser.close();
        }

        return runners;
    }

    public static void writeResultInFile(List<Runner> runners) {
        List<Runner> sortedRunners = runners.stream().sorted(Comparator.comparing(Runner::getFinisherTime)).toList();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/main/java/org/example/PlayWright/resultsPW.txt"))) {
            int place = 1;
            bw.write("place - name - finish time - bib - country");
            bw.newLine();
            for (Runner runner : sortedRunners) {
                bw.write(place++ + ". " + runner.getRunnerName() + " - " + runner.getFinisherTime() + " - " + runner.getBib() + " - " + runner.getCountry());
                bw.newLine();
            }
            System.out.println("\nRace results saved to 'resultsPW.txt'.");
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
