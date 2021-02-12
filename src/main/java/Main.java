import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        Console console = new Console();
        YamlParser yamlParser = new YamlParser();
        Settings settings = yamlParser.parseYaml("Settings.yaml");
        Mail mail = new Mail(settings);
        Authorization authorization = new Authorization(mail);
        if(! authorization.getIsAuth()){
            console.updateMessage("!", "You are unauthorized to use this software.");
            System.exit(1);
        }
        console.updateMessage("Welcome", "Mafia's Xbox Autoclaimer v1");
        System.out.println("\n");
        AccountCredentials accountCredentials = new AccountCredentials(settings);
        AuthThreading authThreading = new AuthThreading(settings.getAuthThreadCount(),console, accountCredentials,settings);
        authThreading.getTokens();
        System.out.println("\n");
        TargetGrabber targetGrabber = new TargetGrabber(accountCredentials, console);
        targetGrabber.refineTargets();
        ArrayList<Target> targets = targetGrabber.getTargets();
        System.out.println("\n");
        console.updateMessage("?", "Threads: ");
        int nThreads = console.getUserInt();
        CheckThreading checkThreading = new CheckThreading(accountCredentials,targets,nThreads, console, settings);
        checkThreading.createAndRunTasks();
    }
}
