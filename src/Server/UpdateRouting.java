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
import java.net.ConnectException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

//does pass in a ConcurrentHashMap and updating dynamic?
public class UpdateRouting extends Thread{
  private String address;
  private String port;
  private String out_address;
  private int out_port;
  private ConcurrentHashMap<String, Long> old_routes;
  private InetAddress server_address;

  public UpdateRouting(InetAddress server_address, String address, int port, String out_address, int out_port, ConcurrentHashMap<String, Long> old_routes ){
    this.address = address;
    this.port = String.valueOf(port);
    this.out_address = out_address;
    this.out_port = out_port;
    this.old_routes = old_routes;
    this.server_address = server_address;
  }

  public void getRoutingTable(Socket server_client, String out_address, int out_port) throws IOException{
    String new_server = "SERV" + address + ":" + String.valueOf(port);
    byte [] encode = new_server.getBytes("US-ASCII");
    server_client.getOutputStream().write(encode,0,encode.length);

    int byte_size = 1024;
    byte[] rbuf = new byte[byte_size];
    int data_length = server_client.getInputStream().read(rbuf);

    if(data_length > 0){
      String new_routes = new String(rbuf, "US-ASCII");
      String [] parsed_routes = keysFromNewRoute(new_routes);
      compareRoutingTable(parsed_routes, out_address, out_port);
    }
  }

  public String [] keysFromNewRoute(String new_routes){
    new_routes = new_routes.trim();
    new_routes = new_routes.substring(1, new_routes.length() - 1);
    String [] address_ports = new_routes.split(",");
    for(int i = 0; i < address_ports.length; i++){
      String new_address = address_ports[i].split("=")[0].trim();
      address_ports[i] = new_address;
    }
    return address_ports;
  }

  public void compareRoutingTable(String [] new_routes, String out_address, int out_port) throws IOException{
    for(String key: new_routes){
      if(! (old_routes.containsKey(key) | key.equals(address+ ":" +port))){
        String [] out_ip_port = key.trim().split(":");
        String new_address = out_ip_port[0];
        int new_port = Integer.valueOf(out_ip_port[1]);
        UdpPingSend getPing = new UdpPingSend(new_address, new_port, old_routes);
        getPing.run(); //TODO: why do i need .join() in server
        Socket sock = create_server_client(new_address, Integer.valueOf(new_port));
        if(sock != null){
          getRoutingTable(sock, new_address, new_port);
        }
      }
    }
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
        return null;
    }
    return server_client;
  }

  public void run(){
    try{
      Socket server_client = create_server_client(out_address, out_port);
      getRoutingTable(server_client, out_address, out_port);
    }
    catch(IOException e){
    }
  }
}