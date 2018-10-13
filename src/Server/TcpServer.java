import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.parser.ParseException;

//java -cp .:json-simple-1.1.1.jar ServerConHub


public class TcpServer extends Thread{

  private ServerSocket tcp_server_sock;
  private InetAddress server_address;
  private int port;
  private ConcurrentHashMap<Long, String> routing_table;
  private String server_id;

  public TcpServer (InetAddress server_address, int port,  ConcurrentHashMap<Long, String> routing_table, String server_id){
    this.server_address = server_address;
    this.port = port;
    this.routing_table = routing_table;
    this.server_id = server_id;
  }

  public void create_tcp_server() throws IOException{
    tcp_server_sock = new ServerSocket(port, 16, server_address);
  }

  public void newClientConnection() throws IOException{
    while(true){
      Socket acpt_sock = tcp_server_sock.accept();
      String type = readInputStream(acpt_sock, 4);

      System.out.println(type);

      byte [] encode;
      switch (type) {
      case "CHEK":
        String con_accept = "ACPT";
        encode = con_accept.getBytes("US-ASCII");
        acpt_sock.getOutputStream().write(encode,0,encode.length);
        acpt_sock.close();
        break;

      case "SERV":
        String [] out_ip_port = readInputStream(acpt_sock, 14).split(":");
        String new_address = out_ip_port[0].trim();
        String new_port = out_ip_port[1].trim();
        UdpPingSend getPing = new UdpPingSend(new_address, Integer.valueOf(new_port), routing_table);
        getPing.run();
        String table = routing_table.toString();
        encode = table.getBytes("US-ASCII");
        acpt_sock.getOutputStream().write(encode,0,encode.length);
        acpt_sock.close();
        break;

      case "RITE":
        DistributeWrite moreWrites = new DistributeWrite( acpt_sock,  routing_table);
        moreWrites.start();
        break;

      case "COPY":
        WriteFile new_file = new WriteFile(acpt_sock, server_id);
        new_file.start();
        break;

      case "READ":
        ReadFile old_file = new ReadFile(acpt_sock);
        old_file.start();
        break;

      default:
        String invalid_msg = "WRNG";
        encode = invalid_msg.getBytes("US-ASCII");
        acpt_sock.getOutputStream().write(encode,0,encode.length);
        acpt_sock.close();
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
