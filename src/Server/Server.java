
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
import java.rmi.ConnectException;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Server {
    private String null_mesg = "\0";
    private String address;
    private int port;
    private InetAddress server_address;
    private ServerSocket tcp_server_sock;
    private Socket incoming_client;
    private HashMap<String, Long> routing_table = new HashMap<String, Long>(); //treemap instead

    public Server(String address, int port) throws IOException{ //switch cases to connect hub or other server first
      this.address = address;
      this.port = port;

      String key = address + ":" + String.valueOf(port);
      routing_table.put(key,Long.valueOf(0));

      // Setup the server side connection data
      try {
          server_address = InetAddress.getByName(address);
      } catch (UnknownHostException e) {
          System.err.println("Bad server address.");
          System.exit(1);
          return;
      }

      //UDP_PingRespond(); //create UDP server and listens/responds
      create_tcp_server(); //create TCP server
      //newClientConnection(); //listens for response
    }

    public void updateRouteTable(String address, int port) throws IOException{
      //Long ping = UDP_PingTime( address,  port); //need different thread
      String key = address + ":" + String.valueOf(port);
      //routing_table.put(key,ping);
      routing_table.put(key,Long.valueOf(0));
    }

    public void UDP_PingRespond() throws IOException {
        DatagramSocket sock;
        byte[] rbuf = new byte[1];
        DatagramPacket packet = new DatagramPacket(rbuf,1);

        // Make the socket
        try {
            sock = new DatagramSocket(port, server_address);
        } catch(SocketException e) {
            System.err.println(" Server Cannot create the socket.");
            System.exit(1);
            return;
        }

        // Read forever
        while(true) {
            // Read from the socket
            sock.receive(packet);
            String recv = new String(packet.getData(), "US-ASCII");
            if(recv.equals(null_mesg)) {
                System.out.println("received");
                packet = new DatagramPacket(null_mesg.getBytes("US-ASCII"),1,packet.getAddress(),packet.getPort());
                sock.send(packet);
            }
        }
    }

    //other check over servers ip address's ping when master until then need table


    public Long UDP_PingTime(String out_address, int out_port) throws IOException, UnsupportedEncodingException {
        DatagramSocket sock;
        InetAddress outserver_address;

        // Setup the server side connection data
        try {
            outserver_address = InetAddress.getByName(out_address);
        } catch (UnknownHostException e) {
            System.err.println("Bad server address.");
            //System.exit(1);
            return null;
        }

        // Make the socket, with a 1 second timeout
        try {
            sock = new DatagramSocket();
            sock.setSoTimeout(1000);
        } catch(SocketException e) {
            System.err.println("hub Cannot create the socket.");
            //System.exit(1);
            return null;
        }

        // Prepare the null message
        DatagramPacket packet = new DatagramPacket(null_mesg.getBytes("US-ASCII"),1,server_address,out_port);
        int capacity = 3;
        long sum = Long.valueOf(0);
        int counter = 0;
        while(counter != capacity){
            Long time = pingTime(sock, packet);
            if(time > Long.valueOf(0)){
                sum += time;
                counter ++;
            }
        }
        return sum / Long.valueOf(capacity);
    }

    public Long pingTime(DatagramSocket sock, DatagramPacket packet) throws IOException{
        Long startTime = System.nanoTime();
        sock.send(packet);

        // Wait up to 1 second for null message
        Long endTime = Long.valueOf(0);
        int retries=0;
        while( retries<4 ) {
            try {
                sock.receive(packet);
                endTime = System.nanoTime();
                break;
            } catch(SocketTimeoutException ste) {
                // packet was probably lost, retry
                sock.send(packet);
                retries++;
            }
        }
        if(retries>3) {
            System.err.println("Server does not exist");
            //TODO: server does not exist should not exit 
            sock.close();
            System.exit(1);
        }

        // Check the response string
        String pong = new String(packet.getData(), "US-ASCII");

        // Check that the response is expected
        if(pong.equals(null_mesg)) {
            return endTime - startTime;
        }
        return null;
    }

    public void create_tcp_server() throws IOException{
      tcp_server_sock = new ServerSocket(port, 16, server_address);
    }

    public Socket create_server_client(String out_address, int out_port) throws UnsupportedEncodingException, IOException{
      InetAddress out_server_address;
      InetSocketAddress endpoint;
      out_server_address = InetAddress.getByName(out_address);
      endpoint = new InetSocketAddress(server_address, out_port);
      Socket server_client = new Socket();
      try {
          server_client.connect(endpoint);
      } catch(ConnectException e) {
          System.err.println("Cannot connect to server.");
          System.exit(1);
      }

      return server_client;
    }

    public void getRoutingTable(Socket server_client) throws IOException{
      String con_accept = "SERV";
      byte [] encode = con_accept.getBytes("US-ASCII");
      System.out.println(encode.length);
      server_client.getOutputStream().write(encode,0,encode.length);

      while(true){
        int byte_size = 1024;
        byte[] rbuf = new byte[byte_size];
        int data_length = server_client.getInputStream().read(rbuf);
        //{127.0.0.1:9090=0, 127.0.0.1:61960=0}
        if(data_length > 0){
          String str = new String(rbuf, "US-ASCII");
          System.out.println("server_client: " + str);

          HashMap<String, String> map = (HashMap<String, String>) Arrays.asList(str.split(",")).stream().map(s -> s.split("=")).collect(Collectors.toMap(e -> e[0], e -> e[1]));
          String table = map.toString();
          System.out.println(map);
        }
      }
    }

    //when hub connects to tcp server makes server client to send message to correct server
    //to connect to old code need server cilent

    public void newClientConnection() throws IOException{
      while(true){
        Socket server_sock = tcp_server_sock.accept();
        String incoming_adr = server_sock.getRemoteSocketAddress().toString();
        System.out.println(incoming_adr);
        String type = readInputStream(server_sock, 4);
        System.out.println(type);
        byte [] encode;
        switch (type) {
        case "CONN": //confirms address with HUB
            String con_accept = "ACPT";
            encode = con_accept.getBytes("US-ASCII");
            server_sock.getOutputStream().write(encode,0,encode.length);
            break;
        case "SERV": //Sends back routing table to new server
            String [] out_ip_port = incoming_adr.split(":"); //need server ip, not client
            updateRouteTable(out_ip_port[0].substring(1), Integer.valueOf(out_ip_port[1]));
            String table = routing_table.toString();
            encode = table.getBytes("US-ASCII");
            server_sock.getOutputStream().write(encode,0,encode.length);
            break;
        default:
            break;
        }

        //Socket incoming_client = create_server_client(e_ip_port[0], Integer.valueOf(e_ip_port[1]));
        //PassAlongThread send = new PassAlongThread(server_sock, incoming_client, incoming_adr, "Client to Server");
        //PassAlongThread recv = new PassAlongThread(incoming_client, server_sock, incoming_adr, "Server to Client");
        //send.start();
        //recv.start();
      }
    }

    public String readInputStream(Socket client, int bytes) throws IOException{
      byte[] content_rbuf = new byte[bytes];
      int data_length = client.getInputStream().read(content_rbuf);
      String content = new String(content_rbuf, "US-ASCII");
      return content;
    }
}
