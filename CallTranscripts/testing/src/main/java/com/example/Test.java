package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


import jxl.*;
import jxl.biff.IntegerHelper;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

public class Test {

    public static void main (String[] args){

        String[] newRow = {"hey this is the predicting words yo", "234", "235235235", "34", "4", "0242344"};

        int i = 9;
        String a = Integer.toString(i);
        System.out.print(a);

//        try {
//            write_excel(newRow);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (BiffException e) {
//            e.printStackTrace();
//        } catch (WriteException e) {
//            e.printStackTrace();
//        }


    }


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
}
