import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.ConnectException;

public class PassAlongThread extends Thread{
  Socket client;
  Socket server;
  String name;
  String client_address;

  public PassAlongThread(Socket client, Socket server, String client_address, String name){
      this.client = client;
      this.server = server;
      this.name = name;
      this.client_address = client_address;
  }

  public void passAlong() throws IOException{
    while(true){
      try{
        int byte_size = 4;
        byte[] rbuf = new byte[byte_size];
        int data_length = client.getInputStream().read(rbuf);

        String type = new String(rbuf, "US-ASCII");
        if(type.equals("hubb")){
          client.getOutputStream().write(PONG.getBytes("US-ASCII"),0,5);
        }

        if(data_length == -1){
          close_sockets();
          break;
        }
        //server.geserverutputStream().write(rbuf,0, data_length);
      }
      catch(SocketException e){
        close_sockets();
        break;
      }
    }
  }

  public void close_sockets()throws IOException{
    client.close();
    server.close();
    System.out.println(name+" connection with "+ client_address+" closed.");
  }

  public void run(){
    try{
      passAlong();
    }
    catch(IOException e){
    }
  }
}
