import java.io.*;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.ConnectException;

public class PassAlongThread extends Thread {

  Socket to;
  int choice;

  public PassAlongThread(Socket to, int _choice) { //String client_address, String name){
    choice = _choice;
    this.to = to;

  }
//      this.name = name;
//      this.client_address = client_address;


  public void passAlong_recv() throws IOException {
    while (true) {
      try {
        InputStream inputStream = to.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while((line = bufferedReader.readLine()) != null){
          System.out.println(line);
//        int byte_size = 1024;
//        byte[] rbuf = new byte[byte_size];
//
////        System.out.print("FUDGE");
//        int data_length = to.getInputStream().read(rbuf);
//        String helo = new String(rbuf, "US-ASCII");
//
//        if (data_length == -1) {
//          close_sockets();
//          break;
        }

      } catch (SocketException e) {
        close_sockets();
        break;
      }
    }
  }

  public void passAlong_send() throws IOException {

      try {
        OutputStream outputStream = to.getOutputStream();
        PrintWriter printwriter = new PrintWriter(outputStream, true);
        String login_msgg = "LOGN";
        printwriter.println(login_msgg); // send login to server to begin user authenticication
        for (int i = 0;i<16;i++ ){
          String poop = "my username is bob"+ i + "my password is *****";
          printwriter.println(poop);}
//        int byte_size = 1024;
//        byte[] rbuf = new byte[byte_size];
//
//        String helo = new String(rbuf, "US-ASCII");
//
//        if (data_length == -1) {
//          close_sockets();
//          break;
//        }
//        to.getOutputStream().write(rbuf, 0, data_length); //
      } catch (SocketException e) {
//        close_sockets();

      }
    }
  public void passAlong_both() throws IOException {

    try {
      OutputStream outputStream = to.getOutputStream();
      PrintWriter printwriter = new PrintWriter(outputStream, true);
      String login_msgg = "LOGN";
      printwriter.println(login_msgg); // send login to server to begin user authenticication
      InputStream inputStream = to.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String line;
//      while((line = bufferedReader.readLine()) != null){ // if the line is not null print whats readline()
//        System.out.println(line);}

      line = bufferedReader.readLine();
      if (line.contentEquals("LOGN")){ // need to form logic of what i will be receiving back from the hub
        printwriter.println("OKOK");
      }




    } catch (SocketException e) {
//        close_sockets();

    }
  }


  public void close_sockets() throws IOException {

    to.close();
    System.out.println(" connection with closed.");
  }

  public void run() {
    if (choice == 1) {
      try {
        passAlong_recv();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else if (choice==2) {
      try {
        passAlong_send();
      } catch (IOException e) {
        e.printStackTrace();
      }}
      else if (choice==3) {
        try {
          passAlong_both();
        } catch (IOException e) {
          e.printStackTrace();
        }

    }
  }
}