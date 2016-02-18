package com.example;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by daniel zateikin on 16/02/2016.
 */
public class ExcelUtils {



    public static void write_excel(String[] newRow) throws IOException, BiffException, WriteException {


        File theExl = new File("output.xls");
        WritableSheet sheet;
        WritableWorkbook workbook;
        int numRows;
        File newFile;
        boolean rename = false;


        if (theExl.exists()){
            Workbook readbook = Workbook.getWorkbook(theExl);
            newFile = new File("newExl.xls");
            workbook = Workbook.createWorkbook(newFile, readbook);
            theExl.delete();
            sheet = workbook.getSheet(0);
            numRows = sheet.getRows();
            rename = true;
        }
        else{
            newFile = new File("output.xls");
            workbook = Workbook.createWorkbook(newFile);
            sheet = workbook.createSheet("data", 0);
            numRows = 0;
        }

        for (int i = 0; i < newRow.length ; i++){
            Label label =new Label(i, numRows, newRow[i]);
            sheet.addCell(label);

        }
        workbook.write();
        workbook.close();


        if (rename) {
            boolean success = newFile.renameTo(new File("output.xls"));
        }
    }

    public static DB creteDBFromExl(String filename) throws IOException, BiffException {
        int numRows;
        DB myDB = new DB();


        File myFile = new File(filename);
        if (!myFile.exists()){
            throw new IOException("no Excel file");
        }
        Workbook workbook = Workbook.getWorkbook(new File(filename));
        Sheet sheet = workbook.getSheet(0);
        numRows = sheet.getRows();

        for (int row = 0; row < numRows; row++){
            Cell[] cellArr = new Cell[7];
            String[] cellContent = new String[7];

            for (int cell = 0; cell < 7; cell ++){
                cellArr[cell] = sheet.getCell(cell, row);
                cellContent[cell] = cellArr[cell].getContents();
            }


            myDB.addCall(new Call(cellContent[6], cellContent[2], cellContent[3], cellContent[4], cellContent[5], cellContent[0]));
        }

        return myDB;

    }



}
