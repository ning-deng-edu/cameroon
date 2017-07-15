import java.sql.Connection;

/**
 * Created by ning on 7/14/17.
 */
public class DataTransformer {
    public static void main(String[] args){
        DBReader dbr=new DBReader();
        Connection conn= dbr.DBConnect(null);
        //dbr.createCSVFile(conn);
        dbr.generateRefinedMetadata(conn);
    }
}
