import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
public static void main(String[] args) throws IOException {

    boolean keepGoing = true;
    TCPServer server = new TCPServer();

    if (args.length != 2) {
        System.out.println("Need two arguments: serverIP and serverPort.");
        return;
    }
    String serverIP = args[0];
    int serverPort = Integer.parseInt(args[1]);

    while (keepGoing) {
        menu();
        Scanner keyboard = new Scanner(System.in);
        String input = keyboard.nextLine();
        byte[] data = input.getBytes();
        // establish a connection w/ the server
        Socket socket = new Socket(serverIP, serverPort);

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        out.write(data);
        // trigger the special signal to the server,
        // letting server get out of its while loop
        socket.shutdownOutput();

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) != -1) { // stops when servers socket closes
            String reply = new String(buffer, 0, bytesRead);
            System.out.println(reply);
        }

        if (input.equalsIgnoreCase("2")) {
            System.out.println("Enter File name to Delete: ");
            String fileName = keyboard.nextLine();
            out.write(fileName.getBytes());
        }

        if (input.equalsIgnoreCase("q")) {
            socket.close();
            keepGoing = false;
        }
    }
}

public static void menu() {
    System.out.println("Please pick an option:");
    System.out.println("1: List\n2. Delete\n3. Rename\n4. Download\n5. Upload\n6. Quit");
}
}