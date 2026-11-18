import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPServer {
    protected static File serverFiles = new File("/Users/nathanpinkelton/Desktop/CS316/Final_316/src/ServerFiles"); //?

    public static void main(String[] args)  {
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
                String message = "";
                byte[] reply;

                System.out.println("Client connected: " + listenSocket.getInetAddress());

                InputStream in = listenSocket.getInputStream();
                OutputStream out = listenSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) { // -1 means that bytes r done being read
                    message = new String(buffer, 0, bytesRead);
                    System.out.println("Client message: " + message);
                    reply = message.toUpperCase().getBytes();
                    out.write(reply);
                }

                // Start of menu
                if (message.equalsIgnoreCase("List")) {
                    listServerFiles(out);
                }
                listenSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void listServerFiles(OutputStream out) {
            File[] files = serverFiles.listFiles();

            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i].getName());
                try {
                    out.write(files[i].getName().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

