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

public class Server {
    private String address;
    private int port;
    private InetAddress server_address;
    private ConcurrentHashMap<Long, String> routing_table = new ConcurrentHashMap<Long, String >(); //treemap instead
    private String id;

    public Server(String address, int port, String id) throws IOException{
      this.address = address;
      this.port = port;
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

      TcpServer tcp_listen = new TcpServer(server_address, port, routing_table, id);
      tcp_listen.start();
    }

    public void connectToServers(String out_address, int out_port) throws InterruptedException{
      UpdateRouting getRouteInfo = new UpdateRouting(server_address, address, port, routing_table, out_address, out_port );
      getRouteInfo.start();
      getRouteInfo.join();
      for(long i: routing_table.keySet()){
        System.out.println("After Con " +i + ":" + routing_table.get(i));
      }
    }
}
