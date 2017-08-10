
import java.sql.Connection;

/**
 * Created by ning on 7/14/17.
 */
public class DataTransformer {
    public static void main(String[] args){
        DBReader dbr=new DBReader();
        System.out.println("Connecting to the database...");
        Connection conn= dbr.DBConnect(null);
        //System.out.println("Dealing with database...");
        //dbr.cleanFiles(conn);
        System.out.println("Generating session data...");
        dbr.ssessionInfo(conn);

        //dbr.generateMetadata(conn);
        //System.out.println("Summarizing session information...");
        //dbr.createCSVFile(conn);
        //dbr.generateRefinedMetadata(conn);
    }
}