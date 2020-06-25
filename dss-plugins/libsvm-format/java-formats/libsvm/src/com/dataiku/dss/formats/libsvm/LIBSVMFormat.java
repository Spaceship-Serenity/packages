package com.dataiku.dss.formats.libsvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dataiku.dip.coremodel.Schema;
import com.dataiku.dip.coremodel.SchemaColumn;
import com.dataiku.dip.datalayer.Column;
import com.dataiku.dip.datalayer.ColumnFactory;
import com.dataiku.dip.datalayer.ProcessorOutput;
import com.dataiku.dip.datalayer.Row;
import com.dataiku.dip.datalayer.RowFactory;
import com.dataiku.dip.plugin.CustomFormat;
import com.dataiku.dip.plugin.CustomFormatSchemaDetector;
import com.dataiku.dip.plugin.CustomFormatInput;
import com.dataiku.dip.plugin.CustomFormatOutput;
import com.dataiku.dip.warnings.WarningsContext;
import com.dataiku.dip.plugin.InputStreamWithContextInfo;
import com.dataiku.dip.logging.LimitedLogContext;
import com.dataiku.dip.logging.LimitedLogFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class LIBSVMFormat implements CustomFormat {
    /**
     * According to: https://github.com/cjlin1/libsvm/blob/master/README
     * <label> can be any real number, <index> is an integer and <value> is a real number
     * Also while digging in: https://github.com/cjlin1/libsvm/blob/master/svm-train.c
     * You can notice that `strtod` is used to parse the label and value, while `strtol` is used for the index
     */
    public static final Pattern LABEL_PATTERN = Pattern.compile("^([+-]?\\d+[.]?\\d*(?:[eE][+-]?\\d+)?)\\s+");
    // \G allows to continue at the end of the previous match without updating the region
    public static final Pattern TOKEN_PATTERN = Pattern.compile("\\G(\\d+):([+-]?\\d+[.]?\\d*(?:[eE][+-]?\\d+)?)\\s+");
    private static final Logger logger = Logger.getLogger(LIBSVMFormat.class);

    private enum ExtractionMode {
        MULTI_COLUMN,
        SINGLE_COLUMN_JSON,
        SINGLE_COLUMN_ARRAY;

        public static ExtractionMode forName(String rep) {
            if (StringUtils.isBlank(rep)) {
                return ExtractionMode.MULTI_COLUMN;
            } else {
                for (ExtractionMode mode : values()) {
                    if (mode.name().equalsIgnoreCase(rep)) {
                        return mode;
                    }
                }
            }
            throw new IllegalArgumentException("Unsupported extraction mode: " + rep);
        }
    }

    private int maxFeatures;
    private ExtractionMode outputType;
    private WarningsContext warnContext;

    /**
     * Create a new instance of the format
     */
    public LIBSVMFormat() {
        maxFeatures = 2000;
        outputType = ExtractionMode.MULTI_COLUMN;
    }

    /**
     * Create a reader for a stream in the format
     */
    @Override
    public CustomFormatInput getReader(JsonObject config, JsonObject pluginConfig) {
        if (config != null) {
            JsonElement tmpElt = config.get("max_features");
            if (tmpElt != null && tmpElt.isJsonPrimitive() && tmpElt.getAsInt() > 0) {
                maxFeatures = tmpElt.getAsInt();
            }

            tmpElt = config.get("output_type");
            if (tmpElt != null && tmpElt.isJsonPrimitive()) {
                outputType = ExtractionMode.forName(tmpElt.getAsString());
            }
        }

        return new LIBSVMFormatInput();
    }

    /**
     * Create a writer for a stream in the format
     */
    @Override
    public CustomFormatOutput getWriter(JsonObject config, JsonObject pluginConfig) {
        throw new UnsupportedOperationException("No output for this format.");
    }

    /**
     * Create a schema detector for a stream in the format (used if canReadSchema=true in the json)
     */
    @Override
    public CustomFormatSchemaDetector getDetector(JsonObject config, JsonObject pluginConfig) {
        throw new UnsupportedOperationException("No detector for this format.");
    }

    public class LIBSVMFormatInput implements CustomFormatInput {
        /**
         * Called if the schema is available (ie, dataset has been created)
         */
        @Override
        public void setSchema(Schema schema, boolean allowExtraColumns) {
        }

        @Override
        public void setWarningsContext(WarningsContext wc) {
            warnContext = wc;
        }

        private void addWarning(WarningsContext.WarningType type, String message, Object... format) {
            warnContext.addWarning(type, String.format(message, format), logger);
        }

        private void parseIntoMultiColumn(ProcessorOutput out, ColumnFactory cf, RowFactory rf,
                                          BufferedReader bf) throws Exception {
            HashMap<String, Column> columns = new HashMap<>();
            Column labelColumn = cf.column("Label");

            Matcher labelMatcher = LABEL_PATTERN.matcher("");
            Matcher tokenMatcher = TOKEN_PATTERN.matcher("");

            String line;
            int lineNumber = 0;

            while ((line = bf.readLine()) != null) {
                lineNumber++;
                labelMatcher.reset(line);
                tokenMatcher.reset(line);

                // If no label is found the line is considered as invalid and skipped
                if (!labelMatcher.find()) {
                    addWarning(WarningsContext.WarningType.INPUT_DATA_LINE_DOES_NOT_PARSE,
                            "No label detected at line %d; skipping it: '%s'", lineNumber, line);
                    continue;
                }
                Row row = rf.row();

                String label = labelMatcher.group(1);
                row.put(labelColumn, label);

                int start = labelMatcher.end();
                tokenMatcher.region(start, line.length());

                while (tokenMatcher.find()) {
                    String index = tokenMatcher.group(1);
                    String value = tokenMatcher.group(2);

                    start = tokenMatcher.end();

                    Column column = columns.get(index);
                    // Creating the newly found column if possible
                    if (column == null) {
                        if (columns.size() >= maxFeatures) {
                            continue;
                        }

                        column = cf.column(index);
                        columns.put(index, column);
                    }

                    row.put(column, value);
                }

                if (start != line.length()) {
                    addWarning(WarningsContext.WarningType.INPUT_DATA_LINE_DOES_NOT_PARSE,
                            "Invalid token at line %d, column %d; ignoring the rest of that line: '%s'", lineNumber, start, line.substring(start));
                }

                out.emitRow(row);
            }
            out.lastRowEmitted();
        }

        private void parseIntoSingleJSONColumn(ProcessorOutput out, ColumnFactory cf, RowFactory rf,
                                               BufferedReader bf) throws Exception {
            Column labelColumn = cf.column("Label");
            Column featuresColumn = cf.column("Features");

            StringBuilder features = new StringBuilder();
            Matcher labelMatcher = LABEL_PATTERN.matcher("");
            Matcher tokenMatcher = TOKEN_PATTERN.matcher("");

            String line;
            int lineNumber = 0;

            while ((line = bf.readLine()) != null) {
                lineNumber++;
                labelMatcher.reset(line);
                tokenMatcher.reset(line);

                // If no label is found the line is considered as invalid and skipped
                if (!labelMatcher.find()) {
                    addWarning(WarningsContext.WarningType.INPUT_DATA_LINE_DOES_NOT_PARSE,
                            "No label detected at line %d; skipping it: '%s'", lineNumber, line);
                    continue;
                }
                Row row = rf.row();

                String label = labelMatcher.group(1);
                row.put(labelColumn, label);

                int start = labelMatcher.end();
                tokenMatcher.region(start, line.length());

                // Creating the JSON using StringBuilder as it's a simple object
                features.append("{");

                while (tokenMatcher.find()) {
                    String index = tokenMatcher.group(1);
                    String value = tokenMatcher.group(2);

                    start = tokenMatcher.end();

                    if (features.length() > 3) {
                        features.append(',');
                    }

                    features.append('"')
                            .append(index)
                            .append("\":")
                            .append(value);
                }

                if (start != line.length()) {
                    addWarning(WarningsContext.WarningType.INPUT_DATA_LINE_DOES_NOT_PARSE,
                            "Invalid token at line %d, column %d; ignoring the rest of that line: '%s'", lineNumber, start, line.substring(start));
                }

                features.append("}");

                row.put(featuresColumn, features.toString());
                out.emitRow(row);

                features.setLength(0);
            }
            out.lastRowEmitted();
        }

        /**
         * extract data from the input stream. The emitRow() on the out will throw exceptions to
         * enforce limits set to number of rows read, so these should not be caught and hidden.
         */
        @Override
        public void run(InputStreamWithContextInfo in, ProcessorOutput out, ColumnFactory cf, RowFactory rf) throws Exception {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(in.getInputStream()))) {
                switch (outputType) {
                    case SINGLE_COLUMN_JSON:
                        parseIntoSingleJSONColumn(out, cf, rf, bf);
                        break;
                    case MULTI_COLUMN:
                    default:
                        parseIntoMultiColumn(out, cf, rf, bf);
                        break;
                }
            }
        }

        @Override
        public void close() throws IOException {
        }
    }

}
