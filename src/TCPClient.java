import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Need two arguments: serverIP and serverPort.");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        Scanner keyboard = new Scanner(System.in);
        int numThreads = 10;
        ExecutorService es = Executors.newFixedThreadPool(numThreads);

        boolean keepGoing = true;

        while (keepGoing) {
            menu();
            String input = keyboard.nextLine().trim();
            if (input.equals("6")) {
                break;
            }

            if (!input.matches("[1-5]")) {
                System.out.println("Invalid choice.");
                continue;
            }

            String message = "";
            String fileName = "";

            if (input.equals("1")) {
                message = "1";
            } else if (input.equals("2")) {
                System.out.print("Enter file name to delete: ");
                fileName = keyboard.nextLine().trim();
                message = "2:" + fileName;
            } else if (input.equals("3")) {
                System.out.print("Current file: ");
                String currentFile = keyboard.nextLine().trim();
                System.out.print("New name: ");
                String newName = keyboard.nextLine().trim();
                message = "3:" + currentFile + ":" + newName;
            } else if (input.equals("4")) {
                System.out.print("Enter file name to download: ");
                fileName = keyboard.nextLine().trim();
                es.submit(new TransferTask(serverIP, serverPort, input, fileName));
                continue;
            } else if (input.equals("5")) {
                System.out.print("Enter file name to upload: ");
                fileName = keyboard.nextLine().trim();

                File fileToSend = new File("ClientFiles", fileName);
                if (fileToSend.exists() && !fileToSend.isDirectory()) {
                    es.submit(new TransferTask(serverIP, serverPort, input, fileName));
                } else {
                    System.out.println("File not found");
                }
                continue;
            }


            try (Socket socket = new Socket(serverIP, serverPort)) {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                out.write(message.getBytes());
                socket.shutdownOutput();

                if (input.equals("1")) {
                    String reply = "";
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        reply += new String(buffer, 0, bytesRead);
                    }
                    System.out.print(reply);
                } else {
                    int status = in.read();
                    if (status == 'S') {
                        System.out.println("File successfully deleted.");
                    } else if (status == 'F') {
                        System.out.println("Could not delete file.");
                    } else if (status == 'R') {
                        System.out.println("File successfully renamed.");
                    } else if (status == 'N') {
                        System.out.println("Failed to rename file.");
                    }
                }
            }
        }

        es.shutdown();
    }

    private static class TransferTask implements Runnable {
        private final String serverIP;
        private final int serverPort;
        private final String command;   // "4" = download, "5" = upload
        private final String fileName;

        private TransferTask(String serverIP, int serverPort, String command, String fileName) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.command = command;
            this.fileName = fileName;
        }

        public void run() {
            try (Socket socket = new Socket(serverIP, serverPort)) {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                // send the request
                if (command.equals("4")) {
                    out.write(("4:" + fileName).getBytes());
                } else {
                    out.write(("5:" + fileName + ":").getBytes());
                    try (FileInputStream fis = new FileInputStream(new File("ClientFiles", fileName))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                socket.shutdownOutput();

                // handle the reply
                int status = in.read();
                if (command.equals("4")) {
                    if (status == 'D') {
                        try (FileOutputStream fos = new FileOutputStream(new File("ClientFiles", fileName))) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        System.out.println("Download of " + fileName + " has completed.");
                    } else if (status == 'X') {
                        System.out.println("File not found on server.");
                    }
                } else {
                    if (status == 'U') {
                        System.out.println("File successfully uploaded.");
                    } else {
                        System.out.println("Could not upload file.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void menu() {
        System.out.println("Please pick an option:");
        System.out.print("1: List\n2. Delete\n3. Rename\n4. Download\n5. Upload\n6. Quit\nEnter your choice: ");
    }
}