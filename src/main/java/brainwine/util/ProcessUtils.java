package brainwine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessUtils {
    
    public static ProcessResult executeCommand(String command) {        
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor(2, TimeUnit.SECONDS); // TODO why does reg query not exit?
            List<String> outputLog = readInputStream(process.getInputStream());
            List<String> errorLog = readInputStream(process.getErrorStream());
            return new ProcessResult(outputLog, errorLog);
        } catch(InterruptedException | IOException e) {
            return new ProcessResult(e);
        }
    }
    
    private static List<String> readInputStream(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        
        while((line = reader.readLine()) != null) {
            lines.add(line);
        }
        
        return lines;
    }
}
