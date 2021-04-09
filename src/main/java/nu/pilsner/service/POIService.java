/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.pilsner.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author flax
 */
@ApplicationScoped
public class POIService {

    @Inject
    ConfigEntity config;

    private Map<String, Integer> headerMap;

    public void parse(InputStream is, StringBuilder sb, Boolean isSlim, Boolean withDate) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(is);
            int activeSheets = workbook.getNumberOfSheets();
            sb.append("File contains " + activeSheets + " sheets\n\n");
            for (int currentSheet = 0; currentSheet < activeSheets; currentSheet++) {
                Sheet sheet = workbook.getSheetAt(currentSheet);
                String sheetName = sheet.getSheetName();
                int len = sheetName.length() + 7 + 2;
                StringBuilder del = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    del.append("-");
                }
                String delimiter = "+" + del.toString() + "+";
                sb.append("\n").append(delimiter).append("\n");
                sb.append("| Sheet: " + sheet.getSheetName() + " |\n");
                sb.append(delimiter).append("\n\n");
                fetchSheetData(sheet, sb, isSlim, withDate); // Find headers and data
            }
        } catch (IOException ex) {
        }
    }

    public void fetchSheetData(Sheet sheet, StringBuilder sb, Boolean isSlim, Boolean withDate) {
        // Iterate over rows until headers found, the start extraction....
        Boolean headersFound = false;
        Iterator<Row> rowIterator = sheet.rowIterator();
        Integer emptyRows = 0;
        Integer currentRowIndex = 0;
        while (rowIterator.hasNext() && currentRowIndex < 400) {
            //System.out.println("Row  " + currentRowIndex);
            Row currentRow = rowIterator.next();
            Boolean emptyRow = isEmptyRow(currentRow);
            // Print some information about row
            //System.out.println("Empty row: " + emptyRow);
            if (emptyRow) {
                emptyRows++;
            } else {
                emptyRows = 0;
            }
            if (emptyRows > config.maxEmptyRows) {
                // Read enough empty rows, terminate
                return;
            }
            if (!emptyRow) {
                if (currentRow != null) {
                    if (!headersFound) {
                        try {
                            headersFound = analyzeHeaders(currentRow);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        // Process row....
                        processDataRow(currentRow, isSlim, sb, withDate);
                    }
                }
            }
            currentRowIndex++;
        }

    }

    public void processDataRow(Row theRow, Boolean isSlim, StringBuilder sb, Boolean withDate) {
        Iterator cellIter = theRow.cellIterator();
        Boolean first = true;
        Map<String, String> header2valueMap = new HashMap<>();
        while (cellIter.hasNext()) {
            Cell theCell = (Cell) cellIter.next();
            Integer cellIndex = theCell.getColumnIndex();
            String header = config.getHeader(cellIndex);
            String value = getCellValueAsString(theCell, header);
            header2valueMap.put(header, value);
        }
        processValues(header2valueMap, isSlim, sb, withDate);
    }

    public String fuzzyValueOf(String header) {
        if (header == null) {
            return "";
        }
        // TODO: create map from header to type...
        String nh = header.toUpperCase();
        if (nh.startsWith("VARUNR")) {
            return "VARUNR";
        }
        if (nh.startsWith("RUBRIK")) {
            return "RUBRIK";
        }
        if (nh.startsWith("PRODUKTNAMN")) {
            return "PRODUKTNAMN";
        }
        if (nh.startsWith("NAMN")) {
            return "PRODUKTNAMN";
        }
        if (nh.startsWith("ÅRGÅNG")) {
            return "ÅRGÅNG";
        }
        if (nh.startsWith("SORTIMENT")) {
            return "SORTIMENT";
        }
        if (nh.startsWith("VOLYM")) {
            return "VOLYM";
        }
        if (nh.startsWith("PRIS")) {
            return "PRIS";
        }
        if (nh.startsWith("LITERPRIS")) {
            return "LITERPRIS";
        }
        if (nh.startsWith("ALKOHOLHALT")) {
            return "ALKOHOLHALT";
        }
        if (nh.startsWith("PRODUCENT")) {
            return "PRODUCENT";
        }
        if (nh.startsWith("LANSERINGSDATUM")) {
            return "LANSERINGSDATUM";
        }
        if (nh.startsWith("LAND")) {
            return "LAND";
        }
        if (nh.startsWith("OMRÅDE")) {
            return "OMRÅDE";
        }
        if (nh.startsWith("PRESENTATION")) {
            return "PRESENTATION";
        }
        if (nh.startsWith("INKÖPT ANTAL")) {
            return "ANTAL";
        }
        if (nh.startsWith("LEVERANTÖR")) {
            return "LEVERANTÖR";
        }
        if (nh.startsWith("ÖVRIGT")) {
            return "ÖVRIGT";
        }
        return nh;
    }

    public void processValues(Map<String, String> h2vMap, Boolean isSlim, StringBuilder sb, Boolean withDate) {
        // Build record and append it to sb
        String tmp;
        sb.append("<strong>");
        sb.append(h2vMap.get("VARUNR")).append(" ");
        sb.append(h2vMap.get("PRODUKTNAMN")).append("</strong>").append("\n");
        if (withDate) {
            sb.append("Lanseringsdatum: ").append(h2vMap.get("LANSERINGSDATUM")).append("\n");
        }
        sb.append("Rubrik: ").append(h2vMap.get("RUBRIK")).append("\n");
        sb.append("Volym: ").append(h2vMap.get("VOLYM")).append(" ml ");
        sb.append("Pris: ").append(h2vMap.get("PRIS")).append(" Sek Literpris: ").append(h2vMap.get("LITERPRIS")).append(" Sek/l ");
        sb.append("Alkoholhalt: ").append(h2vMap.get("ALKOHOLHALT")).append("%\n");
        sb.append("Producent: ").append(h2vMap.get("PRODUCENT")).append(" Sortiment: ").append(h2vMap.get("SORTIMENT")).append("\n");
        sb.append("Land: ").append(h2vMap.get("LAND"));
        tmp = h2vMap.get("OMRÅDE");
        if (tmp != null && !tmp.isBlank()) {
            sb.append(" Område: ").append(tmp);
        }
        String antal = h2vMap.get("ANTAL");
        if (antal != null && !antal.isBlank()) {
            sb.append(" Inköpt antal: ").append(h2vMap.get("ANTAL"));
        }
        sb.append(" Leverantör: ").append(h2vMap.get("LEVERANTÖR")).append("\n");
        if (!isSlim) {
            sb.append("Färg: \n");
            sb.append("Doft: \n");
            sb.append("Smak: \n");
            sb.append("Omdöme: \n");
            sb.append("Betyg (1-6); \n");
        }
        sb.append("\n");
    }

    public boolean isEmptyRow(Row thisRow) {
        // We need at least 10 values in row to consider it...
        if (thisRow.getPhysicalNumberOfCells() < 10) {
            return true;
        }
        // Test first 10 cells, if more than 8 are empty, consider row empty
        int emptyCells = 0;
        for (int currentCellNumber = 0; currentCellNumber < 10; currentCellNumber++) {
            Cell theCell = thisRow.getCell(currentCellNumber);
            CellType cellType = theCell.getCellType();
            switch (cellType) {
                case BLANK:
                    emptyCells++;
                    break; // Start next cell, do nothing
                case BOOLEAN:
                case ERROR:
                case FORMULA:
                case NUMERIC:
                case STRING:
                    break;
            }
            if (emptyCells > 8) {
                return true;
            }
        }
        return false;
    }

    public Boolean analyzeHeaders(Row currentRow) {
        Map<String, Integer> res = new HashMap<>();
        // Require at least Rubrik, Varunr and Namn in row to consider it a header row.
        if (detectHeader(currentRow, config.getDefinedHeaderValue(0), config.getDefinedHeaderValue(1), config.getDefinedHeaderValue(2))) {
            // Iterate over row, storing index of column matching header list in map
            // Iterate and collect headers
            Iterator cellIter = currentRow.cellIterator();
            while (cellIter.hasNext()) {
                Cell currentCell = (Cell) cellIter.next();
                Integer index = currentCell.getColumnIndex();
                try {
                    String cellValue = currentCell.getStringCellValue();
                    config.addHeader(index, fuzzyValueOf(cellValue));
                } catch (Exception ex) {
                    System.out.println("Could not read header value as string for cell ");
                }
            }
            return true;
        }
        return false;
    }

    public String getCellValueAsString(Cell theCell, String header) {
        // Header provides info on value...
        CellType cellType = theCell.getCellType();
        switch (cellType) {
            case BLANK:
                return "";
            case BOOLEAN:
                return Boolean.toString(theCell.getBooleanCellValue());
            case ERROR:
                return "#ERROR";
            case FORMULA:
                return theCell.getCellFormula();
            case NUMERIC:
                if (header != null && header.contains("DATUM")) {
                    return formatDateToString(theCell.getDateCellValue());
                }
                switch (header) {
                    case "VARUNR":
                    case "VOLYM":
                    case "ANTAL":
                        // Return an integer value
                        Double d = theCell.getNumericCellValue();
                        return String.valueOf(d.intValue());
                }
                return Double.toString(theCell.getNumericCellValue());
            case STRING:
                return theCell.getStringCellValue();
            default:
                return "?";
        }
    }

    public String formatDateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public Boolean detectHeader(Row row, String header1, String header2, String header3) {
        Boolean ok1 = false;
        Boolean ok2 = false;
        Boolean ok3 = false;

        if (header1 == null || header2 == null || header3 == null) {
            System.out.println("Got null strings for headers");
            return false;
        }

        Iterator iter = row.cellIterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            CellType cellType = cell.getCellType();
            if (cellType.equals(CellType.STRING)) {
                String cellContent = cell.getStringCellValue();
                if (header1.equals(cellContent)) {
                    ok1 = true;
                }
                if (header2.equals(cellContent)) {
                    ok2 = true;
                }
                if (header3.equals(cellContent)) {
                    ok3 = true;
                }
            }
        }
        return ok1 && ok2 && ok3;

    }

}
