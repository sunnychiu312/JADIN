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
import java.util.List;

public class ServerComm extends Thread{
  private List<String> checked_adr;
  private String out_address;
  private int out_port;
  private String content;

  public ServerComm(List<String> checked_adr, String out_address, int out_port, String content){
    this.checked_adr = checked_adr;
    this.out_address = out_address;
    this.out_port = out_port;
    this.content = content;
  }

  public Socket create_server_client(String out_address, int out_port) throws IOException{
    InetAddress out_server_address;
    InetSocketAddress endpoint;
    out_server_address = InetAddress.getByName(out_address);
    endpoint = new InetSocketAddress(out_server_address, out_port);
    Socket server_client = new Socket();
    try {
        server_client.connect(endpoint);
    } catch(ConnectException e) {
        System.err.println("Cannot connect to server.");
        return null;
    }
    return server_client;
  }

  public void sendAndRead(Socket server_client) throws IOException{
    String msg = "COPY" + content;
    byte [] encode = msg.getBytes("US-ASCII");
    server_client.getOutputStream().write(encode,0,encode.length);

    int byte_size = 4;
    byte[] rbuf = new byte[byte_size];
    int data_length = server_client.getInputStream().read(rbuf);
    String content = new String(rbuf, "US-ASCII");
    if(content.equals("DONE")){
      checked_adr.add(out_address + ":" + String.valueOf(out_port));
    }
    server_client.close();
  }

  public void run(){
    try{
      Socket server_client = create_server_client(out_address, out_port);
      sendAndRead(server_client);
    }
    catch(IOException e){}
  }
}
