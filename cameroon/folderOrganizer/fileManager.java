/**
 * Created by ning on 4/28/17.
 */
import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class fileManager {
    public static Logger logFile=null;
    public void getLogFile(String path, String userName){
        Log lg=new Log();
        logFile=lg.createLog(path, userName);
    }

    public void createFolders(ResultSet resultSet) throws SQLException {// make session folders
        int count=0;
        int failCount=0;
        String mainPath="output/";

        while(resultSet.next()){
            String folderName=resultSet.getString("SessionLabel")+resultSet.getString("SessionID");
            String path=mainPath+folderName;
            try{
                File newFile=new File(path);
                if(!newFile.exists()){
                    if(newFile.mkdir()){
                        count++;
                    }
                    else{
                        if(logFile!=null) {
                            logFile.warning("Unable to create directory:"+folderName);
                        }
                        System.out.println("Creating Folders...");
                        failCount++;
                    }
                }
            }
            catch (Exception e){
                if(logFile!=null) {
                    logFile.log (Level.ALL,"Error when create directory :"+e);
                }
                System.out.println("Creating Folders...");
            }
        }
        logFile.info("Total "+ count +" folders created\n"+"Failed to create total " +failCount+ " folders");
    }

    public void copyAndRenameFiles(ResultSet resultSet) throws SQLException{
        String folderMainPath="output/";
        int count=0;
        int copyCount=0;
        while(resultSet.next()){
            String folderPath=folderMainPath+resultSet.getString("Sname")+resultSet.getString("Sid");
            String oldFileName=resultSet.getString("FPath");
            String fileExtension=getFileExtension(oldFileName);
            if(fileExtension==null) continue;
            String newFileName=resultSet.getString("FName");
            String newFileTmpPath=folderPath+"/"+newFileName;
            String filePath=checkDupFile(newFileTmpPath, fileExtension);
            count++;
            try{
                File fileFolder=new File(folderPath);
                if(!fileFolder.exists()){
                    if(fileFolder.mkdir()) {
                        System.out.println(folderPath + " " + "folder is created");
                    }
                }
                File source=new File(oldFileName);
                File dest=new File(filePath);
                try {
                    Files.copy(source.toPath(), dest.toPath());
                    copyCount++;
                }
                catch (IOException ioe){
                    if(logFile!=null) {
                        logFile.severe("File copy failed due to "+ioe+" "+filePath);
                    }
                    System.out.println("Reorganizing files, please wait...");
                }

            }
            catch (Exception e){
                if(logFile!=null) {
                    logFile.severe("File operation failure due to "+e);
                }
                System.out.println("Reorganizing files, please wait...");
            }
        }
        logFile.info("Total "+ count +" file records scanned in the database\n"+"Total " +copyCount+ " files copied and renamed");
    }
    private String getFileExtension(String filePath){
        int i=filePath.lastIndexOf(".");
        if (i>0){
            return filePath.substring(i);
        }
        return null;
    }
    private String checkDupFile(String path, String extension){
        int suffix=1;
        boolean dupFile=false;
        if(new File(path+extension).isFile()){
            dupFile=true;
            //rename existing file
            File file=new File(path+extension);
            File renameFile=new File(path+"(1)"+extension);
            if(!file.renameTo(renameFile)){
                if(logFile!=null) {
                    logFile.severe("File renaming failed on"+path+extension);
                }
                System.out.println("Reorganizing files, please wait...");
            }
        }
        else if(new File(path+"(1)"+extension).isFile()){
            dupFile=true;
        }
        if(dupFile){
            while(true){
                suffix++;
                String tmpPath=path+"("+suffix+")"+extension;
                if(!new File(tmpPath).isFile()){
                    break;
                }
            }
            return path+"("+suffix+")"+extension;
        }
        else{
            return path+extension;
        }

    }

}
