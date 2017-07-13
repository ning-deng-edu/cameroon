import javax.sound.midi.MidiDevice;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ning on 7/13/17.
 */
public class DBReader {
    public Connection DBConnect(String path){
        try{
            Class.forName("org.sqlite.JDBC");// register the driver
            String connMainString="jdbc:sqlite:";
            String connString=connMainString;
            if(path==null || path.length()==0) connString+="db/db.sqlite3";
            else connString+=path;
            Connection conn= DriverManager.getConnection(connString);
            if(conn==null) System.out.println("Failed to connect to DB");
            return conn;
        }
        catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
    public void createCSVFile(){
        String filePath="output/";
        File csvFile= new File("output/KPAAMCAM.csv");
        try {
            if(!csvFile.isFile()){
                csvFile.createNewFile();
                writeCsvFields(csvFile);
            }
        }
        catch (IOException e){

        }

    }
    private void writeCsvFields(File csvFile){
        try {
            FileOutputStream fos=new FileOutputStream(csvFile);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            String firstRow="sessionuuid, dc.title, dc.coverage.spatial, dc.description, dc.contributor.researcher, dc.contributor.speaker, filename, dc.relation.isPartOf,dc.relation.references";
            bw.write(firstRow);
            bw.newLine();
        }
        catch (IOException e){

        }
    }
    public void writeSessionBasicInfo(Connection conn,  File csv){
        String getAllSessionIdQuery="SELECT sidloc.Sid AS Sid, Sname, Loc, sdsc  FROM "+
                "(((SELECT uuid as id, measure as Loc FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionLocation')) sloc "+
                "INNER JOIN "+
                "(SELECT uuid as Sid,measure as Sname FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')) sidentifier on sloc.id=sidentifier.sid) sidloc "+
                "INNER JOIN "+
                "(SELECT uuid as id, measure as sdsc FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionDescription')) sdesc on "+
                "sidloc.sid=sdesc.id)";
        try{
            PreparedStatement statement=conn.prepareStatement(getAllSessionIdQuery);
            statement.setFetchSize(500);
            try {
                ResultSet rcFile = statement.executeQuery();
                if(csv.isFile()){
                    organizeSessionBasicInfo(csv,rcFile, conn);
                }
            }
            catch(Exception e){
                System.out.print(e+"");
            }
        }
        catch(Exception e){
            System.out.print(e+"");
        }
    }
    private void organizeSessionBasicInfo(File csv, ResultSet rcSss, Connection conn){
        if(!csv.isFile()){return;}
        try {
            HashMap<String, InfoTuple> sessionInfoList=new HashMap<>();
            FileOutputStream fos=new FileOutputStream(csv);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            while(rcSss.next()){
                String tmp=rcSss.getString("Sid");
                InfoTuple tuple=new InfoTuple(tmp, rcSss.getString("Sname"),rcSss.getString("Loc"),rcSss.getString("sdsc"), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null, new ArrayList<String>());
                sessionInfoList.put(tmp, tuple);
            }
            getCompleteSessionInfo(csv,sessionInfoList, conn);
        }
        catch (Exception e){

        }
    }
    private void getCompleteSessionInfo(File csv, HashMap<String, InfoTuple> sssList, Connection conn){
        try {
            String sid="";
            HashMap<String, ArrayList<String>> fileInfo=new HashMap<>();
            for(String key: sssList.keySet()){
                sid=key;
                String consultantQuery="SELECT measure as psLabel from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonID') and uuid in " +
                        "(select uuid from latestNonDeletedAentReln where uuid <>'"+sid+"' and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sid+"' and RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and Consultant') and latestNonDeletedRelationship.Deleted IS NULL)))";
                String interviewerQuery="SELECT measure as psLabel from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonID') and uuid in " +
                        "(select uuid from latestNonDeletedAentReln where uuid <>'"+sid+"' and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sid+"' and RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and Interviewer') and latestNonDeletedRelationship.Deleted IS NULL)))";
                String fileQuery="select fidlabel.fid, flabel, fpath from " +
                        "((select uuid as fid,measure as flabel from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileID') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid in " +
                        "(select uuid as ansId from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+sid+
                        "' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') and latestNonDeletedRelationship.Deleted IS NULL)))) " +
                        "AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and File')and latestNonDeletedRelationship.Deleted IS NULL)))) fidLabel " +
                        "INNER JOIN " +
                        "(select uuid as fid,measure as fpath from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileContent')) fpath " +
                        "ON fidlabel.fid=fpath.fid)";
                String fieldTripQuery="select uuid,measure as FTLabel from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FieldTripID') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+sid+
                        "' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and FieldTrip')and latestNonDeletedRelationship.Deleted IS NULL)))";
                String questionniareQuery="SELECT uuid,measure as quesnir FROM latestNonDeletedAentValue WHERE latestNonDeletedAentValue.AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionnaireName') AND latestNonDeletedAentValue.uuid IN "+
                        "(select measure from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+sid
                        +"' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') and latestNonDeletedRelationship.Deleted IS NULL))) group by measure)";
                Statement s1=conn.createStatement();
                Statement s2=conn.createStatement();
                Statement s3=conn.createStatement();
                Statement s4=conn.createStatement();
                Statement s5=conn.createStatement();
                ResultSet rConsultant=s1.executeQuery(consultantQuery);
                ResultSet rInterviewer=s2.executeQuery(interviewerQuery);
                ResultSet rFile=s3.executeQuery(fileQuery);
                ResultSet rFT=s4.executeQuery(fieldTripQuery);
                ResultSet rQuesnir=s5.executeQuery(questionniareQuery);

                InfoTuple tmp=sssList.get(key);
                while(rConsultant.next()){
                    tmp.consultant.add(rConsultant.getString("psLabel"));
                }
                while(rInterviewer.next()){
                    tmp.interviewer.add(rInterviewer.getString("psLabel"));
                }
                while(rFile.next()){
                    tmp.filename.add(rFile.getString("flabel"));
                    ArrayList<String> tmpFileInfo=new ArrayList<>();
                    tmpFileInfo.add(rFile.getString("fpath"));
                    tmpFileInfo.add(rFile.getString("flabel"));
                    fileInfo.put(rFile.getString("fid"), tmpFileInfo);
                }
                while(rFT.next()){
                    tmp.fieldTrip=rFT.getString("FTLabel");
                }
                while(rQuesnir.next()){
                    tmp.questionnaire.add(rQuesnir.getString("quesnir"));
                }
                sssList.put(key, tmp);
            }

        }
        catch (Exception e){

        }
    }
}
