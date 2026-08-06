import java.io.IOException;
import java.net.*;

// Makes a server socket
// Spawn a connection thread for each connection
public class C_receiver extends Thread {

    private C_buffer buffer;
    private int port;
    private ServerSocket s_socket;
    private Socket socketFromNode;
    private C_Connection_r connect;

    public C_receiver(C_buffer b, int p) {
        buffer = b;
        port = p;
    }

    public void run() {
        // >>> create the socket the server will listen to
        try {
            s_socket = new ServerSocket(port);
        }catch (IOException e){
            System.out.println("Failed to create a server socket: " + e);
            return;
        }

        try {

            while (!s_socket.isClosed()) {

                // >>> get a new connection
                socketFromNode = s_socket.accept();
                System.out.println("C:receiver    Coordinator has received a request ...");

                // >>> create a separate thread to service the request, a C_Connection_r thread.
                C_Connection_r conn = new C_Connection_r(socketFromNode, buffer);
                conn.start();
            }
        } catch (java.io.IOException e) {
            System.out.println("Exception when creating a connection " + e);
        }

    }//end run


}
