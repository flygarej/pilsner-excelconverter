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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import nu.pilsner.service.ConfigEntity.HEADERNAME;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 *
 * @author flax
 */
@ApplicationScoped
public class POIService {

    public static final Logger LOG = Logger.getLogger(POIService.class.getSimpleName());

    @Inject
    ConfigEntity config;

    private Map<String, Integer> headerMap;

    public void parse(InputStream is, StringBuilder sb, Boolean isSlim, Boolean withDate) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(is);
            int activeSheets = workbook.getNumberOfSheets();
            sb.append("File contains " + activeSheets + " sheets\n\n");
            LOG.info("File contains " + activeSheets + " sheets");
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
        // Maps to hold header metadata while processing sheet
        Map<Integer, HEADERNAME> forwardMap = new HashMap<>();
        Map<HEADERNAME, Integer> reverseMap = new HashMap<>();
        while (rowIterator.hasNext() && currentRowIndex < 400) {

            Row currentRow = rowIterator.next();
            LOG.debug("Current row: " + currentRow.getRowNum());
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
                LOG.info("Ran out of rows while searching for data...");
                return;
            }
            if (!emptyRow) {
                if (currentRow != null) {
                    if (!headersFound) {
                        try {
                            headersFound = analyzeHeaders(currentRow, forwardMap, reverseMap);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        // Process row....
                        if (headersFound) {
                            processDataRow(currentRow, isSlim, sb, withDate, forwardMap, reverseMap);
                        } else {
                            LOG.info("Found data row while searching for header row, ignoring!");
                        }
                    }
                }
            }
            currentRowIndex++;
        }
        LOG.info("Headers " + ((headersFound) ? "" : "not") + "found");
    }

    public void processDataRow(Row theRow, Boolean isSlim, StringBuilder sb, Boolean withDate, Map<Integer, HEADERNAME> forwardMap, Map<HEADERNAME, Integer> reverseMap) {
        Iterator cellIter = theRow.cellIterator();
        Boolean first = true;
        Map<HEADERNAME, String> header2valueMap = new HashMap<>();
        while (cellIter.hasNext()) {
            Cell theCell = (Cell) cellIter.next();
            Integer cellIndex = theCell.getColumnIndex();
            HEADERNAME header = forwardMap.get(cellIndex);
            if (header != null) {
                String value = getCellValueAsString(theCell, header);
                header2valueMap.put(header, value);
            } else {
                LOG.info("Got null header for row " + theRow.getRowNum() + " and column " + theCell.getColumnIndex() );
            }
        }
        processValues(header2valueMap, isSlim, sb, withDate, forwardMap, reverseMap);
    }

    public void processValues(Map<HEADERNAME, String> h2vMap, Boolean isSlim, StringBuilder sb, Boolean withDate, Map<Integer, HEADERNAME> forwardMap, Map<HEADERNAME, Integer> reverseMap) {
        // Build record and append it to sb
        String tmp;
        sb.append("<strong>");
        sb.append(h2vMap.get(HEADERNAME.VARUNR)).append(" ");
        sb.append(h2vMap.get(HEADERNAME.PRODUKTNAMN)).append("</strong>").append("\n");
        if (withDate) {
            sb.append("Lanseringsdatum: ").append(h2vMap.get(HEADERNAME.LANSERINGSDATUM)).append("\n");
        }
        sb.append("Rubrik: ").append(h2vMap.get(HEADERNAME.RUBRIK)).append("\n");
        sb.append("Volym: ").append(h2vMap.get(HEADERNAME.VOLYM)).append(" ml ");
        sb.append("Pris: ").append(h2vMap.get(HEADERNAME.PRIS)).append(" Sek Literpris: ").append(h2vMap.get(HEADERNAME.LITERPRIS)).append(" Sek/l ");
        sb.append("Alkoholhalt: ").append(h2vMap.get(HEADERNAME.ALKOHOLHALT)).append("%\n");
        sb.append("Producent: ").append(h2vMap.get(HEADERNAME.PRODUCENT)).append(" Sortiment: ").append(h2vMap.get(HEADERNAME.SORTIMENT)).append("\n");
        sb.append("Land: ").append(h2vMap.get(HEADERNAME.LAND));
        tmp = h2vMap.get(HEADERNAME.OMRÅDE);
        if (tmp != null && !tmp.isBlank()) {
            sb.append(" Område: ").append(tmp);
        }
        String antal = h2vMap.get(HEADERNAME.INKÖPT_ANTAL);
        if (antal != null && !antal.isBlank()) {
            sb.append(" Inköpt antal: ").append(antal);
        }
        sb.append(" Leverantör: ").append(h2vMap.get(HEADERNAME.LEVERANTÖR)).append("\n");
        String förpackning = h2vMap.get(HEADERNAME.FÖRPACKNING);
        if (förpackning != null && !förpackning.isEmpty()) {
            sb.append("Förpackning: ").append(förpackning).append("\n");
        }
        if (!isSlim) {
            sb.append("Färg: \n");
            sb.append("Doft: \n");
            sb.append("Smak: \n");
            sb.append("Omdöme: \n");
            sb.append("Betyg (1-6):  \n");
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
            if (theCell==null) {
                emptyCells++;
                continue; // next loop
            }
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

    public Boolean analyzeHeaders(Row currentRow, Map<Integer, HEADERNAME> headerMapForward, Map<HEADERNAME, Integer> headerMapReverse) {
        Map<String, Integer> res = new HashMap<>();
        // Require at least Rubrik, Varunr and Namn in row to consider it a header row.
        if (detectHeader(currentRow, HEADERNAME.RUBRIK, HEADERNAME.VARUNR, HEADERNAME.PRODUKTNAMN)) {
            // Iterate over row, storing index of column matching header list in map
            // Iterate and collect headers
            Iterator cellIter = currentRow.cellIterator();
            while (cellIter.hasNext()) {
                Cell currentCell = (Cell) cellIter.next();
                Integer index = currentCell.getColumnIndex();
                try {
                    String cellValue = currentCell.getStringCellValue();
                    HEADERNAME hname = HEADERNAME.getValue(cellValue); // Make fuzzy
                    if (hname != HEADERNAME.UNKNOWN) {
                        // Save in maps
                        headerMapForward.put(index, hname);
                        headerMapReverse.put(hname, index);
                    } else {
                        if (!cellValue.isEmpty()) {
                            LOG.info("Unknown header value ignored: " + cellValue);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Could not read header value as string for cell ");
                }
            }
            return true;
        } else {
            LOG.info("Could not match Rubrik, Varunr and Name/Produktnamn in row " + currentRow.getRowNum());
        }
        return false;
    }

    public String getCellValueAsString(Cell theCell, HEADERNAME header) {
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
                if (header != null && header.name().toUpperCase().contains("DATUM")) {
                    return formatDateToString(theCell.getDateCellValue());
                }
                switch (header) {
                    case VARUNR:
                    case VOLYM:
                    case INKÖPT_ANTAL:
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

    public Integer findHeaderColumn(Row row, String header) {
        if (row == null || header == null) {
            LOG.info("Got null row or header, returning null!");
            return null;
        }
        HEADERNAME hname = HEADERNAME.getValue(header);
        if (hname == HEADERNAME.UNKNOWN) {
            return null;
        }
        Iterator iter = row.cellIterator();
        while (iter.hasNext()) {
            // Rteurn true if named header found
            Cell cell = (Cell) iter.next();
            CellType cellType = cell.getCellType();
            // We only care about string cells
            if (cellType.equals(CellType.STRING)) {
                if (header.equals(cell.getStringCellValue())) {
                    return cell.getColumnIndex();
                }
            }
        }
        return null;
    }

    public Boolean detectHeader(Row row, HEADERNAME header1, HEADERNAME header2, HEADERNAME header3) {
        Boolean ok1 = false;
        Boolean ok2 = false;
        Boolean ok3 = false;

        if (header1 == null || header2 == null || header3 == null) {
            System.out.println("Got null values for headers");
            return false;
        }

        Iterator iter = row.cellIterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            CellType cellType = cell.getCellType();
            if (cellType.equals(CellType.STRING)) {
                String cellContent = cell.getStringCellValue();
                if (header1.checkValue(cellContent)) {
                    ok1 = true;
                }
                if (header2.checkValue(cellContent)) {
                    ok2 = true;
                }
                if (header3.checkValue(cellContent)) {
                    ok3 = true;
                }
            }
        }
        return ok1 && ok2 && ok3;

    }

}
