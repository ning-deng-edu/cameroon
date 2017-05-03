import java.io.File;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
/**
 * Created by ning on 4/28/17.
 */
public class folderExtractor {
    public static void main(String[] args){
        System.out.println("Please enter your name for logging");
        String username=null;
        username=System.console().readLine();
        System.out.println("Validating system settings and program settings...");
        String currentDir=System.getProperty("user.dir");
        if(!validDBFile()) {
            System.out.println("No database file found\n Please put the db.sqlite3 file in the db directory under the current directory\n "+currentDir);
            System.exit(0);
        }
        if(!validFileFolder()){
            System.out.println("No files folder found or no file in the folder\n Please put the 'files' folder in the current directory\n"+currentDir);
            System.exit(0);
        }
        if(!createOutputDir()){
            System.out.println("The program can't create folders on your system\n Please check if your system setting is correct \n or contact ndeng@buffalo.edu");
            System.exit(0);
        }

        System.out.println("Connecting Database...");
        DBReader dbr=new DBReader();
        Connection conn=dbr.DBConnect(null);
        System.out.println("Executing Query...");
        HashMap<String, ResultSet> resultSet=dbr.queryExecution(null, conn);
        fileManager fm=new fileManager();
        try{
            System.out.println("Creating LogFile...");
            fm.getLogFile("output", username);
            System.out.println("Creating Folders...");
            fm.createFolders(resultSet.get("Session"));
            System.out.println("Reorganizing files, please wait...");
            fm.copyAndRenameFiles(resultSet.get("File"));
            System.out.println("Reorganizing files done, the result is in the 'output' folder");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    private static boolean validDBFile(){
        File dbFile=new File("db/db.sqlite3");
        if(!dbFile.isFile()){
           return false;
        }
        return true;
    }
    private static boolean validFileFolder(){
        File fileFolder=new File("files/app");
        if(!fileFolder.isDirectory()){
            return false;
        }
        try{
           Path path= Paths.get("files/app");
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
            if(!dirStream.iterator().hasNext()) {
                dirStream.close();
                return false;
            }
            dirStream.close();
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
    private static boolean createOutputDir(){
        File outPut=new File("output");
        if(!outPut.isDirectory()){
            if(!outPut.mkdir()){
                return false;
            }
        }
        return true;
    }
}
