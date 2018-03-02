package perf.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Histo dump utility that can be injected as a javaagent to any JVM.
 *
 * Histo dump intervals can be configured using System properties
 * max.histodumps and histodumps.interval. Interval is in seconds.
 *
 * e.g. setting JAVA_OPTS in tomcat's catalina.sh like this will generate max of 200 dumps 2 mins apart.
 *
 * export JAVA_OPTS="-javaagent:/Users/sraj/temp/histo-dump-agent-1.0.jar
 * -Dtest.run.with.histodumps=true -Dmax.histodumps=200 -Dhistodumps.interval=120"
 *
 */

public class HistoDumpAgent {

    private static final int MAX_HISTO_DUMPS = 100;
    private static final int HISTO_DUMP_INTERVAL_SEC = 60;
    private static int maxHistoDumps;
    private static int histoDumpIntervals;

    static final HistoDumpWorker histoDumpWorker = new HistoDumpWorker();
    static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    static final String pid = runtimeMXBean.getName().substring(0, (runtimeMXBean.getName()).indexOf('@'));
    static final long startTime = System.currentTimeMillis();
    static File histoDumpDir = null;

    public static void premain(String paramString, Instrumentation paramInstrumentation) {

        String strMaxHistodumps = System.getProperty("max.histodumps");
        maxHistoDumps = (strMaxHistodumps == null || strMaxHistodumps.equals("")) ? MAX_HISTO_DUMPS : Integer.parseInt(strMaxHistodumps);

        String strHistoDumpInterval = System.getProperty("histodumps.interval");
        histoDumpIntervals = (strHistoDumpInterval == null || strHistoDumpInterval.equals("")) ?
                HISTO_DUMP_INTERVAL_SEC : Integer
                .parseInt(strHistoDumpInterval);

        histoDumpWorker.start();
    }

    private static class HistoDumpWorker extends Thread {

        public HistoDumpWorker() {
            setDaemon(true);
        }

        public void run() {
            int count = 0;
            while (count < maxHistoDumps) {
                writeHistoDumps();
                try {
                    Thread.sleep(histoDumpIntervals * 1000);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                    return;
                }
            }
        }

        private void writeHistoDumps() {
            if (histoDumpDir == null) {
                histoDumpDir = new File("/tmp/histo-dumps-" + startTime);
                histoDumpDir.mkdirs();
            }
            long timeStamp = System.currentTimeMillis();

            File newFile =
                    new File(histoDumpDir.getAbsolutePath() + File.separator + "histodump-" + timeStamp + ".txt");
            try {
                String content;
                File histoOutputFile = new File(newFile.getAbsolutePath());

                String[] cmd = {"jmap", "-histo:live", pid.toString()};
                ProcessBuilder pb = new ProcessBuilder(cmd);
                Process p = pb.start();

                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                FileWriter fw = new FileWriter(histoOutputFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);

                while ((content = br.readLine()) != null) {
                    bw.write(content);
                    bw.write(System.getProperty("line.separator"));
                }

                while ((content = er.readLine()) != null) {
                    System.out.println(content);
                }

                bw.close();
                br.close();

                System.out.println("Heap histo created: " + histoOutputFile.getAbsolutePath());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("Error creating a histo dump");
            }
        }
    }
}
