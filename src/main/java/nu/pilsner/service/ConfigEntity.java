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

    // define headers we're interested and that we've seen before
    // TODO: Make into map from KEYWORD to list of allowed values,
    // then PRODUKTNAMN->{"Namn", "Produktnamn"} could be used to allow
    // alternatives.
    // We use this information to find RUBRIK, VARUNR and PRODUKTNAMN values on same row,
    // if we do, we store the column numbers of the headers so we know which column
    // contains what value for the data rows.
    public enum HEADERNAME {
        RUBRIK("Rubrik"),
        VARUNR("Varunr"),
        PRODUKTNAMN(new LinkedList<String>() {
            {
                add("Produktnamn");
                add("Namn");
            }
        }),
        ÅRGÅNG("Årgång"),
        SORTIMENT("Sortiment"),
        VOLYM("Volym"),
        PRIS("Pris"),
        LITERPRIS("Literpris"),
        ALKOHOLHALT("Alkoholhalt"),
        PRODUCENT("Producent"),
        LANSERINGSDATUM("Lanseringsdatum"),
        LAND("Land"),
        MODUL("Modul"),
        OMRÅDE("Område"),
        INKÖPT_ANTAL(new LinkedList<String>() {
            {
                add("Inköpt antal");
                add("Antal");
            }
        }),
        LEVERANTÖR("Leverantör"),
        ÖVRIGT("Övrigt"),
        REGION("Region"),
        PRESENTATION("Presentation"),
        SÄSONG("Säsong"),
        DEPÅ(new LinkedList<String>() {
            {
                add("Depå");
                add("TSLS: Depå");
            }
        }),
        FÄRG("Färg"),
        DOFT("Doft"),
        SMAK("Smak"),
        //ANTAL("Antal"),
        OMDÖME("Omdöme"),
        BETYG("Betyg"),
        ORDERNR(new LinkedList<String>() {
            {
                add("Order nr");
                add("Ord nr");
            }
        }),
        FÖRPACKNING("Förpackning"),
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
