import java.io.*;
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
    public void createCSVFile(Connection conn){
        String filePath="files/";
        File csvFile= new File("files/KPAAMCAM.csv");
        try {
            if(!csvFile.isFile()){
                csvFile.createNewFile();
                writeCsvFields(csvFile, conn);

            }
        }
        catch (IOException e){

        }

    }
    private void writeCsvFields(File csvFile, Connection conn){
        try {
            FileOutputStream fos=new FileOutputStream(csvFile);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            String firstRow="sessionuuid, dc.title, dc.coverage.spatial, dc.description, dc.contributor.researcher, dc.contributor.speaker, filename, dc.relation.isPartOf,dc.relation.references";
            bw.write(firstRow);
            bw.newLine();
            bw.close();
            fos.close();
            writeSessionBasicInfo(conn, csvFile);
        }
        catch (IOException e){

        }
    }
    private void writeSessionBasicInfo(Connection conn,  File csv){
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
                if(!csv.isFile()){
                    csv.createNewFile();
                    writeCsvFields(csv, conn);
                }

                organizeSessionBasicInfo(csv,rcFile, conn);

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
            while(rcSss.next()){
                String tmp=rcSss.getString("Sid");
                InfoTuple tuple=new InfoTuple(tmp, rcSss.getString("Sname"),rcSss.getString("Loc"),rcSss.getString("sdsc"), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null, new ArrayList<String>());
                sessionInfoList.put(tmp, tuple);
            }
            getCompleteSessionInfo(csv,sessionInfoList, conn);
        }
        catch (Exception e){
            e.printStackTrace();
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
                String fileQuery="select fidlabel.fid as fid, flabel, fpath, fStartTime from " +
                        "(select uuid as fid,measure as flabel from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileID') " +
                        "and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid in " +
                        "(select uuid as ansId from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+sid+"' " +
                        "AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') and latestNonDeletedRelationship.Deleted IS NULL)))) " +
                        "AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and File')and latestNonDeletedRelationship.Deleted IS NULL)))) fidLabel " +
                        "INNER JOIN " +
                        "(select ftime.fid as fid, fpath, fStartTime FROM " +
                        "(SELECT uuid as fid, measure as fpath FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileContent')) fcontent " +
                        "INNER JOIN " +
                        "(SELECT uuid as fid, measure as fStartTime FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileStartTime')) ftime " +
                        "on fcontent.fid=ftime.fid) finfo " +
                        "ON fidlabel.fid=finfo.fid";
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
                    checkDupFile(rFile.getString("flabel"), fileInfo, rFile.getString("fpath"), rFile.getString("fid"),rFile.getString("fStartTime"), tmp);
                }
                while(rFT.next()){
                    tmp.fieldTrip=rFT.getString("FTLabel");
                }
                while(rQuesnir.next()){
                    tmp.questionnaire.add(rQuesnir.getString("quesnir"));
                }
                sssList.put(key, tmp);
            }
            if(!csv.isFile()){
                csv.createNewFile();
                writeCsvFields(csv,conn);
            }
            FileOutputStream fos=new FileOutputStream(csv);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            for(String key: sssList.keySet()){
                StringBuilder sb=new StringBuilder();
                InfoTuple t=sssList.get(key);
                sb.append(key+",");
                sb.append(t.sname+",");
                sb.append(t.loc+",");
                sb.append(t.desc+",");

                for(int i=0;i<t.interviewer.size(); i++) {
                    if(i!=t.interviewer.size()-1){
                        sb.append(t.interviewer.get(i)+";");
                    }
                    else{
                        sb.append(t.interviewer.get(i)+",");
                    }
                }

                for(int i=0;i< t.consultant.size(); i++){
                    if(i!=t.consultant.size()-1) {
                        sb.append(t.consultant.get(i) + ";");
                    }
                    else{
                        sb.append(t.consultant.get(i)+",");
                    }
                }

                for(int i=0; i<t.filename.size();i++){
                    if(i!=t.filename.size()-1){
                        sb.append(t.filename.get(i)+"|");
                    }
                    else{
                        sb.append(t.filename.get(i)+",");
                    }
                }
                sb.append(t.fieldTrip+",");
                for(int i=0; i<t.questionnaire.size();i++){
                    if(i!=t.questionnaire.size()-1){
                        sb.append(t.questionnaire.get(i)+";");
                    }
                    else{
                        sb.append(t.questionnaire.get(i));
                    }
                }
                String s=sb.toString();
                bw.write(s);
                bw.newLine();
            }
            bw.close();
            fos.close();
            renameFiles(fileInfo);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void renameFiles(HashMap<String, ArrayList<String>> filels){
        for(String key: filels.keySet()){
            String oldPath=filels.get(key).get(0);
            String newPath="files"+oldPath.substring(oldPath.lastIndexOf("/"));
            File oldFile=new File(newPath);
            if(oldFile.isFile()){
                String extension=getFileExtension(newPath);
                File newFile=new File("files/"+key+extension);
                if(!oldFile.renameTo(newFile)){
                    System.out.println("renameing failed"+key);
                }
            }
        }
    }
    private void checkDupFile(String filename, HashMap<String, ArrayList<String>> filels, String filePath, String fileId, String fileTime, InfoTuple tuple){
        int suffix=1;
        boolean dupFile=false;
        if(filels.containsKey(filename)){
            dupFile=true;
            //rename existing file
            ArrayList<String> tmp=filels.get(filename);
            filels.remove(filename);
            filels.put(filename+"(1)", tmp);
            for(int i=0;i<tuple.filename.size();i++){
                if(tuple.filename.get(i).equals(filename)){
                    tuple.filename.set(i,filename+"(1)");
                }
            }
        }
        else if(filels.containsKey(filename+"(1)")){
            dupFile=true;
        }

        if(dupFile){
            while(true){
                suffix++;
                String tmpName=filename+"("+suffix+")";
                if(!filels.containsKey(tmpName)){
                    break;
                }
            }
            ArrayList fileInfo=new ArrayList<>();
            fileInfo.add(filePath);
            fileInfo.add(fileId);
            String newFilename=filename+"("+suffix+")";
            filels.put(newFilename,fileInfo);
            tuple.filename.add(newFilename);
        }
        else{
            ArrayList fileInfo=new ArrayList<>();
            fileInfo.add(filePath);
            fileInfo.add(fileId);
            filels.put(filename,fileInfo);
            tuple.filename.add(filename);
        }
    }

    private String getFileExtension(String filePath){
        int i=filePath.lastIndexOf(".");
        if (i>0){
            return filePath.substring(i);
        }
        return null;
    }
    public void generateRefinedMetadata(Connection conn){
        //generate fieldTrip metadata file for each fieldTrip
        //generateFTMetaFile(conn);
        //generateSessionMetaFile(conn);
        generatePersonMetaFile(conn);
        //generate session person role, name file for each fieldTrip
        //generate answer metadata file
        //generate file metadata: file name, file type, file start timestamp
        //generate questionnaire: including questions
    }
    private void generateFTMetaFile(Connection conn){
        String loadAllFTQuery="SELECT uuid as ftid, measure as ftlabel from latestAllArchEntIdentifiers where AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FieldTripID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllFT=s1.executeQuery(loadAllFTQuery);
            while(rAllFT.next()){
                uuid=rAllFT.getString("ftid");

                String getEntityValue="SELECT uuid as ftid, attributename as ftattr, measure as ftval, freetext as ftannotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rFT=s2.executeQuery(getEntityValue);
                if(!rFT.next()){continue;}

                File ftFile=createMetaDataFile(rAllFT.getString("ftlabel")+".json");
                if(ftFile==null){
                    //TODO: write log here
                    continue;
                }
                FileOutputStream fos=new FileOutputStream(ftFile);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("{");
                bw.newLine();
                bw.write("\"FieldTripUuid\":\""+uuid+"\"");
                while(rFT.next()){
                    bw.write(",");
                    bw.newLine();
                    String data="\""+rFT.getString("ftattr")+"\":\""+rFT.getString("ftval")+"\"";
                    bw.write(data);
                    if(rFT.getString("ftannotation")!=null){
                        bw.write(",");
                        bw.newLine();
                        bw.write("\"annotation\":\""+rFT.getString("ftannotation")+"\"");
                    }
                }
                bw.newLine();
                bw.write("}");
                bw.close();
                fos.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void  generateSessionMetaFile(Connection conn){

    }
    private void generatePersonMetaFile(Connection conn){
        String allPersonQuery="SELECT uuid as pid,measure as plabel FROM latestNonDeletedArchEntIdentifiers " +
                "WHERE latestNonDeletedArchEntIdentifiers.AttributeID "+
                "= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='PersonID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllPS=s1.executeQuery(allPersonQuery);
            while(rAllPS.next()){
                uuid=rAllPS.getString("pid");

                String getEntityValue="SELECT uuid as pid, attributename as pattr, measure as psval, freetext as annotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rFT=s2.executeQuery(getEntityValue);
                if(!rFT.next()){continue;}

                File psFile=createMetaDataFile(rAllPS.getString("plabel")+".json");
                if(psFile==null){
                    //TODO: write log here
                    continue;
                }
                FileOutputStream fos=new FileOutputStream(psFile);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("{");
                bw.newLine();
                bw.write("\"PersonUuid\":\""+uuid+"\"");
                while(rFT.next()){
                    bw.write(",");
                    bw.newLine();
                    String data="\""+rFT.getString("pattr")+"\":\""+rFT.getString("psval")+"\"";
                    bw.write(data);
                    if(rFT.getString("annotation")!=null){
                        bw.write(",");
                        bw.newLine();
                        bw.write("\"annotation\":\""+rFT.getString("ftannotation")+"\"");
                    }
                }
                bw.newLine();
                bw.write("}");
                bw.close();
                fos.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private File createMetaDataFile(String filename){
        String fileFullName="files/"+filename;
        File metaFile=new File(fileFullName);
        if(!metaFile.isFile()){
            try {
                metaFile.createNewFile();
                return metaFile;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }
        return metaFile;
    }
}
