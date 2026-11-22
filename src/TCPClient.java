import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            String input = keyboard.nextLine();
            if (input.equals("6")){
                break;
            }

            byte[] data = input.getBytes();
            // establish a connection w/ the server
            Socket socket = new Socket(serverIP, serverPort);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            if (input.equals("1")) {
                out.write(data);
            }
            else if (input.equals("2")) {
                String message = "";
                System.out.println("Enter File name to Delete: ");
                String fileName = keyboard.nextLine();
                message = "2" + ":" + fileName;
                out.write(message.getBytes());
            }
            else if (input.equals("3")) {
                String message = "";
                System.out.println("Enter File name to be Renamed: ");
                String fileName = keyboard.nextLine();
                System.out.println("Enter the new name for the File: ");
                String newFileName = keyboard.nextLine();
                message = "3" + ":" + fileName + ":" + newFileName;
                out.write(message.getBytes());
            }

            // trigger the special signal to the server,
            // letting server get out of its while loop
            socket.shutdownOutput();

            int bytesRead;
            String reply = "";
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) != -1) { // stops when servers socket closes
                reply = new String(buffer, 0, bytesRead);
            }

            if (reply.equals("S")) {
                System.out.println("File Successfully Deleted");
            }
            else if (reply.equals("F")){
                System.out.println("Could not delete File");
            }
            else {
                System.out.println(reply);
            }
        }
    }

    public static void menu() {
        System.out.println("Please pick an option:");
        System.out.println("1: List\n2. Delete\n3. Rename\n4. Download\n5. Upload\n6. Quit\nEnter Your choice: ");
    }
}