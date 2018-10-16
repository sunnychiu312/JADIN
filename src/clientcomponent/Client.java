import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class Client{
  private String key;
  private ArrayList <String> hub_address;
  private String content;

  public Client( String key) throws
  IOException{
    this.key = key;
    hub_address = new ArrayList <String>();
    get_hub_adr();
    start();
  }

  public void get_hub_adr(){
    String file_name = "hub_adr.txt";
    try{
      File file = new File(file_name);
      Scanner sc = new Scanner(file);
      while (sc.hasNextLine()){
        hub_address.add(sc.nextLine());
      }
    }
    catch(FileNotFoundException e){
      System.out.println("Basic Hub Address not found");
    }

  }

  public void get_user_inputs(){
    Input_Command user_input = new Input_Command();
    content = user_input.input_command(key);
  }

  public void start() throws IOException{
    while(true){
      get_user_inputs();
      if(content.toLowerCase().equals("quit")){
        System.exit(1);
      }
      Create_TCP_Con tcp_con = new Create_TCP_Con(content);
      for(String adr: hub_address){
        String [] ip_port = adr.split(":");
        if(tcp_con.TCP_operation(ip_port[0], Integer.valueOf(ip_port[1]))){
          break;
        }
      }
    }
  }

}
