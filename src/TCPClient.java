import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Need two arguments: serverIP and serverPort.");
            return;
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        Scanner keyboard = new Scanner(System.in);

        boolean keepGoing = true;

        while (keepGoing) {
            menu();
            String input = keyboard.nextLine().trim();
            //checks for valid inputs
            if (input.equals("6")) {
                break;
            }

            if (!input.matches("[1-5]")) {
                System.out.println("Invalid choice.");
                continue;
            }

            String message = "";
            String fileName = "";

            //handles all the data to send to the sever
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
                message = "4:" + fileName;
            } else if (input.equals("5")) {
                System.out.print("Enter file name to upload: ");
                fileName = keyboard.nextLine().trim();

                File fileToSend = new File("ClientFiles", fileName);

                if (fileToSend.exists() && !fileToSend.isDirectory()) {
                    message = "5:" + fileName + ":";
                } else {
                    System.out.println("File not found");
                    continue;
                }
            }


            try (Socket socket = new Socket(serverIP, serverPort)) {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                out.write(message.getBytes());

                if (input.equals("5")) {
                    try (FileInputStream fis = new FileInputStream(new File("ClientFiles", fileName))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }

                socket.shutdownOutput();


                //handles all reply data
                if (input.equals("1")) {
                    String reply = "";
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        reply += new String(buffer, 0, bytesRead);
                    }
                    System.out.print(reply);

                }

                else if (input.equals("4")) {
                    int status = in.read();
                    if (status == 'D') {
                        File outFile = new File("ClientFiles", fileName);

                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        System.out.println("Download of " + fileName + " has completed.");
                    }
                    else if (status == 'X') {
                        System.out.println("File not found on server.");
                    }
                }

                else {
                    int status = in.read();
                    if (status == 'S') {
                        System.out.println("File successfully deleted.");
                    }
                    else if (status == 'F') {
                        System.out.println("Could not delete file.");
                    }
                    else if (status == 'R') {
                        System.out.println("File successfully renamed.");
                    }
                    else if (status == 'N') {
                        System.out.println("Failed to rename file.");
                    }
                    else if (status == 'U') {
                        System.out.println("File successfully uploaded.");
                    }
                    else {
                        System.out.println("Could not upload file.");
                    }
                }
            }
        }
    }

    public static void menu() {
        System.out.println("Please pick an option:");
        System.out.print("1: List\n2. Delete\n3. Rename\n4. Download\n5. Upload\n6. Quit\nEnter your choice: ");
    }
}