import java.io.File;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
@SuppressWarnings("unchecked")

public class Input_Command{

  public String input_command(String server_id){
    Scanner sc = new Scanner(System.in);
    System.out.print("Please input read, write, or quit: ");

    String type = sc.nextLine().toLowerCase();
    check_quit(type);

    while(!(type.equals("read") | type.equals("write"))){
      System.out.print("Please input read, write");
      type = sc.nextLine().toLowerCase();
      check_quit(type);

    }

    if(type.equals("write")){
      System.out.print("Please input filename: ");
      String filename = sc.nextLine();
      JSONObject obj = new JSONObject();

      while(true){
        System.out.print("Please input key: ");
        String key = sc.nextLine();
        check_quit(key);

        if(key.equals("WRITE DONE")){
          break;
        }
        System.out.print("Please input key's value: ");
        String value = sc.nextLine();
        check_quit(value);
        obj.put(key,value);
        System.out.println("------When finished adding to file, please input WRITE DONE------");
      }
      System.out.println("RITE" + server_id + ":"+ filename + obj.toJSONString());
      return "RITE" + server_id + ":" + filename + obj.toJSONString();
    }
    else{
      System.out.print("Please input filename: ");
      String filename = sc.nextLine();
      check_quit(filename);
      System.out.println("READ" + server_id + ":" + filename);

      return "READ" + server_id + ":" + filename;
    }
  }

  public void check_quit(String msg){
    if(msg.toLowerCase().equals("quit")){
      System.exit(1);
    }
  }
}
