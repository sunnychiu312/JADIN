import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class Create_TCP_Con{
  private String msg;

  public Create_TCP_Con(String msg){
    this.msg = msg;
  }


  public boolean TCP_operation(String address, int port) throws IOException {
      Socket sock;
      InetAddress server_address;
      InetSocketAddress endpoint;

      server_address = InetAddress.getByName(address);
      endpoint = new InetSocketAddress(server_address, port);

      //// Make the TCP connection
      try {
          sock = new Socket();
          sock.setSoTimeout(5000);
      } catch(SocketException e) {
          System.err.println("Cannot create the socket.");
          return false;
      }

      // Make the connection
      try {
          sock.connect(endpoint, 0);
      } catch(ConnectException e) {
          System.err.println("Cannot connect to server.");
          return false;
      }

      byte [] encode = msg.getBytes("US-ASCII");

      sock.getOutputStream().write(encode,0,encode.length);
      try{
        byte[] type_rbuf = new byte[4];
        int data_length = sock.getInputStream().read(type_rbuf);
        String type = new String(type_rbuf, "US-ASCII");

        switch (type) {
          case "RITE":
            System.out.println("Database completed WRITE request");
            return true;

          case "READ":
            byte[] read_rbuf = new byte[1024];
            data_length = sock.getInputStream().read(read_rbuf);
            String content = new String(read_rbuf, "US-ASCII");
            System.out.println("Database completed READ request. Results: " + content);
            return true;

          default:
            System.out.println("Database unable to fulfill request");
            return false;
        }

      }catch(SocketTimeoutException e){
        return false;
      }

    }





}
