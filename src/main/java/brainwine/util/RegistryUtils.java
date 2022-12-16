package brainwine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Small, silly hack-ish (and probably unreliable) utility class for doing Windows registry stuff.
 */
public class RegistryUtils {

    public static ProcessResult add(String location, String key, String value) {
        String command = "reg add \"%s\" /v \"%s\" /d \"%s\" /f";
        return ProcessUtils.executeCommand(String.format(command, location, key, value));
    }
    
    public static ProcessResult delete(String location, String key) {
        String command = "reg delete \"%s\" /v \"%s\" /f";
        return ProcessUtils.executeCommand(String.format(command, location, key));
    }
    
    public static ProcessResult query(String location) {
        return query(location, "*");
    }
    
    public static ProcessResult query(String location, String key) {
        String command = "reg query \"%s\" /v \"%s\"";
        return ProcessUtils.executeCommand(String.format(command, location, key));
    }
    
    public static RegistryKey getFirstQueryResult(ProcessResult queryProcessResult) {
        List<RegistryKey> keys = getQueryResult(queryProcessResult);
        return keys.isEmpty() ? null : keys.get(0);
        
    }
    
    public static List<RegistryKey> getQueryResult(ProcessResult queryProcessResult) {
        List<RegistryKey> keys = new ArrayList<>();
        
        if(!queryProcessResult.hasErrors() && !queryProcessResult.hasException()) {
            List<String> outputLog = queryProcessResult.getOutputLog();
            
            if(outputLog.size() > 2) {
                String location = outputLog.get(1);
                
                for(int i = 2; i < outputLog.size(); i++) {
                    String[] segments = outputLog.get(i).trim().split("    ");
                    
                    if(segments.length == 2 || segments.length == 3) {
                        keys.add(new RegistryKey(location, segments[0], segments[1], segments.length == 3 ? segments[2] : null));
                    }
                }
            }
        }
        
        return keys;
    }
}
