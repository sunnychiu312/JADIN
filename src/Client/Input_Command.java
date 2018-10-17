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

    String type = sc.nextLine().toLowerCase().trim();
    check_quit(type);

    while(!(type.equals("read") | type.equals("write"))){
      System.out.print("Please input read, write: ");
      type = sc.nextLine().toLowerCase().trim();
      check_quit(type);
    }

    if(type.equals("write")){
      System.out.print("Please input filename: ");
      String filename = sc.nextLine().trim();
      JSONObject obj = new JSONObject();

      while(true){
        System.out.print("Please input key: ");
        String key = sc.nextLine().trim();
        check_quit(key);

        if(key.equals("DONE")){
          break;
        }
        System.out.print("Please input key's value: ");
        String value = sc.nextLine().trim();
        check_quit(value);
        obj.put(key,value);
        System.out.println("------When finished adding to file, please input DONE------");
      }
      return "RITE" + server_id + ":" + filename + obj.toJSONString();
    }
    else{
      System.out.print("Please input filename: ");
      String filename = sc.nextLine().trim();
      check_quit(filename);

      return "READ" + server_id + ":" + filename;
    }
  }

  public void check_quit(String msg){
    if(msg.toLowerCase().equals("quit")){
      System.exit(1);
    }
  }
}
