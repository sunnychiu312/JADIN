import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.concurrent.ConcurrentHashMap;

public class UdpPingSend extends Thread{
  private String null_mesg = "\0";
  private String out_address;
  private int out_port;
  private ConcurrentHashMap<Long, String[]> routing_table;


  public UdpPingSend(String out_address, int out_port, ConcurrentHashMap<Long, String[]> routing_table){
    this.out_address = out_address.trim();
    this.out_port = out_port;
    this.routing_table = routing_table;
  }

  public Long UDP_PingTime() throws IOException, UnsupportedEncodingException {
      DatagramSocket sock;
      InetAddress out_server_address;

      // Setup the server side connection data
      try {
          out_server_address = InetAddress.getByName(out_address);
      } catch (UnknownHostException e) {
          System.err.println("Bad server address " + out_address + ":" + out_port);
          return null;
      }

      try {
          sock = new DatagramSocket();
          sock.setSoTimeout(1000);
      } catch(SocketException e) {
          System.err.println("hub Cannot create the socket.");
          return null;
      }

      DatagramPacket packet = new DatagramPacket(null_mesg.getBytes("US-ASCII"),1,out_server_address,out_port);
      int capacity = 3;
      long sum = Long.valueOf(0);
      int counter = 0;
      while(counter != capacity){
          Long time = pingTime(sock, packet);
          if(time > Long.valueOf(0)){
              sum += time;
              counter ++;
          }
      }
      if(sum == Long.valueOf(0)){
        return Long.valueOf(-1);
      }
      return sum / Long.valueOf(capacity);
  }

  public Long pingTime(DatagramSocket sock, DatagramPacket packet) throws IOException{
      Long startTime = System.nanoTime();
      sock.send(packet);

      Long endTime = Long.valueOf(0);
      int retries=0;
      while( retries<4 ) {
          try {
              sock.receive(packet);
              endTime = System.nanoTime();
              break;
          } catch(SocketTimeoutException ste) {
              sock.send(packet);
              retries++;
          }
      }
      if(retries>3) {
          System.err.println("Server does not exist");
          return Long.valueOf(-1);
      }

      String pong = new String(packet.getData(), "US-ASCII");

      if(pong.equals(null_mesg)) {
          return endTime - startTime;
      }
      return Long.valueOf(-1);
  }

  public void run(){
    try{
      Long ping =  UDP_PingTime();
      String[] addr = new String[]{out_address, String.valueOf(out_port)};
      routing_table.put(ping, addr);
    }
    catch(IOException e){
    }
  }
}
