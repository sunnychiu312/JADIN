import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

public class UdpPingListen extends Thread{
  private InetAddress server_address;
  private int port;

  public UdpPingListen(InetAddress server_address, int port){
    this.server_address = server_address;
    this.port = port;
  }
  public void UDP_PingRespond() throws IOException {
      String null_mesg = "\0";
      DatagramSocket sock;
      byte[] rbuf = new byte[1];
      DatagramPacket packet = new DatagramPacket(rbuf,1);

      // Make the socket
      try {
          sock = new DatagramSocket(port, server_address);
      } catch(SocketException e) {
          System.err.println(" Server Cannot create the socket.");
          System.exit(1);
          return;
      }

      while(true) {
          sock.receive(packet);
          String recv = new String(packet.getData(), "US-ASCII");
          if(recv.equals(null_mesg)) {
              packet = new DatagramPacket(null_mesg.getBytes("US-ASCII"),1,packet.getAddress(),packet.getPort());
              sock.send(packet);
          }
      }
  }

  public void run(){
    try{
      UDP_PingRespond();
    }
    catch(IOException e){
    }

  }
}
