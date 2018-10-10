import java.io.IOException;
import java.net.Socket;


public class ServerConServer{
    public static void main(String [] args) throws IOException{
      String address = "127.0.0.1";
      int port = 9095;
      Server serv = new Server(address, port);
      Socket sock = serv.create_server_client("127.0.0.1",  9090);
      serv.getRoutingTable(sock);
      //serv1.UDP_PingRespond();

      /*

      hub connects to new server
      create tcp server
      hub connect to tcp server
      output send serv_accept
      create udp server

      new server connect to old server
      create tcp client and connect tcp client to other old server
      create udp server
      old server send new updated routing table
      recive serv_routing table
      close client sock
      save recieved routing table
      if new address on routing table excluding current and new, connect to each
      one with serv_new
      update routing table

      */
    }
}
