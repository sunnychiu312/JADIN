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
    private ConcurrentHashMap<String, Long> routing_table = new ConcurrentHashMap<String, Long>(); //treemap instead

    public Server(String address, int port) throws IOException{
      this.address = address;
      this.port = port;
      String key = address + ":" + String.valueOf(port);
      routing_table.put(key,Long.valueOf(0));

      try {
          server_address = InetAddress.getByName(address);
      } catch (UnknownHostException e) {
          System.err.println("Bad server address.");
          System.exit(1);
          return;
      }

      UdpPingListen ping_listen = new UdpPingListen(server_address, port);
      ping_listen.start();

      TcpServer tcp_listen = new TcpServer(server_address, port, routing_table);
      tcp_listen.start();
    }

    public void connectToServers(String out_address, int out_port) throws InterruptedException{
      UpdateRouting getRouteInfo = new UpdateRouting(server_address, address, port, out_address, out_port, routing_table );
      getRouteInfo.start();
      getRouteInfo.join();
      for(String i: routing_table.keySet()){
        System.out.println("After Con " +i + ":" + routing_table.get(i));
      }
    }
}
