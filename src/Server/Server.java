import java.net.InetAddress;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private String address;
    private int port;
    private InetAddress server_address;
    private String server_id;
    private ConcurrentHashMap<Long, String> routing_table = new ConcurrentHashMap<Long, String >(); //treemap instead

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

    public void connectToServers(String out_address, int out_port) throws IOException, InterruptedException{
      UpdateRouting getRouteInfo = new UpdateRouting(server_address, address, port, routing_table, out_address, out_port );
      getRouteInfo.start();
      for(long i: routing_table.keySet()){
        System.out.println("Updated Routing Table: " +i + ":" + routing_table.get(i));
      }
    }
}
