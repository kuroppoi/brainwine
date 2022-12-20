package brainwine.util;

import java.util.Collections;
import java.util.List;

public class ProcessResult {
    
    private List<String> outputLog;
    private List<String> errorLog;
    private Exception exception;
    
    public ProcessResult(List<String> outputLog, List<String> errorLog) {
        this.outputLog = outputLog;
        this.errorLog = errorLog;
    }
    
    public ProcessResult(Exception exception) {
        this.exception = exception;
    }
    
    public List<String> getOutputLog() {
        return Collections.unmodifiableList(outputLog);
    }
    
    public boolean hasErrors() {
        return !errorLog.isEmpty();
    }
    
    public List<String> getErrorLog() {
        return Collections.unmodifiableList(errorLog);
    }
    
    public boolean hasException() {
        return exception != null;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public boolean wasSuccessful() {
        return !hasErrors() && !hasException();
    }
}
