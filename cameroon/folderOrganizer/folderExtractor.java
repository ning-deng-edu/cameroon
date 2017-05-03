import java.io.File;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by ning on 4/28/17.
 */
public class folderExtractor {
    public static Logger logFile=null;
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
        if(conn==null){
            System.out.println("Unable to connect to the database, please check if you are running this program with the dependent .jar file");
            System.exit(0);
        }
        System.out.println("Creating LogFile...");
        getLogFile("output", username);
        System.out.println("Executing Query...");
        long startTime=System.currentTimeMillis();
        HashMap<String, ResultSet> resultSet=dbr.queryExecution(null, conn, logFile);
        long endTime= System.currentTimeMillis();
        System.out.println("Query execution time:"+(endTime-startTime)+" ms");
        fileManager fm=new fileManager();
        try{
            System.out.println("Creating Folders...");
            startTime=System.currentTimeMillis();
            fm.createFolders(resultSet.get("Session"), logFile);
            endTime=System.currentTimeMillis();
            System.out.println("Folder creation time:"+(endTime-startTime)+" ms");

            System.out.println("Reorganizing files, please wait...");
            startTime=System.currentTimeMillis();
            fm.copyAndRenameFiles(resultSet.get("File"),  logFile);
            endTime=System.currentTimeMillis();
            System.out.println("File reorganizing time:"+(endTime-startTime)+" ms");
            System.out.println("Reorganizing files done, the result is in the 'output' folder");
        }
        catch (Exception e){
            logFile.severe(e+"");
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
    private static void getLogFile(String path, String userName){
        Log lg=new Log();
        logFile=lg.createLog(path, userName);
    }
}
