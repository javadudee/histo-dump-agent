package perf.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class HistoDumpAnalyzer {
    public static void main(String[] args) throws InterruptedException, IOException {
        File dir = new File(args[0]);
        String reportDir = args[1];

        if (dir.isDirectory()) {
            File list[] = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            });

            Arrays.sort(list, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            HistoDumpParser parser = new HistoDumpParser();
            for (int i = 0; i < list.length; i++) {
                File file = list[i];
                System.out.println("Parsing:" + (i + 1) + "/" + list.length + " - " + file.getName());
                parser.parse(file);
                System.out.println("Done");
                if (i > 0) {
                    String fileName = "deltaReport-OriginalandDump" + i + ".csv";
                    parser.createDeltaReport(reportDir, fileName);
                }
            }

        } else {
            throw new FileNotFoundException(dir + " is not a directory");
        }
    }
}
