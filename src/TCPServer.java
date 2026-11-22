import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPServer {
    protected static File serverFiles = new File("ServerFiles"); //?

    static void main()  {
        int serverPort = 3000;
        int numThreads = 10;
        ExecutorService es = Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listenSocket = new ServerSocket(serverPort)){ // try with resource!
            es.submit(new KeyboardMonitor(listenSocket));

            while (true) {
                // wait for a client to connect
                Socket clientSocket = listenSocket.accept();
                es.submit(new ClientHandler(clientSocket));
            }

        } catch (SocketException e) {
            es.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket listenSocket;

        private ClientHandler(Socket listenSocket) {
            this.listenSocket = listenSocket;
        }

        public void run() {
            try {
                InputStream in = listenSocket.getInputStream();
                OutputStream out = listenSocket.getOutputStream();

                String command = readUntilColon(in);

                if (command.equals("1")) {
                    listServerFiles(out);
                }
                else if (command.equals("2")){
                    String fileName = readUntilColon(in);
                    deleteServerFile(fileName, out);
                }
                else if (command.equals("3")) {
                    String currentFile = readUntilColon(in);
                    String newName = readUntilColon(in);
                    renameFile(currentFile, newName, out);
                }
                else if (command.equals("4")) {
                    String fileName = readUntilColon(in);
                    downloadFile(fileName, out);
                }
                else if (command.equals("5")){
                    String fileName = readUntilColon(in);
                    uploadFile(fileName, out, in);
                }

                listenSocket.close();

            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void listServerFiles(OutputStream out) {
            File[] files = serverFiles.listFiles();

            for (int i = 0; i < files.length; i++) {
                try {
                    String fileName = files[i].getName();
                    if (fileName.charAt(0) == '.') {
                        continue;
                    }
                    else {
                        out.write((files[i].getName() + "\n").getBytes());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void deleteServerFile(String fileName, OutputStream out) throws IOException {
            File fileToDelete = new File(serverFiles + "/" + fileName);
            boolean isDeleted = fileToDelete.delete();

            if (isDeleted) {
                out.write('S');
            }
            else {
                out.write('F');
            }
        }

        public void renameFile(String currentFileName, String newName, OutputStream out) throws IOException {
            File currentFile = new File(serverFiles, currentFileName);
            File newFile = new File(serverFiles, newName);

            if (!newFile.exists() && currentFile.renameTo(newFile)) {
                out.write('R');
            }
            else {
                out.write('N');
            }
        }

        public void downloadFile(String fileName, OutputStream out) throws IOException {
            File fileToSend = new File(serverFiles, fileName);
            byte[] buffer = new byte[1024];
            int bytesRead;

            if (fileToSend.exists() && !fileToSend.isDirectory()) {
                out.write('D');

                try (FileInputStream fis = new FileInputStream(fileToSend)) {
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                out.write('X');
            }
        }

        public void uploadFile(String fileName, OutputStream out, InputStream in) throws IOException {
            File outFile = new File(serverFiles, fileName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                out.write('U');
            } catch (IOException e) {
                out.write('V');
                e.printStackTrace();
            }
        }
    }

    private static String readUntilColon(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int input;
        while ((input = in.read()) != -1 && input != ':') {
            sb.append((char) input);
        }
        return sb.toString();
    }

    private static class KeyboardMonitor implements Runnable {
        private final ServerSocket clientSocket;

        private KeyboardMonitor(ServerSocket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            Scanner keyboard = new Scanner(System.in);
            try {
                while (true) {
                    String input = keyboard.nextLine().trim();
                    if (input.equalsIgnoreCase("Q")) {
                        clientSocket.close();
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

