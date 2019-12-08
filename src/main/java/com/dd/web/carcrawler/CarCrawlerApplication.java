package com.dd.web.carcrawler;

import com.dd.web.carcrawler.controllers.CopyAllCarsSQLService;
import com.dd.web.carcrawler.controllers.SaveAllCarsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarCrawlerApplication {
    private static SaveAllCarsService saveAllCarsService;
    private static CopyAllCarsSQLService copyAllCarsSQLService;

    @Autowired
    public CarCrawlerApplication(SaveAllCarsService saveAllCarsService, CopyAllCarsSQLService copyAllCarsSQLService) {
        CarCrawlerApplication.saveAllCarsService = saveAllCarsService;
        CarCrawlerApplication.copyAllCarsSQLService = copyAllCarsSQLService;
    }


    public static void main(String[] args) {
        SpringApplication.run(CarCrawlerApplication.class, args);

        //test();
        writeSqlForAllCars();
    }

    public static void test() {
        saveAllCarsService.prepare();
        saveAllCarsService.test();
        saveAllCarsService.teardown();
    }

    public static void writeSqlForAllCars() {
        copyAllCarsSQLService.createSQLStatementsForAllCars();
    }

}
