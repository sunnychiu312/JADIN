import java.io.IOException;
import java.net.Socket;


public class ServerConServer2{
    public static void main(String [] args) throws IOException, InterruptedException{
      String address = "127.0.0.1";
      int port = 9100;
      Server serv = new Server(address, port, "2");
      serv.connectToServers("127.0.0.1",  9095);

    }
}
