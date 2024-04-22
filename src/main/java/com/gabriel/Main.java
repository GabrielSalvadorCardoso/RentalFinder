package com.gabriel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.Duration;

public class Main {
	public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/home/gabriel/dev/rentalfinder/src/main/java/com/gabriel/chromedriver"); // Fonte do driver: https://stackoverflow.com/questions/77614587/where-can-i-find-chromedriver-119
        // System.setProperty("webdriver.firefox.driver", "/home/gabriel/dev/rentalfinder/src/main/java/com/gabriel/geckodriver");

        WebDriver driver = new ChromeDriver();
        // #main-content > div.olx-d-flex.olx-jc-space-between > div > p
        
        final String MAIN_QUERY_URL = "https://www.olx.com.br/imoveis/aluguel/estado-rj/rio-de-janeiro-e-regiao/zona-sul?pe=1200&sd=2232&sd=2218&sd=2234&sd=2231&sd=2220&sd=2227&coe=600"; 
        driver.get(MAIN_QUERY_URL);
        
        final String PAGING_INF_ELEMENT_SELECTOR = "#main-content > div.olx-d-flex.olx-jc-space-between > div > p";
        WebElement pagingInfoElement = driver.findElement(By.cssSelector(PAGING_INF_ELEMENT_SELECTOR));
        String pagingInfoText = pagingInfoElement.getText();
        
        int resultsInPage = Main.getResultsInPage(pagingInfoText);        
        int totalResults = Main.getTotalResults(pagingInfoText);        
        System.out.println(resultsInPage);
        
        final String ANNOUNCEMENTS_IN_PAGE_CONTAINER_SELECTOR = "#main-content > div.sc-a8d048d5-2.prLrC";
        WebElement announcementsContainerDiv = driver.findElement(By.cssSelector(ANNOUNCEMENTS_IN_PAGE_CONTAINER_SELECTOR));
        List<WebElement> divsInContainer = announcementsContainerDiv.findElements(By.cssSelector("div.sc-a8d048d5-0"));
        System.out.println(divsInContainer.size());        
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0,300)");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1L));
        
        int announcementDivsIterated = 1;
        final String ANNOUNCEMENT_DIV_SELECTOR_TEMPLATE = "#main-content > div.sc-a8d048d5-2.prLrC > div:nth-child(%d) section";
    	int announcementDivsIdx = 1;
    	List<String> announcementLinks = new ArrayList<>();
    	while(announcementLinks.size()!=resultsInPage) {
    		js.executeScript("window.scrollBy(0,250)");
			String remainingAnnouncementDivSelector = String.format(ANNOUNCEMENT_DIV_SELECTOR_TEMPLATE, announcementDivsIdx);
    		
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(remainingAnnouncementDivSelector)));
				WebElement remaingAnnouncementSection = driver.findElement(By.cssSelector(remainingAnnouncementDivSelector));
            	WebElement remaingAnnouncementLink = remaingAnnouncementSection.findElement(By.cssSelector("a"));
            	
            	announcementLinks.add(remaingAnnouncementLink.getAttribute("href"));
            	announcementDivsIterated++;
            	System.out.println("("+ announcementDivsIdx + ") " + remaingAnnouncementLink.getAttribute("href") + " - " + announcementLinks.size());
			} catch(TimeoutException toe) {
				System.out.println("("+ announcementDivsIdx + ") INFO: ANÚNCIO");
			} finally {
				announcementDivsIdx++;
			}
    	}
    	System.out.println(announcementLinks.size());
    	WebElement btnNext = driver.findElement(By.cssSelector("#listing-pagination > aside > div > a:nth-child(4) > span"));
    	btnNext.click();
    	
//         driver.quit();
    }
	
	public static int getResultsInPage(String pagingInfoText) {
		String startRangeValue = pagingInfoText.split(" - ")[0].trim();
		String finalRangeValue = pagingInfoText.split(" - ")[1].split(" ")[0].trim();
		return Integer.parseInt(finalRangeValue) - Integer.parseInt(startRangeValue) + 1;
		
	}
	
	public static int getTotalResults(String pagingInfoText) {
		String totalResultsSnippet = pagingInfoText.split(" de ")[ pagingInfoText.split(" de ").length-1 ];
		String totalResultsText = totalResultsSnippet.split(" ")[0].trim();
		return Integer.parseInt(totalResultsText);
	}
	
	public static String convertToCSV(String[] data) {
        return Stream.of(data).map(Main::escapeSpecialCharacters).collect(Collectors.joining(","));
    }

    public static String escapeSpecialCharacters(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
