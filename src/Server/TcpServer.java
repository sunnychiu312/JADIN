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


public class TcpServer extends Thread{

  private ServerSocket tcp_server_sock;
  private InetAddress server_address;
  private int port;
  private ConcurrentHashMap<String, Long> routing_table;

  public TcpServer (InetAddress server_address, int port,  ConcurrentHashMap<String, Long> routing_table){
    this.server_address = server_address;
    this.port = port;
    this.routing_table = routing_table;
  }

  public void create_tcp_server() throws IOException{
    tcp_server_sock = new ServerSocket(port, 16, server_address);
  }

  public void newClientConnection() throws IOException{
    while(true){
      Socket server_sock = tcp_server_sock.accept();
      String type = readInputStream(server_sock, 4);

      System.out.println(type);

      byte [] encode;
      switch (type) {
      case "CONN": //confirms address with HUB TODO change "CHEK"
          String con_accept = "ACPT";
          encode = con_accept.getBytes("US-ASCII");
          server_sock.getOutputStream().write(encode,0,encode.length);
          server_sock.close();
          break;
      case "SERV": //Sends back routing table to new server
          String [] out_ip_port = readInputStream(server_sock, 14).split(":");
          String new_address = out_ip_port[0];
          String new_port = out_ip_port[1];
          UdpPingSend getPing = new UdpPingSend(new_address, Integer.valueOf(new_port), routing_table);
          getPing.run();
          String table = routing_table.toString();
          encode = table.getBytes("US-ASCII");
          server_sock.getOutputStream().write(encode,0,encode.length);
          server_sock.close();
          break;
      default:
          break;
      }
    }
  }

  public String readInputStream(Socket client, int bytes) throws IOException{
    byte[] content_rbuf = new byte[bytes];
    int data_length = client.getInputStream().read(content_rbuf);
    String content = new String(content_rbuf, "US-ASCII");
    return content;
  }

  public void run(){
    try{
      create_tcp_server();
      newClientConnection();
    }
    catch(IOException e){
    }
  }
}