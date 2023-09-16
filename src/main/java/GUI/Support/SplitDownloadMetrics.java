package GUI.Support;

import Enums.Program;
import Utils.Utility;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SplitDownloadMetrics {

    private final int id;
    private final long start;
    private final long end;
    private final String filename;
    private File file;
    private final URL url;
    private long bytesRead;
    private boolean failed = false;
    private boolean success = false;
    private boolean stop = false;


    public SplitDownloadMetrics(int id, long start, long end, String filename, URL url) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.filename = filename;
        this.url = url;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public int getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public FileOutputStream getFileOutputStream() {
        FileOutputStream fos;
        String filename = "temp" + Utility.randomString(10) + "_" + id + ".bin";
        try {
            Path filePath = Paths.get(Program.get(Program.DRIFTY_PATH), "temp", filename);
            if(!filePath.toFile().getParentFile().exists()) {
                FileUtils.createParentDirectories(filePath.toFile());
            }
            file = filePath.toFile();
            file.deleteOnExit();
            fos = new FileOutputStream(file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fos;
    }

    public File getFile() {
        return file;
    }

    public URL getUrl() {
        return url;
    }

    public boolean failed() {
        return failed;
    }

    public void setFailed() {
        failed = true;
    }

    public boolean success() {
        return success;
    }

    public void setSuccess() {
        success = !failed;
    }

    public boolean running() {
        return !success && !failed;
    }
    public boolean stop() {
        return stop;
    }

    public void setStop() {
        stop = true;
    }

    public byte[] getFileContent() {
        if(file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[(int) file.length()];
                int bytesRead = is.read(buffer);
                if(bytesRead == -1) {
                    throw new IOException("Failed to read file part: " + filename);
                }
                return buffer;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
