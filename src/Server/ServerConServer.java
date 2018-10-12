import java.io.IOException;
import java.net.Socket;


public class ServerConServer{
    public static void main(String [] args) throws IOException, InterruptedException{
      String address = "127.0.0.1";
      int port = 9095;
      Server serv = new Server(address, port, "1");
      serv.connectToServers("127.0.0.1",  9090);

    }
}
