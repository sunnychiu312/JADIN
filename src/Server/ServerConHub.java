import java.io.IOException;

public class ServerConHub{
    public static void main(String [] args) throws IOException, InterruptedException{
      String address = "127.0.0.1";
      int port = 9090;
      String id = "Server_0";
      Server serv = new Server(address, port, id);

    }
}
