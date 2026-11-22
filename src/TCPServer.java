import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPServer {
    protected static File serverFiles = new File("ServerFiles"); //?

    static void main(String[] args)  {
        int serverPort = 3000;
        int numThreads = 10;
        ExecutorService es = Executors.newFixedThreadPool(numThreads);

        try (ServerSocket listenSocket = new ServerSocket(serverPort)){ // try with resource!
            es.submit(new KeyboardMonitor(listenSocket));

            boolean keepGoing = true;
            while (keepGoing) {
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
                String message = "";
                byte[] reply;

                InputStream in = listenSocket.getInputStream();
                OutputStream out = listenSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) { // -1 means that bytes r done being read
                    message += new String(buffer, 0, bytesRead);
                }
                String[] parts = message.split(":");
                String command = parts[0];

                // Start of menu
                if (command.equals("1")) {
                    listServerFiles(out);
                }
                else if (command.equals("2")){ // Parse client's response, get trigger and file name
                    String fileName = parts[1];
                    deleteServerFile(fileName, out);
                }
                else if (command.equals("3")) {
                    String currentFile = parts[1];
                    String newName = parts[2];
                    renameFile(currentFile,newName,out);
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
                out.write("S".getBytes());
            }
            else {
                out.write("F".getBytes());
            }
        }

        public void renameFile(String currentFileName, String newName, OutputStream out) throws IOException {
            File currentFile = new File(serverFiles, currentFileName);
            File newFile = new File(serverFiles, newName);

            if (!newFile.exists() && currentFile.renameTo(newFile)) {
                out.write("R".getBytes());
            }
            else {
                out.write("N".getBytes());
            }
        }
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

