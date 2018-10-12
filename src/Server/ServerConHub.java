import java.io.IOException;

public class ServerConHub{
    public static void main(String [] args) throws IOException, InterruptedException{
      String address = "127.0.0.1";
      int port = 9090;
      Server serv = new Server(address, port, "0");
      //serv.connectToServers("127.0.0.1",  9090); //hub id

    }
}
