
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.net.ConnectException;

public class Hub {

    public static void main(String[] args) throws IOException {
        newClientConnection();
    }

    Socket ingress_srv_sock;
    String in_ip_port;
    String e_ip_port;
    String address;


    //    public Hub(HashMap<String, String[]> routeMapping) throws IOException{
////        in_ip_port = "127.0.0.1";
////        e_ip_port = routeMapping.get("E");
//        create_ingress_socket("127.0.0.1", 5056);
//        newClientConnection();
//    }
//
//    public static Socket create_ingress_socket() throws IOException {
//        int port = 5056;
//        ServerSocket ingress_srv_sock = new ServerSocket(port);
//        while (true) {
//            Socket s = null;
//
//            try {
//                // socket object to receive incoming client requests
//                s = ingress_srv_sock.accept();
//
//                System.out.println("A new client is connected : " + s);
//
//            } catch (Exception e) {
//                s.close();
//                e.printStackTrace();
//            }


//        InetAddress server_address;
////        try {
////            server_address = InetAddress.getByName(address);
////        } catch (UnknownHostException e) {
////            System.err.println("Bad server address.");
////            System.exit(1);
////            return;
////        }



    public static Socket create_mirror_client() throws IOException {
//        InetAddress server_address;
//        InetSocketAddress endpoint;
        String host = "127.0.0.1";
        int port = 5056;

//        server_address = InetAddress.getByName(address);
//        endpoint = new InetSocketAddress(server_address, port);
        Socket mirror_client = new Socket();
        System.out.println("connected bruhhhhhhhhh");


        try {
            mirror_client.connect(new InetSocketAddress(host, port));
            System.out.println("connected bruh");
        } catch (ConnectException e) {
            System.err.println("Cannot connect to server.");
            System.exit(1);
        }
        return mirror_client;
    }


    public static void newClientConnection() throws IOException {
        while (true) {
            //Socket ingress_sock = ingress_srv_sock;  //.accept();
            System.out.print("FUCKKK");
//            Socket ingress_sock = create_ingress_socket();
            System.out.print("MOM");

//            String client_address = ingress_sock.getRemoteSocketAddress().toString();
            Socket mirror_client1 = create_mirror_client();
            //TODO: Need new configuration java methods for new egress DB servers
            // use a for loop to iterate through list of egresses to make tcp connections
            // Idea is to send query worker threads through TCP connections
//            Socket mirror_client2 = create_mirror_client(); //2nd server
//            aduitConOpen(client_address);
            createComThreads( mirror_client1);
            createComThreads(mirror_client1,ingress_sock);

        }
    }


    public static void createComThreads(Socket ingress_sock, Socket mirror_client) {
        PassAlongThread send = new PassAlongThread(ingress_sock, mirror_client);
        PassAlongThread recv = new PassAlongThread(mirror_client, ingress_sock);
        send.start();
        recv.start();
    }
}
//
//    public void aduitConOpen(String client_address){
//        OffsetDateTime connect_time = OffsetDateTime.now(ZoneOffset.UTC);
//        System.out.println("Connection Time: "+ connect_time + "\n" + "Client IP/Port: " +
//                client_address + "\n" + "Server IP/Port: " +
//                e_ip_port[0] + ":" +e_ip_port[1]);
//    }


