package Hub;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import java.util.Arrays;

public class Configurator{
  private HashMap< String, String [] > routeMapping;
  public Configurator(String file, String alias){
    readConfig(file, alias);
  }

  public HashMap< String, String [] > readConfig(String config_file, String alias){
   File file = new File(config_file);
  routeMapping = new HashMap<>();
   Boolean iflag = false;
   Boolean eflag = false;
   try {
       Scanner sc = new Scanner(file);
       while (sc.hasNextLine()) {
           String i = sc.nextLine();
           String [] split_line = i.split(" ");
           if(i.isEmpty()){
             continue;
           }
           else{
             String config_alias = split_line[1];
             if(!(config_alias.equals(config_alias.toLowerCase()))){
               System.out.println("Incorrect alias information");
               System.exit(1);
             }
             if(alias.equals(config_alias)){
               if(split_line.length != 4){
                 System.out.println("Config file incomplete");
                 System.exit(1);
               }
               String eOri = split_line[0];
               String ip = split_line[2];
               String port = split_line[3];
               check_rex(eOri , "E|I");
               check_rex(ip, "(\\d+).(\\d+).(\\d+).(\\d+)");
               check_rex(port, "(\\d+)");

               String [] ip_port = new String [2];
               ip_port[0] = ip;
               ip_port[1] = port;
               routeMapping.put(eOri,ip_port);
               System.out.println(i);
             }
           }
       }
       sc.close();
   }
   catch (FileNotFoundException e) {
       e.printStackTrace();
   }

   if (!(routeMapping.containsKey("E") & routeMapping.containsKey("I"))){
     System.out.println("Either Ingress or Egress directions not found");
     System.exit(1);
   }
   return routeMapping;
  }

  public static void check_rex(String value, String pattern){
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(value);
    if (!m.find( )) {
       System.out.println("Incomplete config file");
       System.exit(1);
    }
  }

  public HashMap< String, String []> get_routeMapping(){
    return routeMapping;
  }

}
