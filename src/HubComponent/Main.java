
import java.io.IOException;

public class Main{
  public static void main(String[] args) throws IOException, InterruptedException {
    if(args.length!=2) {
        System.out.println("Input file name and alias");
        System.exit(1);
    }
    String alias = args[1];
    String file_name = args[0];
    Hub myHub = new Hub(file_name, alias);
  }
}
