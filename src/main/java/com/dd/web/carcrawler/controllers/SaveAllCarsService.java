package com.dd.web.carcrawler.controllers;

import com.dd.web.carcrawler.entities.Details;
import com.dd.web.carcrawler.entities.Manufacturer;
import com.dd.web.carcrawler.entities.Model;
import com.dd.web.carcrawler.entities.TypeYear;
import com.dd.web.carcrawler.repositories.TypeYearRepository;
import com.dd.web.carcrawler.repositories.ManufacturerRepository;
import com.dd.web.carcrawler.repositories.ModelRepository;
import com.dd.web.carcrawler.repositories.DetailsRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class SaveAllCarsService {

    private String testUrl;
    private WebDriver driver;

    private DetailsRepository detailsRepository;
    private TypeYearRepository typeYearRepository;
    private ModelRepository modelRepository;
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    public SaveAllCarsService(DetailsRepository detailsRepository, TypeYearRepository typeYearRepository, ModelRepository modelRepository, ManufacturerRepository manufacturerRepository) {
        this.detailsRepository = detailsRepository;
        this.typeYearRepository = typeYearRepository;
        this.modelRepository = modelRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    public void prepare() {
        System.setProperty(
                "webdriver.chrome.driver",
                "webdriver/chromedriver.exe");

        testUrl = "https://www.autokarma.ro/piese-auto";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        driver.get(testUrl);
    }

    public void test() {
        List<Manufacturer> manufacturerList = getAllManufacturers();
        manufacturerList.forEach(manufacturer -> {
            manufacturer.setModels(getAllModelsForManufacturer(manufacturer));
            manufacturer.getModels().forEach(model -> {
                model.setTypeYears(getAllTypeYearForModel(model));
                model.getTypeYears().forEach(typeYear -> {
                    typeYear.setDetails(getAllEngineDetailsForType(typeYear));
                });
            });
            System.out.println(manufacturer);
        });
    }

    public void teardown() {
        driver.quit();
    }

    private List<Manufacturer> getAllManufacturers() {
        List<WebElement> links = driver.findElements(By.tagName("a")).stream().filter(link -> {
            try {
                return link.getText().toUpperCase().equals(link.getText()) && link.getAttribute("href").contains("https://www.autokarma.ro/piese-auto-") && !StringUtils.isEmpty(link.getText());
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
        return links.stream()
                .map(link -> manufacturerRepository.save(Manufacturer
                        .builder()
                        .name(link.getText())
                        .url(link.getAttribute("href"))
                        .imageUrl(link.findElement(By.className("img-responsive")).getAttribute("src"))
                        .build()))
                .collect(Collectors.toList());
    }

    private List<Model> getAllModelsForManufacturer(Manufacturer manufacturer) {
        driver.get(manufacturer.getUrl());
        List<WebElement> links = driver.findElements(By.tagName("a")).stream().filter(link -> {
            try {
                return link.getAttribute("href").contains("?grupa_modele=") && !StringUtils.isEmpty(link.getText());
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());
        return links.stream()
                .map(link -> modelRepository.save(Model
                        .builder()
                        .manufacturer(manufacturer)
                        .name(link.getText())
                        .url(link.getAttribute("href"))
                        .imageUrl(link.findElement(By.className("img-responsive")).getAttribute("src"))
                        .build()))
                .collect(Collectors.toList());
    }

    private List<TypeYear> getAllTypeYearForModel(Model model) {
        driver.get(model.getUrl());
        Set<WebElement> links = driver.findElements(By.tagName("a")).stream().filter(link -> {
            try {
                return link.getAttribute("href").contains(model.getManufacturer().getUrl()) && !StringUtils.isEmpty(link.getText()) && !link.getText().contains("Inapoi") && !link.getAttribute("href").equalsIgnoreCase(model.getUrl());
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toSet());
        links.forEach(link -> System.out.println(link.getText() + " " + link.getAttribute("href")));
        return links.stream()
                .map(link -> typeYearRepository.save(TypeYear
                        .builder()
                        .model(model)
                        .name(link.getText())
                        .url(link.getAttribute("href"))
                        .imageUrl(link.findElement(By.className("img-responsive")).getAttribute("src"))
                        .build()))
                .collect(Collectors.toList());
    }

    private List<Details> getAllEngineDetailsForType(TypeYear typeYear) {
        driver.get(typeYear.getUrl());
        List<Details> detailsList = new ArrayList<>();
        driver.findElements(By.tagName("h3")).stream()
                .filter(link -> {
                    try {
                        return link.getText().contains("Tip motor");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(link -> {
                    String fuel = link.getText().replace("Tip motor : ", "");
                    link.findElement(By.xpath("following-sibling::*")).findElement(By.tagName("tbody")).findElements(By.tagName("tr")).forEach(tr -> {
                        List<WebElement> tds = tr.findElements(By.tagName("td"));
                        detailsList.add(detailsRepository.save(Details.builder()
                                .typeYear(typeYear)
                                .fuel(fuel)
                                .type(tds.get(0).getText())
                                .fromDate(tds.get(1).getText())
                                .toDate(tds.get(2).getText())
                                .body(tds.get(3).getText())
                                .capacity(tds.get(4).getText())
                                .power(tds.get(5).getText())
                                .engineCode(tds.get(6).getText())
                                .build()));
                    });

                });
        return detailsList;
    }

}
