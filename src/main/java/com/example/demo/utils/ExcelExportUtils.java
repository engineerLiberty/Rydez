package com.example.demo.utils;

import com.example.demo.model.Orders;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
@Component
@RequiredArgsConstructor
public class ExcelExportUtils {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Orders> ordersList;

    public ExcelExportUtils(List<Orders> ordersList){
        this.ordersList = ordersList;
        workbook = new XSSFWorkbook();
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle cellStyle) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Enum) {
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof LocalDate) {
            cell.setCellValue(String.valueOf(value));
        }else{
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(cellStyle);
    }

    private void createHeaderRow(){
        sheet = workbook.createSheet("Order Information");
        Row row = sheet.createRow(0); //the title header
        CellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(20);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        createCell(row, 0, "Order Information", cellStyle);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0, 20));
        font.setFontHeightInPoints((short)10);

        row = sheet.createRow(1); // column header
        font.setBold(true);
        font.setFontHeight(16);
        cellStyle.setFont(font);
        createCell(row, 0, "Id", cellStyle);
        createCell(row, 1, "date", cellStyle);
        createCell(row, 2, "client code", cellStyle);
        createCell(row, 3, "client type", cellStyle);
        createCell(row, 4, "company name", cellStyle);
        createCell(row, 5, "customer name", cellStyle);
        createCell(row, 6, "delivery address", cellStyle);
        createCell(row, 7, "pick-up address", cellStyle);
        createCell(row, 8, "item quantity", cellStyle);
        createCell(row, 9, "item type", cellStyle);
        createCell(row, 10, "order status", cellStyle);
        createCell(row, 11, "payment type", cellStyle);
        createCell(row, 12, "reason for cancellation", cellStyle);
        createCell(row, 13, "rider id", cellStyle);
        createCell(row, 14, "rider name", cellStyle);
        createCell(row, 15, "bike number", cellStyle);
        createCell(row, 16, "third-party pickup", cellStyle);
        createCell(row, 17, "admin id", cellStyle);
        createCell(row, 18, "pick-up landmark", cellStyle);
        createCell(row, 19, "delivery landmark", cellStyle);
    }

    private void writeOrderDetails(){
        int rowCount = 2;
//        Row row = sheet.createRow(0); //the title header
        CellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        cellStyle.setFont(font);

        for(Orders orders : ordersList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, orders.getId(), cellStyle);
            createCell(row, columnCount++, orders.getCreatedAt(), cellStyle);
            createCell(row, columnCount++, orders.getClientCode(), cellStyle);
            createCell(row, columnCount++, orders.getCustomerType(), cellStyle);
            createCell(row, columnCount++, orders.getCompanyName(), cellStyle);
            createCell(row, columnCount++, orders.getCustomerFirstName() + " " + orders.getCustomerLastName(), cellStyle);
            createCell(row, columnCount++, orders.getDeliveryAddress(), cellStyle);
            createCell(row, columnCount++, orders.getPickUpAddress(), cellStyle);
            createCell(row, columnCount++, orders.getItemQuantity(), cellStyle);
            createCell(row, columnCount++, orders.getItemType(), cellStyle);
            createCell(row, columnCount++, orders.getOrderStatus(), cellStyle);
            createCell(row, columnCount++, orders.getPaymentType(), cellStyle);
            createCell(row, columnCount++, orders.getReasonForOrderCancellation(), cellStyle);
            createCell(row, columnCount++, orders.getRiderId(), cellStyle);
            createCell(row, columnCount++, orders.getRiderName(), cellStyle);
            createCell(row, columnCount++, orders.getBikeNumber(), cellStyle);
            createCell(row, columnCount++, orders.getThirdPartyPickUp(), cellStyle);
            createCell(row, columnCount++, orders.getDispatchAdminNumber(), cellStyle);
            createCell(row, columnCount++, orders.getLandmarkAtPickupAddress(), cellStyle);
            createCell(row, columnCount++, orders.getLandmarkAtDeliveryAddress(), cellStyle);
        }
    }

    public void exportOrdersDataToExcel(HttpServletResponse response) throws IOException {
        createHeaderRow();
        writeOrderDetails();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
