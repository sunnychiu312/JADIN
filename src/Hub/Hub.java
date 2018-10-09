package Hub;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.net.ConnectException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Hub{
    ServerSocket ingress_srv_sock;
    String [] in_ip_port;
    String [] e_ip_port;

    public Hub(HashMap<String, String[]> routeMapping) throws IOException{
        in_ip_port = routeMapping.get("I");
        e_ip_port = routeMapping.get("E");
        create_ingress_socket(in_ip_port[0], Integer.valueOf(in_ip_port[1]));
        newClientConnection();
    }

    public void create_ingress_socket(String address, int port) throws IOException{
        InetAddress server_address;
        try {
            server_address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            System.exit(1);
            return;
        }
        ingress_srv_sock = new ServerSocket(port, 16, server_address);
    }

    public Socket create_mirror_client(String address, int port) throws IOException{
        InetAddress server_address;
        InetSocketAddress endpoint;

        server_address = InetAddress.getByName(address);
        endpoint = new InetSocketAddress(server_address, port);
        Socket mirror_client = new Socket();

        try {
            mirror_client.connect(endpoint);
        } catch(ConnectException e) {
            System.err.println("Cannot connect to server.");
            System.exit(1);
        }
        return mirror_client;
    }

    public void newClientConnection() throws IOException{
        while(true){
            Socket ingress_sock = ingress_srv_sock.accept();
            String client_address = ingress_sock.getRemoteSocketAddress().toString();
            Socket mirror_client1 = create_mirror_client(e_ip_port[0], Integer.valueOf(e_ip_port[1]));
            //TODO: Need new configuration java methods for new egress DB servers
            // use a for loop to iterate through list of egresses to make tcp connections
            // Idea is to send query worker threads through TCP connections
            Socket mirror_client2 = create_mirror_client(e_ip_port[0], Integer.valueOf(e_ip_port[1])); //2nd server
            aduitConOpen(client_address);
            createComThreads(ingress_sock, mirror_client1, client_address);
            createComThreads(ingress_sock, mirror_client2, client_address);

        }
    }

    public void createComThreads(Socket ingress_sock, Socket mirror_client, String client_address) {
        PassAlongThread send = new PassAlongThread(ingress_sock, mirror_client, client_address, "Client to Server");
        PassAlongThread recv = new PassAlongThread(mirror_client, ingress_sock, client_address, "Server to Client");
        send.start();
        recv.start();
    }

    public void aduitConOpen(String client_address){
        OffsetDateTime connect_time = OffsetDateTime.now(ZoneOffset.UTC);
        System.out.println("Connection Time: "+ connect_time + "\n" + "Client IP/Port: " +
                client_address + "\n" + "Server IP/Port: " +
                e_ip_port[0] + ":" +e_ip_port[1]);
    }
}


