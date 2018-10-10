import java.io.IOException;

public class ServerConHub{
    public static void main(String [] args) throws IOException{
      String address = "127.0.0.1";
      int port = 9090;
      Server serv = new Server(address, port);
      serv.newClientConnection();
      //serv.UDP_PingRespond();

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
