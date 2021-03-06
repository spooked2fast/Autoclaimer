import org.fusesource.jansi.AnsiConsole;

import java.util.Scanner;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.Color.WHITE;
import static org.fusesource.jansi.Ansi.ansi;

public class Console {
    private char[] spinBuf = {'|' , '/','-','\\'};
    private int spinIndex = 0;
    public Console(){
        AnsiConsole.systemInstall();
    }
    public void updateMessage(String header, String message){
        System.out.print( ansi().fg(WHITE).a("[").fgBrightGreen().a(header).fg(WHITE).a("] " + message));
    }
    public void updateAutoclaimerMessage(int tries, int RL, long time) {
            System.out.print("\r"+ ansi().fg(WHITE).a("[").fgBrightGreen().a("Autoclaimer").fg(WHITE).a("] Requests: (").fg(WHITE).a(tries).fg(WHITE).a(")").fg(WHITE).a(" |  RL: (").fg(WHITE).a(RL).fg(WHITE).a(") | Check Time: ").fg(WHITE).a(time).fg(WHITE).a(" ms"));
    }
    public void updateTokenHeader(int tokens){
        System.out.print("\r"+ ansi().fg(WHITE).a("[").fgBrightGreen().a(spinBuf[spinIndex]).fg(WHITE).a("] Accounts Loaded: (").fg(WHITE).a(tokens).fg(WHITE).a(")"));
        if(spinIndex < 3){
            spinIndex++;
        } else {
            spinIndex = 0;
        }
    }
    public void updateTargetHeader(int targets){
        System.out.print("\r"+ ansi().fg(WHITE).a("[").fgBrightGreen().a(spinBuf[spinIndex]).fg(WHITE).a("] Targets Found: (").fg(WHITE).a(targets).fg(WHITE).a(")"));
        if(spinIndex < 3){
            spinIndex++;
        } else {
            spinIndex = 0;
        }
    }
    public boolean getUserBool(){
        while(true){
            Scanner in = new Scanner(System.in);
            if(in.hasNext()){
                String ans = in.nextLine();
                if(ans.equalsIgnoreCase("y")){
                    return true;
                }
                if(ans.equalsIgnoreCase("n")){
                    return false;
                }
            }
            printError("bruh, enter 'Y' or 'N'");
        }
    }
    public String getUserString(){
        while(true){
            Scanner in = new Scanner(System.in);
            if(in.hasNext()){
                return in.nextLine();
            } else {
                printError("Please enter a valid value.");
            }
        }
    }
    public int getUserInt(){
        int numThreads;
        while(true){
            Scanner in = new Scanner(System.in);
            if(in.hasNextInt()){
                numThreads = in.nextInt();
                return numThreads;
            }
            printError("Enter a valid integer.");
        }
    }
    public void printError(String errorMessage){
        System.out.print(ansi().fg(WHITE).a("[").fgBrightRed().a("!").fg(WHITE).a("] " + errorMessage));
    }
}
