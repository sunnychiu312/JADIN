import java.io.IOException;
import java.net.Socket;


public class ServerConServer2{
    public static void main(String [] args) throws IOException, InterruptedException{
      String address = "127.0.0.1";
      int port = 9100;
      String id = "Server_2";
      Server serv = new Server(address, port, id);
      serv.connectToServers();

    }
}
