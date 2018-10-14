import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ConcurrentHashMap;

//javac -cp .:json-simple-1.1.1.jar *.java

public class ServerComm{
  private ConcurrentHashMap<String, String > checked_adr;
  private String out_address;
  private int out_port;
  private String content;
  private ConcurrentHashMap<Long, String > routing_table;
  private Socket server_client;

  public ServerComm(ConcurrentHashMap<String, String > checked_adr, String out_address, int out_port, String content, ConcurrentHashMap<Long, String > routing_table){
    this.checked_adr = checked_adr;
    this.out_address = out_address;
    this.out_port = out_port;
    this.content = content;
    this.routing_table = routing_table;
  }

  public boolean create_server_client(String out_address, int out_port) throws IOException{
    InetAddress out_server_address;
    InetSocketAddress endpoint;
    out_server_address = InetAddress.getByName(out_address);
    endpoint = new InetSocketAddress(out_server_address, out_port);
    server_client = new Socket();
    try {
        server_client.connect(endpoint);

    } catch(ConnectException e) {
      routing_table.remove(out_address + ":" + String.valueOf(out_port));
      checked_adr.put(out_address + ":" + String.valueOf(out_port), "FAIL");
      System.err.println("Cannot connect to server.");
      return false;
    }
    return true;
  }

  public void sendAndRead() throws IOException{
    String msg = "COPY" + content;
    byte [] encode = msg.getBytes("US-ASCII");
    server_client.getOutputStream().write(encode,0,encode.length);

    int byte_size = 4;
    byte[] rbuf = new byte[byte_size];
    int data_length = server_client.getInputStream().read(rbuf);
    String content = new String(rbuf, "US-ASCII");
    if(content.equals("true")){
      checked_adr.put(out_address + ":" + String.valueOf(out_port), "DONE");
    }
    else{
      checked_adr.put(out_address + ":" + String.valueOf(out_port), "FAIL");
    }
    server_client.close();
  }

  public void start() throws IOException{
      boolean server_client = create_server_client(out_address, out_port);
      if(server_client){
        sendAndRead();
      }
  }
}
