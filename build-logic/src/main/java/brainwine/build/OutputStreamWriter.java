package brainwine.build;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface OutputStreamWriter {

    public void write(OutputStream outputStream) throws IOException;
}
