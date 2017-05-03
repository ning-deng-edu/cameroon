/**
 * Created by ning on 4/28/17.
 */
import java.sql.*;
import java.util.*;


public class DBReader {
    Connection c=null;
    public Connection DBConnect(String path){
        try{
            Class.forName("org.sqlite.JDBC");// register the driver
            String connMainString="jdbc:sqlite:";
            String connString=connMainString;
            if(path==null || path.length()==0) connString+="db/db.sqlite3";
            else connString+=path;
            Connection conn=DriverManager.getConnection(connString);
            if(conn==null) System.out.println("Failed to connect to DB");
            return conn;
        }
        catch (Exception e){
            System.out.println(e);
            return null;
        }
    }


    public HashMap<String, ResultSet> queryExecution(String query, Connection conn){
        ResultSet rcSession=null;
        ResultSet rcFile=null;
        rcSession=querySession(conn);
        if(query==null) {// default query
            rcFile=fileQuery(conn);
        }
        HashMap<String, ResultSet> res=new HashMap<>();
        res.put("Session", rcSession);
        res.put("File", rcFile);
        return res;
    }

    /**
     * ** For incremental query
     * @param query
     * @param oldFile: old sqlite3 file
     * @param newFile: new sqlite3 file
     * @param conn
     */
    public void queryExecution(String query, String oldFile, String newFile, Connection conn){

    }

    private ResultSet querySession(Connection conn){
        ResultSet rcSession=null;
        String query="SELECT uuid AS SessionID, measure AS SessionLabel FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')";
        try{
            Statement statement=conn.createStatement();
            try{
                rcSession=statement.executeQuery(query);
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        return rcSession;
    }

    private ResultSet fileQuery(Connection conn){
        ResultSet rcFile=null;
        String query="SELECT Sid, Sname, Alabel, Fid, FName,FPath FROM " +
                "(SELECT Sid, Sname, Aid, Alabel FROM " +
                "(SELECT Sid, Sname, relnid FROM " +
                "(SELECT uuid AS rlid, RelationshipID as relnid from AEntReln WHERE RelationshipID IN (SELECT RelationshipID FROM latestNonDeletedRelationship WHERE RelnTypeID=(SELECT RelnTypeID FROM RelnType WHERE RelnTypeName='Answer and Session'))) rnSession " +
                "INNER JOIN  " +
                "(SELECT uuid AS Sid, measure AS Sname FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID =(SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')) session " +
                "ON rnSession.rlid=session.Sid) sessionReln " +
                "INNER JOIN " +
                "(SELECT relnid, Aid, Alabel From " +
                "(SELECT uuid AS rlid, RelationshipID as relnid  " +
                "from AEntReln  " +
                "WHERE RelationshipID IN  " +
                " (SELECT RelationshipID FROM latestNonDeletedRelationship  " +
                " WHERE RelnTypeID=(SELECT RelnTypeID FROM RelnType WHERE RelnTypeName='Answer and Session'))) rnAnswer " +
                "INNER JOIN " +
                "(SELECT uuid AS Aid, measure AS Alabel  " +
                "FROM latestAllArchEntIdentifiers  " +
                "WHERE  latestAllArchEntIdentifiers.AttributeID=( " +
                " select AttributeID from AttributeKey where AttributeName='AnswerLabel')) answer  " +
                "ON rnAnswer.rlid=answer.Aid) AnsReln  " +
                "ON sessionReln.relnid=AnsReln.relnid) SessionAns " +
                " " +
                "INNER JOIN  " +
                "(SELECT Aid, Fid,FName, FPath FROM   " +
                "(SELECT FileInfo.Fid, FName, FPath, frelnid from  " +
                "(SELECT uuid AS frlid, RelationshipID as frelnid  " +
                "from AEntReln  " +
                "WHERE RelationshipID IN  " +
                " (SELECT RelationshipID FROM latestNonDeletedRelationship  " +
                " WHERE RelnTypeID=(SELECT RelnTypeID FROM RelnType WHERE RelnTypeName='Answer and File'))) freln " +
                "INNER JOIN  " +
                "(Select fBasic.Fid, FName, FPath FROM " +
                "(SELECT uuid as Fid, measure As FName FROM latestNonDeletedArchEntIdentifiers " +
                "WHERE AttributeID =(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileID')) fBasic " +
                "INNER JOIN  " +
                "(SELECT uuid as Fid, measure As FPath FROM latestNonDeletedAentValue " +
                "WHERE AttributeID =(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileContent')) fPath " +
                "ON fBasic.Fid=fPath.Fid) FileInfo " +
                "ON FileInfo.Fid=freln.frlid) FileReln " +
                "INNER JOIN " +
                "(SELECT relnid , Aid From " +
                "(SELECT uuid AS rlid, RelationshipID as relnid  " +
                "from AEntReln  " +
                "WHERE RelationshipID IN  " +
                " (SELECT RelationshipID FROM latestNonDeletedRelationship  " +
                " WHERE RelnTypeID=(SELECT RelnTypeID FROM RelnType WHERE RelnTypeName='Answer and File'))) rnAnswer " +
                "INNER JOIN " +
                "(SELECT uuid AS Aid " +
                "FROM latestAllArchEntIdentifiers  " +
                "WHERE  latestAllArchEntIdentifiers.AttributeID=( " +
                " select AttributeID from AttributeKey where AttributeName='AnswerLabel')) answer  " +
                "ON rnAnswer.rlid=answer.Aid) ansFile " +
                "ON FileReln.frelnid =ansFile.relnid) AnswerFile " +
                "ON SessionAns.Aid=AnswerFile.Aid";
        try{
            Statement statement=conn.createStatement();
            try{
                rcFile=statement.executeQuery(query);
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        return rcFile;
    }
}
