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
  private Socket server_client;
  private String address;
  private String port;
  private String out_address;
  private int out_port;
  private ConcurrentHashMap<String, Long> old_routes;

  public UpdateRouting(String address, int port, Socket server_client, String out_address, int out_port, ConcurrentHashMap<String, Long> old_routes ){
    this.address = address;
    this.port = String.valueOf(port);
    this.server_client = server_client;
    this.out_address = out_address;
    this.out_port = out_port;
    this.old_routes = old_routes;
  }

  public void getRoutingTable(Socket server_client, String out_address, int out_port) throws IOException{
    String new_server = "SERV" + address + ":" + String.valueOf(port);
    byte [] encode = new_server.getBytes("US-ASCII");
    System.out.println("encode length: " +encode.length);
    server_client.getOutputStream().write(encode,0,encode.length);


    int byte_size = 1024;
    byte[] rbuf = new byte[byte_size];
    int data_length = server_client.getInputStream().read(rbuf);
    if(data_length > 0){
      String str = new String(rbuf, "US-ASCII");
      str = str.trim();

      str = str.substring(1, str.length() - 1);
      System.out.println("server_client: " + str);

      ConcurrentHashMap<String, Long> new_routes = (ConcurrentHashMap<String, Long>) Arrays.asList(str.split(",")).stream().map(s -> s.split("=")).collect(Collectors.toMap(e -> e[0], e -> Long.parseLong(e[1])));

      System.out.println(new_routes);
      compareRoutingTable(new_routes, out_address, out_port);
    }

  }

  public void compareRoutingTable(ConcurrentHashMap<String, Long> new_routes, String out_address, int out_port) throws IOException{
    for(String key: new_routes.keySet()){
      String new_owner = out_address + ":" + String.valueOf(out_port);
      key = key.trim();
      if(! (old_routes.containsKey(key) | key.equals(new_owner) | key.equals(address_port))){
        String [] out_ip_port = key.trim().split(":");
        String new_address = out_ip_port[0];
        int new_port = Integer.valueOf(out_ip_port[1]);
        updateRouteTable(new_address, new_port);
        Socket sock = create_server_client(new_address, Integer.valueOf(new_port));
        if(sock != null){
          getRoutingTable(sock, new_address, new_port);
        }
      }
    }
  }

  public void updateRouteTable(String address, int port) throws IOException{
    UdpPingSend getPing = new UdpPingSend(address, port);
    Long ping = getPing.run();
    String key = address + ":" + String.valueOf(port);
    old_routes.put(key,ping);
    //old_routes.put(key,Long.valueOf(0));
    System.out.print("updated routing table: " + old_routes);
  }

  public void run(){
    try{
      getRoutingTable(server_client, out_address, out_port);
    }
    catch(IOException e){
    }

  }
}
