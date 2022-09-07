package pl.szmaus.firebirdraks3000.service;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class UnzipInputStream {
    public InputStream unzip(InputStream inputS) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputS);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            // consume all the data from this entry
            while (zis.available() > 0)
                return zis;
           zis.closeEntry();
        }
        return zis;
    }
}
