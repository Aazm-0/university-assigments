import java.net.*;
import java.io.*;
import java.util.*;

public class Node {

    private Random ra;
    private Socket s;
    private PrintWriter pout = null;
    private ServerSocket n_ss;
    private Socket n_token;
    String c_host = "127.0.0.1";
    int c_request_port = 7000;
    int c_return_port = 7001;
    String n_host = "127.0.0.1";
    String n_host_name;
    int n_port;

    // Also adding the return port socket
    private Socket rs;
    private PrintWriter to;

    public Node(String nam, int por, int sec) {
        ra = new Random();
        n_host_name = nam;
        n_port = por;

        System.out.println("Node " + n_host_name + ":" + n_port + " of DME is active ....");

        // NODE sends n_host and n_port  through a socket s to the coordinator
        // c_host:c_req_port
        // and immediately opens a server socket through which will receive
        // a TOKEN (actually just a synchronization).

        // Now i need to open a server socket which will receive the token
        try {
            n_ss = new ServerSocket(n_port);
        } catch (IOException e) {
            System.out.println("Error in starting token listening server" + e.getMessage());
            return;
        }

        while (true) {
            sleep((ra.nextInt(sec) + 1));
            try {
                // The socket that will emit the port and ip to listener
                s = new Socket(c_host, c_request_port);
                pout = new PrintWriter(s.getOutputStream(), true);
                System.out.println("Node " + n_host_name + ":" + n_port + ": Requesting token");
                // **** Send to the coordinator a token request.
                // send your ip address and port number
                pout.println(n_host);
                pout.println(n_port);
                s.close();

                // **** Then Wait for the token
                // Print suitable messages
                n_token = n_ss.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(n_token.getInputStream()));
                String tokenReceived = in.readLine();
                if (tokenReceived != null) {
                    System.out.println("Node " + n_host_name + ":" + n_port + ": Token has been received");
                }
                n_token.close();



                // **** Sleep for a while
                // This is the critical session
                System.out.println("Node " + n_host_name + ":" + n_port + ": Entering critical section");
                // >>>  sleep a random number of seconds linked to the initialisation sec value
                sleep((ra.nextInt(4) + 1) * 1000); // 3-5 sec sleep simulated
                System.out.println("Node " + n_host_name + ":" + n_port + ": Leaving critical section");


                // **** Return the token
                // Print suitable messages - also considering communication failures
                rs = new Socket(c_host, c_return_port);
                to = new PrintWriter(rs.getOutputStream(), true);
                to.println(tokenReceived);
                rs.close();
                System.out.println("Node " + n_host_name + ":" + n_port + ": Token returned");

            } catch (IOException e) {
                System.out.println(e);
                System.exit(1);
            }
        }
    }

    private  void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Thread sleep interrupted: " + e.getMessage());
        }
    }

    public static void main(String args[]) {
        String n_host_name = "";
        int n_port;

        // port and millisec (average waiting time) are specific of a node
        if ((args.length < 1) || (args.length > 2)) {
            System.out.print("Usage: Node [port number] [millisecs]");
            System.exit(1);
        }

        // get the IP address and the port number of the node
        try {
            InetAddress n_inet_address = InetAddress.getLocalHost();
            n_host_name = n_inet_address.getHostName();
            System.out.println("node hostname is " + n_host_name + ":" + n_inet_address);
        } catch (java.net.UnknownHostException e) {
            System.out.println(e);
            System.exit(1);
        }

        n_port = Integer.parseInt(args[0]);
        System.out.println("node port is " + n_port);
        Node n = new Node(n_host_name, n_port, Integer.parseInt(args[1]));
    }


}
