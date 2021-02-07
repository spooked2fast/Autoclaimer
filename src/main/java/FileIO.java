import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class FileIO {
    private String dataDirectoryPath;
    private ReentrantLock lock = new ReentrantLock();
    public FileIO(){
        String windowsPath = System.getProperty("user.dir") + "\\Data\\";
        try{
            File testDirectoryPath = new File(windowsPath + "Gamertags.txt");
            this.dataDirectoryPath = windowsPath;
        } catch(Exception e){
            this.dataDirectoryPath = System.getProperty("user.dir") + "/Data/";
        }
    }
    public ArrayList<String> fileContentsToList(String filePath){
        ArrayList<String> contents = new ArrayList<String>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String l;
            while((l = br.readLine())!=null){
                if(! contents.contains(l)){
                    contents.add(l);
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("File not found:" + filePath);
        }
        finally {
            return contents;
        }
    }
    public void writeToFile(String pathName, String contents) throws IOException {
        lock.lock();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(pathName, true)));
        pw.println(contents);
        pw.close();
        lock.unlock();
    }
    public void clearFile(String name){
        lock.lock();
        try {
            File file = new File(dataDirectoryPath + name);
            file.delete();
            try{
                file.createNewFile();
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    public String getDataDirectoryPath(){
        return dataDirectoryPath;
    }
}
