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
import jakarta.enterprise.context.Dependent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author flax
 */
@Dependent
public class ConfigEntity {

    @ConfigProperty(name = "excel.maxemptyrows", defaultValue = "6")
    public Integer maxEmptyRows;

    // define headers we're interested and that we've seen before
    // TODO: Make into map from KEYWORD to list of allowed values,
    // then PRODUCT_NAME->{"Namn", "Produktnamn"} could be used to allow
    // alternatives.
    // We use this information to find HEADLINE, ITEMID and PRODUCT_NAME values on same row,
    // if we do, we store the column numbers of the headers so we know which column
    // contains what value for the data rows.
    public enum HEADERNAME {
        HEADLINE("Rubrik"),
        ITEMID("Varunr"),
        PRODUCT_NAME(new LinkedList<String>() {
            {
                add("Produktnamn");
                add("Namn");
            }
        }),
        YEAR("Årgång"),
        SEGMENT("Sortiment"),
        VOLUME("Volym"),
        PRICE("Pris"),
        PRICE_PER_LITER("Literpris"),
        ALCOHOL_PERCENTAGE("Alkoholhalt"),
        PRODUCER("Producent"),
        RELEASE_DATE("Lanseringsdatum"),
        LAND("Land"),
        MODULE("Modul"),
        AREA("Område"),
        ITEMS_BOUGHT(new LinkedList<String>() {
            {
                add("Inköpt antal");
                add("Antal");
            }
        }),
        DISTRIBUTOR("Leverantör"),
        OTHER("Övrigt"),
        REGION("Region"),
        PRESENTATION("Presentation"),
        SEASON("Säsong"),
        DEPOT(new LinkedList<String>() {
            {
                add("Depå");
                add("TSLS: Depå");
            }
        }),
        COLOR("Färg"),
        SMELL("Doft"),
        TASTE("Smak"),
        //ANTAL("Antal"),
        STATEMENT("Omdöme"),
        GRADE("Betyg"),
        ORDERNR(new LinkedList<String>() {
            {
                add("Order nr");
                add("Ord nr");
            }
        }),
        PACKAGING("Förpackning"),
        UNKNOWN("unknown");

        private List<String> columnName = new LinkedList<String>();

        HEADERNAME(String name) {
            this.columnName.add(name);
        }

        HEADERNAME(List<String> names) {
            for (String name : names) {
                columnName.add(name);
            }
        }

        static public List<String> getHeaders() {
            List<String> retval = new LinkedList<>();
            for (HEADERNAME hname : HEADERNAME.values()) {
                for (String name : hname.columnName) {
                    retval.add(name);
                }
            }
            return retval;
        }

        static public HEADERNAME getValue(String name) {
            for (HEADERNAME hname : HEADERNAME.values()) {
                for (String cname : hname.columnName) {
                    // Look for "starts with" in column name
                    if (name.startsWith(cname)) {
                        return hname;
                    }
                }
            }
            return UNKNOWN;
        }

        public Boolean checkValue(String name) {
            return columnName.contains(name);
        }

        public String getPrimaryValue() {
            return columnName.get(0);
        }

    };

}
