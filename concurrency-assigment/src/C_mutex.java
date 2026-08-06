import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.SecureRandom;
import java.util.Base64;

// The mutex has two socket connections
// One for its 7001 port where it listens for the token to be sent back
// The other for the users/client/nodes port where it will send the token
public class C_mutex extends Thread {
    C_buffer buffer;
    Socket s;
    int port;

    // ip address and port number of the node requesting the token.
    // They will be fetched from the buffer    
    String n_host;
    int n_port;

    // Token
    String token;

    public C_mutex(C_buffer b, int p) {
        buffer = b;
        port = p;
        token = generateToken();
    }

    public void run() {
        ServerSocket ss_back = null;

        try {
            //  >>>  Listening from the server socket on port 7001
            // from where the TOKEN will be returned later.
            ss_back = new ServerSocket(7001);

        } catch (IOException e) {
            System.out.println("Failed to create server socket: " + e.getMessage());
            return;
        }

        try {
            while (true) {

                // >>> Print some info on the current buffer content for debugging purposes.
                // >>> please look at the available methods in C_buffer
                System.out.println("The IP and Port inside the Buffer: ");
                buffer.show();

                System.out.println("C:mutex   Buffer size is " + buffer.size());

                // if the buffer is not empty
                // This will stop the thread if buffer empty so no if checks


                // >>>   Getting the first (FIFO) node that is waiting for a TOKEN form the buffer
                //       Type conversions may be needed.

                n_host = (String) buffer.get();
                n_port = Integer.parseInt((String) buffer.get());


                // >>>  **** Granting the token
                //
                try {
                    // This will create a socket connection to the listening node
                    s = new Socket(n_host, n_port);
                    PrintWriter wr = new PrintWriter(s.getOutputStream(), true);
                    wr.println(token);
                    s.close();
                } catch (java.io.IOException e) {
                    System.out.println(e);
                    System.out.println("CRASH Mutex connecting to the node for granting the TOKEN" + e);
                }


                //  >>>  **** Getting the token back
                try {
                    // THIS IS BLOCKING ! Means its a read line
                    Socket listeningSocket = ss_back.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(listeningSocket.getInputStream()));
                    // This will block and check for the token
                    String tokenRecieved = in.readLine();
                    if (!tokenRecieved.equals(token)) {
                        throw new Exception("Token Received is incorrect: " + tokenRecieved);
                    }
                    listeningSocket.close();
                } catch (java.io.IOException e) {
                    System.out.println(e);
                    System.out.println("CRASH Mutex waiting for the TOKEN back" + e);
                }

            }// endwhile
        } catch (Exception e) {
            System.out.print(e);
        }

    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
