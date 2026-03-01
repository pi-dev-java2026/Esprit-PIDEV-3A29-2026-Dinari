package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
public class LoggerUtil {


    private static final String LOG_FILE = "logs/app-log.txt";

    public static void log(String action, String entity, String details) {
        try {

            File file = new File("logs");
            if (!file.exists()) {
                file.mkdirs();
            }

            FileWriter writer = new FileWriter(LOG_FILE, true);

            String logLine = LocalDateTime.now()
                    + " | ACTION=" + action
                    + " | ENTITY=" + entity
                    + " | DETAILS=" + details
                    + "\n";

            writer.write(logLine);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


