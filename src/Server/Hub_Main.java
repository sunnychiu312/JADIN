import java.io.IOException;

public class Hub_Main {
    public static void main(String [] args) throws IOException{
      String address = "127.0.0.1";
      int port = 9090;
      Hub hub1 = new Hub();
      System.out.print(hub1.UDP_PingTime(address, port));
    }
}
