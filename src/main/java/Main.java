public class Main {
    public static void main(String[] args){
        FileIO fileIO = new FileIO();
        YamlParser yamlParser = new YamlParser();
        Settings settings = yamlParser.parseYaml("Settings.yaml");
        XboxLiveAuth xboxLiveAuth = new XboxLiveAuth();
        xboxLiveAuth.login("allxbl1337@outlook.com","#$tayS4fer");
    }
}
