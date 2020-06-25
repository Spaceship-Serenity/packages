package com.dataiku.dss.formats.libsvm;


import com.dataiku.dip.datalayer.memimpl.MemRow;
import com.dataiku.dip.datalayer.memimpl.MemTable;
import com.dataiku.dip.datalayer.memimpl.MemTableAppendingOutput;

import com.dataiku.dip.plugin.CustomFormatInput;
import com.dataiku.dip.plugin.InputStreamWithFilename;
import org.junit.Test;
import com.dataiku.dip.warnings.WarningsContext;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;

import java.io.InputStream;

public class LIBSVMFormatTest {
    private void assertSize(MemTable mt, int rows, int cols) {
        assertEquals(rows, mt.rows.size());
        assertEquals(cols, mt.columns.size());
    }

    private void assertHasCol(MemTable mt, String col) {
        assertTrue(mt.columns.containsKey(col));
        assertEquals(col, mt.column(col).getName());
    }

    private void assertCells(MemTable mt, String[][] data) {
        String[] cols = data[0];

        for (int i = 1; i < data.length; i++) {
            String[] line = data[i];
            MemRow mr = mt.rows.get(i - 1);

            for (int j = 0; j < line.length; j++) {
                String cell = line[j];
                assertEquals(cell, mr.get(mt.column(cols[j])));
            }
        }
    }

    private InputStreamWithFilename getResourceFile(String filename) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("com/dataiku/dss/formats/libsvm/" + filename);
        return new InputStreamWithFilename(is, filename);
    }

    private JsonObject createConfig(int maxFeatures, String outputType) {
        JsonObject config = new JsonObject();
        config.addProperty("max_features", maxFeatures);
        config.addProperty("output_type", outputType);
        return config;
    }

    @Test
    public void defaultConfig() throws Exception {
        MemTable mt = new MemTable();
        WarningsContext wc = new WarningsContext();
        CustomFormatInput lsvmInput = new LIBSVMFormat().getReader(null, null);

        lsvmInput.setWarningsContext(wc);
        lsvmInput.run(getResourceFile("australian.svm"), new MemTableAppendingOutput(mt), mt, mt);

        assertEquals(wc.getTotalCount(), 0);
        assertSize(mt, 690, 15);

        String[] cols = {"Label", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        for (String col : cols) {
            assertHasCol(mt, col);
        }
    }

    @Test
    public void customMaxFeaturesConfig() throws Exception {
        MemTable mt = new MemTable();
        WarningsContext wc = new WarningsContext();
        CustomFormatInput lsvmInput = new LIBSVMFormat().getReader(createConfig(10, "multi_column"), null);

        lsvmInput.setWarningsContext(wc);
        lsvmInput.run(getResourceFile("australian.svm"), new MemTableAppendingOutput(mt), mt, mt);

        assertEquals(wc.getTotalCount(), 0);
        assertSize(mt, 690, 11);

        String[] cols = {"Label", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        for (String col : cols) {
            assertHasCol(mt, col);
        }
    }


    @Test
    public void customOutputTypeConfig() throws Exception {
        MemTable mt = new MemTable();
        WarningsContext wc = new WarningsContext();
        CustomFormatInput lsvmInput = new LIBSVMFormat().getReader(createConfig(0, "single_column_json"), null);

        lsvmInput.setWarningsContext(wc);
        lsvmInput.run(getResourceFile("australian.svm"), new MemTableAppendingOutput(mt), mt, mt);

        assertEquals(wc.getTotalCount(), 0);
        assertSize(mt, 690, 2);

        String[] cols = {"Label", "Features"};
        for (String col : cols) {
            assertHasCol(mt, col);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongConfig() throws Exception {
        new LIBSVMFormat().getReader(createConfig(10, "unknown"), null);
    }


    @Test
    public void warningContextErrors() throws Exception {
        MemTable mt = new MemTable();
        WarningsContext wc = new WarningsContext();
        CustomFormatInput lsvmInput = new LIBSVMFormat().getReader(null, null);
        lsvmInput.setWarningsContext(wc);
        lsvmInput.run(getResourceFile("australian_errors.svm"), new MemTableAppendingOutput(mt), mt, mt);

        /* Debug: show warning context errors
        for (Map.Entry<WarningsContext.WarningType, WarningsContext.WarningTypeData> entry : wc.getOutput().getWarnings().entrySet()) {
            System.out.println(entry.getKey().name());
            for (WarningsContext.StoredWarning storedWarning : entry.getValue().stored) {
                System.out.println(storedWarning.message);
            }
        }
        */

        assertEquals(wc.getTotalCount(), 7);
        assertSize(mt, 687, 15);
    }
}

