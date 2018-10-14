import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Scanner;

@SuppressWarnings("unchecked")

public class Input_JSON{
  public static void main(String[] args){
    // predefined standard input object
    Scanner sc = new Scanner(System.in);
    System.out.print("Please input read or write: ");

    String type = sc.nextLine().toLowerCase();
    while(!(type.equals("read") | type.equals("write"))){
      System.out.print("Please input read or write: ");
      type = sc.nextLine().toLowerCase();
    }

    if(type.equals("write")){
      System.out.print("Please input filename: ");
      String filename = sc.nextLine();
      JSONObject obj = new JSONObject();

      while(true){
        System.out.print("Please input key: ");
        String key = sc.nextLine();
        if(key.equals("WRITE DONE")){
          break;
        }
        System.out.print("Please input key's value: ");
        String value = sc.nextLine();
        obj.put(key,value);
        System.out.println("------When finished adding to file, please input WRITE DONE------");
      }
      System.out.println(obj.toJSONString());
    }

    else{
      System.out.print("Please input filename: ");
      String filename = sc.nextLine();
      System.out.println(filename);
    }
  }
}

/*

read file count how many fails
number of fails = number of duplicates 
*/
