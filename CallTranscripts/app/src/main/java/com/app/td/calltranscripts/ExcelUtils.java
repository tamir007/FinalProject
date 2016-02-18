package com.app.td.calltranscripts;


import android.os.Environment;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {

    public static void write_excel(String[] newRow) throws IOException, BiffException, WriteException {


        File theExl = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.xls");
        WritableSheet sheet;
        WritableWorkbook workbook;
        int numRows;
        File newFile;
        boolean rename = false;


        if (theExl.exists()){
            Workbook readbook = Workbook.getWorkbook(theExl);
            newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/newExl.xls");
            workbook = Workbook.createWorkbook(newFile, readbook);
            theExl.delete();
            sheet = workbook.getSheet(0);
            numRows = sheet.getRows();
            rename = true;
        }
        else{
            newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.xls");
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
            boolean success = newFile.renameTo(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.xls"));
        }
    }

}
