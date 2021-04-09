/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.pilsner.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author flax
 */
@Dependent
public class ConfigEntity {

    @ConfigProperty(name = "excel.maxemptyrows", defaultValue = "6")
    public Integer maxEmptyRows;

    public Map<Integer, String> headers = new HashMap<>();

    public void clearHeaders() {
        headers.clear();
    }

    public void addHeader(Integer cellIndex, String headerName) {
        headers.put(cellIndex, headerName);
    }

    public String getHeader(Integer cellIndex) {
        return headers.get(cellIndex);
    }

    // define headers
    private List<String> headerValues = new LinkedList<String>() {
        {
            add("Rubrik");
            add("Varunr");
            add("Produktnamn"); // Same as "Namn".
            add("Namn");
            add("Årgång");
            add("Sortiment");
            add("Volym");
            add("Pris");
            add("Literpris");
            add("Alkoholhalt");
            add("Producent");
            add("Lanseringsdatum");
            add("Land");
            add("Modul");
            add("Område");
            add("Inköpt antal");
            add("Leverantör");
            add("Övrigt");
            add("Depå");
            add("Färg");
            add("Doft");
            add("Smak");
            add("Omdöme");
            add("Betyg");
            add("Ord Nr");
        }
    };

    public String getDefinedHeaderValue(Integer index) {
        return headerValues.get(index);
    }
}
