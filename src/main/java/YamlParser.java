import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class YamlParser {
    private FileIO fileIO = new FileIO();
    public YamlParser(){}
    public Settings parseYaml(String fileName) {
        Settings settings = null;
        File settingsFile = new File(fileIO.getDataDirectoryPath() + fileName);
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        try {
            om.findAndRegisterModules();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            settings = om.readValue(settingsFile,Settings.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return settings;
        }
    }
}
