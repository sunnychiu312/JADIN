import java.net.InetAddress;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.net.SocketException;
import java.net.ConnectException;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;

public class Server {
    private String address;
    private int port;
    private InetAddress server_address;
    private String server_id;
    private ConcurrentHashMap<Long, String> routing_table = new ConcurrentHashMap<Long, String >(); //treemap instead
    private ArrayList <String> server_con_address;
    private Socket server_client;

    public Server(String address, int port, String server_id) throws IOException{
      this.address = address;
      this.port = port;
      this.server_id = server_id;
      String key = address + ":" + String.valueOf(port);
      routing_table.put(Long.valueOf(0),key);

      try {
          server_address = InetAddress.getByName(address);
      } catch (UnknownHostException e) {
          System.err.println("Bad server address.");
          System.exit(1);
          return;
      }

      UdpPingListen ping_listen = new UdpPingListen(server_address, port);
      ping_listen.start();

      TcpServer tcp_listen = new TcpServer(server_address, port, routing_table, server_id);
      tcp_listen.start();
    }

    public void get_server_adr(){
    String file_name = "server_address.conf";
    server_con_address = new ArrayList <String>();
    try{
      File file = new File(file_name);
      Scanner sc = new Scanner(file);
      while (sc.hasNextLine()){
        server_con_address.add(sc.nextLine());
      }
    }
    catch(FileNotFoundException e){
      System.out.println("Basic Server Addresses not found");
    }
   }

   public Boolean create_server_client(String out_address, int out_port) throws IOException{
     InetAddress out_server_address;
     InetSocketAddress endpoint;
     out_server_address = InetAddress.getByName(out_address);
     endpoint = new InetSocketAddress(out_server_address, out_port);
    server_client = new Socket();
     try {
         server_client.connect(endpoint);
     } catch(ConnectException e) {
         System.err.println("Cannot connect to server.");
         return false;
     }
     return true;
   }

    public void connectToServers() throws IOException, InterruptedException{
      get_server_adr();
      for(String addresses : server_con_address){
        String [] ip_port = addresses.split(":");
        String out_address = ip_port[0];
        int out_port = Integer.valueOf(ip_port[1]);
        if(out_address.equals(address) & out_port == port){
          continue;
        }
        Boolean success = create_server_client( out_address,  out_port);
        if(!success){
          continue;
        }
        UpdateRouting getRouteInfo = new UpdateRouting(server_address, address, port, routing_table, out_address, out_port, server_client);
        getRouteInfo.start();
        for(long i: routing_table.keySet()){
          System.out.println("Updated Routing Table: " +i + ":" + routing_table.get(i));
        }
        break;
      }

    }
}
