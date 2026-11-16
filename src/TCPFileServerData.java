import java.io.File;
import java.io.FileInputStream;

import static java.lang.System.out;

public class TCPFileServerData {
    private static class ClientHandler implements Runnable {
        @Override
        public void run() {

            File fileToSend = new File(folder, fileName);

            if (fileToSend.exists() && !fileToSend.isDirectory()) {
                // Status code
                out.write('S');

                // The file content
                try (FileInputStream fis = new FileInputStream(fileToSend)) {
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                out.println("Error: File not found on server");
                // Status code
                out.write('F');
            }
        }
    }
}
