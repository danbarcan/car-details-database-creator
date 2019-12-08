package com.dd.web.carcrawler.controllers;

import com.dd.web.carcrawler.entities.Details;
import com.dd.web.carcrawler.entities.Manufacturer;
import com.dd.web.carcrawler.entities.Model;
import com.dd.web.carcrawler.entities.TypeYear;
import com.dd.web.carcrawler.repositories.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class CopyAllCarsSQLService {
    private final String INSERT_QUERY = "insert into %s (%s) values (%s);\n";
    private final String COLUMNS_USUAL = "id, image_url, name, url";
    private final String COLUMNS_DETAILS = "id, body, capacity, engine_code, from_date, fuel, power, to_date, type, type_year_id";
    private final String VALUES_USUAL = "%s, '%s', '%s', '%s'";
    private final String VALUES_DETAILS = "%s, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'";

    private ManufacturerRepository manufacturerRepository;

    @Autowired
    public CopyAllCarsSQLService(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    public void createSQLStatementsForAllCars() {
        StringBuilder sb = new StringBuilder();
        getAllManufacturersFromDB().forEach(manufacturer -> {
            sb.append(createInsertStatement(manufacturer));
            manufacturer.getModels().forEach(model -> {
                sb.append(createInsertStatement(model));
                model.getTypeYears().forEach(typeYear -> {
                    sb.append(createInsertStatement(typeYear));
                    typeYear.getDetails().forEach(details -> sb.append(createInsertStatement(details)));
                });
            });
        });

        try {
            Files.write(Paths.get("cars.sql"), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Manufacturer> getAllManufacturersFromDB() {
        return manufacturerRepository.findAll();
    }

    private String createInsertStatement(Object object) {
        if (object instanceof Manufacturer) {
            Manufacturer manufacturer = (Manufacturer) object;
            System.out.println("Added manufacturer: " + manufacturer.getName());
            return String.format(INSERT_QUERY, "manufacturers", COLUMNS_USUAL, String.format(VALUES_USUAL, manufacturer.getId(), replaceOrEscapeUnwantedCharacters(manufacturer.getImageUrl()), replaceOrEscapeUnwantedCharacters(manufacturer.getName()), replaceOrEscapeUnwantedCharacters(manufacturer.getUrl())));
        } else if (object instanceof Model) {
            Model model = (Model) object;
            System.out.println("Added model: " + model.getName());
            return String.format(INSERT_QUERY, "models", COLUMNS_USUAL + ", manufacturer_id", String.format(VALUES_USUAL + ", %s", model.getId(), replaceOrEscapeUnwantedCharacters(model.getImageUrl()), replaceOrEscapeUnwantedCharacters(model.getName()), replaceOrEscapeUnwantedCharacters(model.getUrl()), model.getManufacturer().getId()));
        } else if (object instanceof TypeYear) {
            TypeYear typeYear = (TypeYear) object;
            System.out.println("Added typeYear: " + typeYear.getName());
            return String.format(INSERT_QUERY, "type_year", COLUMNS_USUAL + ", model_id", String.format(VALUES_USUAL + ", %s", typeYear.getId(), replaceOrEscapeUnwantedCharacters(typeYear.getImageUrl()), replaceOrEscapeUnwantedCharacters(typeYear.getName()), replaceOrEscapeUnwantedCharacters(typeYear.getUrl()), typeYear.getModel().getId()));
        } else {
            Details details = (Details) object;
            System.out.println("Added details: " + details.getType());
            return String.format(INSERT_QUERY, "details", COLUMNS_DETAILS, String.format(VALUES_DETAILS, details.getId(), replaceOrEscapeUnwantedCharacters(details.getBody()), replaceOrEscapeUnwantedCharacters(details.getCapacity()), replaceOrEscapeUnwantedCharacters(details.getEngineCode()), replaceOrEscapeUnwantedCharacters(details.getFromDate()), replaceOrEscapeUnwantedCharacters(details.getFuel()), replaceOrEscapeUnwantedCharacters(details.getPower()), replaceOrEscapeUnwantedCharacters(details.getToDate()), replaceOrEscapeUnwantedCharacters(details.getType()), details.getTypeYear().getId()));
        }
    }

    private String replaceOrEscapeUnwantedCharacters(String string) {
        return string.replace("\n", "").replace("'", "''");
    }
}
