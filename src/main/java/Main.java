public class Main {
    public static void main(String[] args){
        FileIO fileIO = new FileIO();
        System.out.println(fileIO.getDataDirectoryPath());
        YamlParser yamlParser = new YamlParser();
        Settings settings = yamlParser.parseYaml("Settings.yaml");
        System.out.println(settings.getAuthThreadCount());
        XboxLiveAuth xboxLiveAuth = new XboxLiveAuth();
        xboxLiveAuth.login("allxbl1337@outlook.com","#$tayS4fer");
    }
}
