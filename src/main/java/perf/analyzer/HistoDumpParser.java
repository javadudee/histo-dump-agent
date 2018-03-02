package perf.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HistoDumpParser {
    private final ArrayList<HashMap<String, Integer>> histoCounts = new ArrayList<HashMap<String, Integer>>();
    private File delta_histo_output = null;

    public void parse(File file) throws IOException {

        // data starts on line 4
        int startLineNumber = 4;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashMap<String, Integer> classInstances = new HashMap<String, Integer>();
        try {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber >= startLineNumber) {
                    // parse file of the form
                    //
                    //  num     #instances         #bytes  class name
                    //----------------------------------------------
                    //        1:         53843        7472344  <constMethodKlass>
                    String[] arr = line.trim().split("\\s+");
                    if (arr.length > 3) {
                        String count = arr[1];
                        String className = arr[3];
                        classInstances.put(className, Integer.parseInt(count));
                    }
                }
            }
            // add histodump file's class instance counts to a list, to be processed later.
            histoCounts.add(classInstances);
        } finally {
            reader.close();
        }
    }

    public void createDeltaReport(String reportDir, String reportFile) {
        try {
            if (delta_histo_output == null) {
                delta_histo_output = new File(reportDir);
                delta_histo_output.mkdirs();
            }
            String text = "Instance Increase, Class Name";
            File deltaHistoOutputFile = new File(delta_histo_output.getAbsolutePath()
                    + File.separator + reportFile);
            FileWriter fw = new FileWriter(deltaHistoOutputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            HashMap<String, Integer> firstHisto = histoCounts.get(0);
            HashMap<String, Integer> currentHisto = histoCounts.get(histoCounts.size() - 1);

            bw.write(text);
            bw.write(System.getProperty("line.separator"));
            bw.write("------------------------------------------------------------------------------------");
            bw.write(System.getProperty("line.separator"));

            for (String entry : currentHisto.keySet()) {
                Integer histoDelta = 0;
                histoDelta = firstHisto.containsKey(entry) ? currentHisto.get(entry) - firstHisto.get(entry) : 0;

                if (histoDelta > 0) {
                    text = histoDelta + ", " + entry;
                    bw.write(text);
                    bw.write(System.getProperty("line.separator"));
                    System.out.println(text);
                }

            }
            bw.close();
            System.out.println("Delta file created: " + deltaHistoOutputFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error creating a delta histo dump");
        }
    }
}
