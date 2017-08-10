import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by ning on 7/13/17.
 */
public class DBReader {
    HashMap<String, String> fileUpdate=new HashMap<>();
    HashMap<String, InfoTuple> sessionInfoList=new HashMap<>();
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
    private File createCSVFile(){
        File csvFile= new File("files/KPAAMCAM.csv");
        try {
            if(!csvFile.isFile()){
                csvFile.createNewFile();
                return csvFile;
            }
            return csvFile;
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public void cleanFiles(Connection conn){
        String getAllFilesQuery="SELECT uuid as fid,measure as flabel FROM latestNonDeletedArchEntIdentifiers " +
                "WHERE AttributeID "+
                "= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileID') "+
                "GROUP BY uuid ORDER BY ValueTimestamp asc;";
        HashMap<String, ArrayList<String>> map=new HashMap<>();
        try{
            Statement fs=conn.createStatement();
            ResultSet rfile=fs.executeQuery(getAllFilesQuery);
            while(rfile.next()){
                String nonSuffixLabel=getOriginLabel(rfile.getString("flabel"));
                if(!map.containsKey(nonSuffixLabel)){
                    map.put(nonSuffixLabel, new ArrayList<String>());
                }
                ArrayList<String> tmp=map.get(nonSuffixLabel);
                tmp.add(rfile.getString("fid"));
                map.put(nonSuffixLabel, tmp);
            }

            String attributeIDQuery="SELECT AttributeID as aid FROM AttributeKey WHERE AttributeName='FileID'";
            String attributeID="";

            Statement s1=conn.createStatement();
            ResultSet ra=s1.executeQuery(attributeIDQuery);
            while(ra.next()){
                attributeID=ra.getString("aid");
            }
            for(String key: map.keySet()){
                ArrayList<String> temp=map.get(key);
                int size=temp.size();
                if(size==1){
                    String updateQuery="UPDATE AentValue SET Measure='"+ key+ "' WHERE AttributeID='"
                            +attributeID+"' AND UUID='"+temp.get(0)+"'";

                    Statement u1=conn.createStatement();
                    u1.executeUpdate(updateQuery);
                    u1.close();
                    continue;
                }
                else{
                    for(int i=0;i<size;i++){
                        int suffix=i+1;
                        String withSuffix=key+"("+suffix+")";

                        String updateQuery="UPDATE AEntValue SET Measure='"+ withSuffix+ "' WHERE AttributeID='"
                                +attributeID+"' AND UUID='"+temp.get(i)+"'";

                        Statement u1=conn.createStatement();
                        u1.executeUpdate(updateQuery);
                        u1.close();
                    }
                }
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getOriginLabel(String label){
        String nonSuffixLabel=label;
        Pattern numberPat = Pattern.compile("\\(\\d+\\)$");
        Matcher numMatcher = numberPat.matcher(label);
        if(numMatcher.find()){
            String [] patternSplit=label.split("\\(");
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<patternSplit.length-1;i++){
                sb.append(patternSplit[i]);
            }
            nonSuffixLabel=sb.toString();

        }
        return nonSuffixLabel;
    }
    public void ssessionInfo(Connection connection){
        String getAllSessionInfo="SELECT uuid as Sid,measure as Sname, ValueTimeStamp as ts FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')";
        ArrayList<String> photoFile=new ArrayList<>();
        try{
            Statement s1=connection.createStatement();
            ResultSet rAllSession=s1.executeQuery(getAllSessionInfo);
            while(rAllSession.next()){
                //session basic info
                JSONObject obj=new JSONObject();
                String uuid=rAllSession.getString("Sid");
                InfoTuple it=new InfoTuple();
                it.sid=uuid;
                String sessionName=rAllSession.getString("Sname");
                obj.put("DBSessionLabel", sessionName);
                sessionName=cleanFileName(sessionName);
                it.sname=sessionName;
                String ts=rAllSession.getString("ts");
                it.date=ts.substring(0, ts.indexOf(" "));
                obj.put("SessionUuid",uuid);
                obj.put("SessionLabel", it.sname);
                String getSessionBasicInfo="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement sbasic=connection.createStatement();
                ResultSet sBasicInfo= sbasic.executeQuery(getSessionBasicInfo);
                while (sBasicInfo.next()){
                    String attr=sBasicInfo.getString("attr");
                    if(!attr.equals("SessionID")){
                        obj.put(sBasicInfo.getString("attr"), sBasicInfo.getString("val"));
                    }
                    if(sBasicInfo.getString("annotation")!=null){
                        obj.put(sBasicInfo.getString("attr")+"Annotation", sBasicInfo.getString("annotation"));
                    }
                    switch (attr){
                        case "SessionLocation":
                            it.loc=sBasicInfo.getString("val");
                            break;
                        case "SessionDescription":
                            it.desc=sBasicInfo.getString("val");
                            break;
                    }
                }
                //session person info
                JSONArray jPerson=new JSONArray();
                String sessionPersonRoleQuery="select t5.personUuid as pid, t6.personRoleName as psRoleName, t5.persName as pName "+
                        "from (select t1.personId as personUuid, t1.personName as persName, t2.psssId as relnId from (select pId.uuid as personId, pId.measure as personName "+
                        "from latestNonDeletedAentValue as pId where pId.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='PersonName') "+
                        "and pId.uuid in (select psName.measure from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln "+
                        "where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') "+
                        "and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psName.uuid=psReln.uuid and psName.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey "+
                        "where AttributeName='SessionIDforPerson')))) t1 inner join "+
                        "(select psName.uuid as psssId, psName.measure as psId from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln "+
                        "where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') "+
                        "and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psName.uuid=psReln.uuid) t2 on t1.personId=t2.psId) t5 "+
                        "inner join "+
                        "(select t3.roleId as personRoleUuid, t3.roleName as personRoleName, t4.rsssId as relnId from (select rId.uuid as roleId, rId.measure as roleName "+
                        "from latestNonDeletedAentValue as rId where rId.AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonRoleName') "+
                        "and rId.uuid in (select psRole.measure from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln "+
                        "where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID "+
                        "from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' and psRole.uuid=psReln.uuid))t3 "+
                        "inner join (select psRole.uuid as rsssId, psRole.measure as tempPsRoleID from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln "+
                        "where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID "+
                        "from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psRole.uuid=psReln.uuid)t4 on t3.roleId=t4.tempPsRoleID) t6 on t5.relnId =t6.relnId group by pid, psRoleName";

                Statement sPs=connection.createStatement();
                ResultSet rPs=sPs.executeQuery(sessionPersonRoleQuery);
                while(rPs.next()){
                    JSONObject psObj=new JSONObject();
                    String puuid=rPs.getString("pid");
                    String roleName=rPs.getString("psRoleName");
                    String psName=rPs.getString("pName");
                    psObj.put("PersonUuid",puuid);
                    if(roleName.equals("consultant")){
                        it.consultant.add(psName);
                    }
                    else if(roleName.equals("interviewer")){
                        it.interviewer.add(psName);
                    }
                    String getPersonInfo="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile as isfile, ValueTimestamp as ts, AttributeID as attrid " +
                            "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                            "WHERE uuid = '"+puuid+"'";
                    Statement spsInfo=connection.createStatement();
                    ResultSet spsDetail=spsInfo.executeQuery(getPersonInfo);

                    while(spsDetail.next()){
                        String psAttr=spsDetail.getString("attr");
                        psObj.put(psAttr, spsDetail.getString("val"));
                        if(psAttr.equals("PersonPhoto") && spsDetail.getString("val")!=null){
                            String oldFilename=spsDetail.getString("val");
                            String newPhotoFileName=generatePhotoName(psName,oldFilename, spsDetail.getString("ts"));
                            photoFile.add(newPhotoFileName);

                            String oldFile="files/"+oldFilename.substring(oldFilename.lastIndexOf("/")+1);
                            File oFile=new File(oldFile);
                            File newFile=new File("files/"+newPhotoFileName);
                            oFile.renameTo(newFile);
                            psObj.put("PersonPhotoNewName", newPhotoFileName);
                            it.filename.add(newPhotoFileName);
                        }
                    }
                    psObj.put("PersonRoleInSession",roleName);
                    jPerson.add(psObj);
                }
                obj.put("PersonList",jPerson);
                //session answer and file info
                JSONArray ansList=new JSONArray();
                String getSessionAnswerQuery="select uuid as aid from latestNonDeletedAentValue "+
                        "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') "+
                        "and uuid in "+
                        "(select uuid from AentReln where RelationshipID in "+
                        "(select RelationshipID from AEntReln where AEntReln.uuid='"+uuid+"' "+
                        "AND RelationshipID in "+
                        "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                        "(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') "+
                        "and latestNonDeletedRelationship.Deleted IS NULL)))";
                Statement sAns=connection.createStatement();
                ResultSet sAnsInfo=sAns.executeQuery(getSessionAnswerQuery);
                while(sAnsInfo.next()){
                    String auuid=sAnsInfo.getString("aid");
                    JSONObject ansInfo=new JSONObject();
                    ansInfo.put("AnswerUuid",auuid);
                    String getAnswerBasicInfo="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                            "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                            "WHERE uuid = '"+auuid+"'";
                    String getAnswerFileInfo="select uuid as fid ,measure as flabel from latestNonDeletedAentValue "+
                            "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileID') "+
                            "and uuid in "+
                            "(select uuid from AentReln where RelationshipID in "+
                            "(select RelationshipID from AEntReln where AEntReln.uuid="+auuid+" "+
                            "AND RelationshipID in "+
                            "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                            "(select RelnTypeID from RelnType where RelnTypeName='Answer and File') "+
                            "and latestNonDeletedRelationship.Deleted IS NULL)))";

                    Statement sAnsBasic=connection.createStatement();
                    ResultSet sAnsBasicInfo=sAnsBasic.executeQuery(getAnswerBasicInfo);
                    Statement sAnsFile=connection.createStatement();
                    ResultSet sAnsFileLs=sAnsFile.executeQuery(getAnswerFileInfo);
                    while(sAnsBasicInfo.next()){
                        ansInfo.put(sAnsBasicInfo.getString("attr"), sAnsBasicInfo.getString("val"));
                    }
                    JSONArray filelist=new JSONArray();
                    while(sAnsFileLs.next()){
                        JSONObject fObj=new JSONObject();
                        String fid=sAnsFileLs.getString("fid");
                        String flabel=sAnsFileLs.getString("flabel");
                        fObj.put("FileUuid",fid);
                        String getFileBasicInfo="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                                "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                                "WHERE uuid = '"+fid+"'";
                        Statement fBasic=connection.createStatement();
                        ResultSet fInfo=fBasic.executeQuery(getFileBasicInfo);
                        while(fInfo.next()){
                            String attrName=fInfo.getString("attr");
                            fObj.put(attrName, fInfo.getString("val"));
                            if(attrName.equals("FileContent")){
                                flabel=generateAnsFileName(flabel, fInfo.getString("val"));
                                String oldFilePath=fInfo.getString("val");
                                oldFilePath="files/"+oldFilePath.substring(oldFilePath.lastIndexOf("/")+1);
                                File oldFile=new File(oldFilePath);
                                File newFile=new File("files/"+flabel);
                                oldFile.renameTo(newFile);
                                fObj.put("FileNewLabel",flabel);
                                it.filename.add(flabel);
                            }
                        }
                        filelist.add(fObj);
                    }
                    ansInfo.put("FileList",filelist);
                    ansList.add(ansInfo);
                }
                obj.put("AnswerList",ansList);
                //session fieldtrip info
                String fieldTripQuery="select uuid,measure as FTLabel from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FieldTripID') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+uuid+
                        "' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and FieldTrip')and latestNonDeletedRelationship.Deleted IS NULL)))";
                Statement sFt=connection.createStatement();
                ResultSet rFt=sFt.executeQuery(fieldTripQuery);
                while(rFt.next()){
                    it.fieldTrip=rFt.getString("FTLabel");
                }

                obj.put("FieldTripName",it.fieldTrip);
                //session questionnaire info
                String questionniareQuery="SELECT uuid,measure as quesnir FROM latestNonDeletedAentValue WHERE latestNonDeletedAentValue.AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionnaireName') AND latestNonDeletedAentValue.uuid IN "+
                        "(select measure from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') and uuid in (select uuid from AentReln where RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='"+uuid +
                        "' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') and latestNonDeletedRelationship.Deleted IS NULL))) group by measure)";
                Statement sQuesnir=connection.createStatement();
                ResultSet rQuesnir=sQuesnir.executeQuery(questionniareQuery);
                while(rQuesnir.next()){
                    it.questionnaire.add(rQuesnir.getString("quesnir"));
                }
                JSONArray quesnir=new JSONArray();
                for(String q: it.questionnaire){
                    JSONObject qe=new JSONObject();
                    qe.put("QuestionnaireUsed", q);
                    quesnir.add(qe);
                }
                if(it.desc==null || it.desc.equals("")){
                    //using questionnaire to generate the session description
                    StringBuilder sb=new StringBuilder();
                    for(int i=0; i<it.questionnaire.size();i++){
                        if(i!=it.questionnaire.size()-1){
                            sb.append(it.questionnaire.get(i)+"||");
                        }
                        else{
                            sb.append(it.questionnaire.get(i));
                        }
                    }
                    it.desc=sb.toString();
                }
                obj.put("QuestionnaireList", quesnir);
                sessionInfoList.put(uuid, it);
                generateSessionMetaFile(obj, it.sname);
                it.sJson=it.sname+".json";
            }
            System.out.println("Writing csv file...");
            writeSessionInfoToCSV();
            generateMetadata(connection);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void writeSessionInfoToCSV(){
        File file=createCSVFile();
        try {
            if(file!=null){
                FileOutputStream fos=new FileOutputStream(file);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                String firstRow="dc.title,dc.date.created,dc.coverage.spatial,dc.description,dc.contributor.researcher,dc.contributor.speaker,filename,dc.relation.isPartOf,dc.relation.references,dc.description.sponsorship";
                bw.write(firstRow);
                bw.newLine();
                for(String key: sessionInfoList.keySet()){
                    StringBuilder sb=new StringBuilder();
                    InfoTuple t=sessionInfoList.get(key);
                    sb.append(t.sname+",");
                    sb.append(t.date+",");
                    sb.append(t.loc+",");
                    sb.append(t.desc+",");

                    for(int i=0;i<t.interviewer.size(); i++) {
                        if(i!=t.interviewer.size()-1){
                            sb.append(t.interviewer.get(i)+"||");
                        }
                        else{
                            sb.append(t.interviewer.get(i)+",");
                        }
                    }

                    for(int i=0;i< t.consultant.size(); i++){
                        if(i!=t.consultant.size()-1) {
                            sb.append(t.consultant.get(i) + "||");
                        }
                        else{
                            sb.append(t.consultant.get(i)+",");
                        }
                    }

                    for(int i=0; i<t.filename.size();i++){
                        sb.append(t.filename.get(i)+"||");
                        /*
                        if(i!=t.filename.size()-1){
                            sb.append(t.filename.get(i)+"||");
                        }
                        else{
                            sb.append(t.filename.get(i)+",");
                        }
                        */
                    }
                    sb.append(t.sJson+",");
                    sb.append(t.fieldTrip+",");
                    for(int i=0; i<t.questionnaire.size();i++){
                        if(i!=t.questionnaire.size()-1){
                            sb.append(t.questionnaire.get(i)+"||");
                        }
                        else{
                            sb.append(t.questionnaire.get(i)+",");
                        }
                    }
                    sb.append("The collection of this data was supported by funding from the U.S. National Science Foundation under Award No. BCS-1360763");
                    String s=sb.toString();
                    bw.write(s);
                    bw.newLine();
                }
                bw.close();
                fos.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void generateSessionMetaFile(JSONObject object, String filename){
        try {
            FileWriter fw=new FileWriter("files/"+filename+".json");
            fw.write(object.toJSONString());
            fw.flush();
            fw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private String generatePhotoName(String name, String oldFileName, String timestamp){
        if(name=="") name="nameUnknown";
        String[] time=timestamp.split(" ");
        String extension=oldFileName.substring(oldFileName.lastIndexOf("."));
        String newFileName=name+time[0]+extension;
        newFileName=cleanFileName(newFileName);
        return newFileName;
    }
    private String generateAnsFileName(String name, String oldName){
        String fileExtension=oldName.substring(oldName.lastIndexOf("."));
        name=cleanFileName(name);
        name+=fileExtension;
        return name;
    }
    private String cleanFileName(String fileName){
        fileName= fileName.replaceAll("\\(","_");
        fileName=fileName.replaceAll("\\)","");
        fileName=fileName.replaceAll(" ","");
        fileName=fileName.replaceAll("\\+","plus");
        fileName=fileName.replaceAll("'","");
        return fileName;
    }
    public void generateMetadata(Connection conn){
        //generate fieldTrip metadata file for each fieldTrip
        System.out.println("Generating metadata files...");
        generateFTMetaFile(conn);
        System.out.println("Done creating metadata file for fieldtrip");
        System.out.println("Generating metadata files...");
        generateQuestionnaire(conn);
        System.out.println("Done creating metadata file for questionnaire");
        System.out.println("Done generating metadata files.");
    }
    private void generateFTMetaFile(Connection conn){
        String loadAllFTQuery="SELECT uuid as ftid, measure as ftlabel from latestAllArchEntIdentifiers where AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='FieldTripID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllFT=s1.executeQuery(loadAllFTQuery);
            while(rAllFT.next()){
                JSONObject ftObject=new JSONObject();
                uuid=rAllFT.getString("ftid");
                String ftLabel=rAllFT.getString("ftlabel");

                ftObject.put("FieldTripUuid", uuid);
                ftObject.put("DBFieldTripLabel", ftLabel);
                ftLabel=cleanFileName(ftLabel);
                ftObject.put("FieldTripLabel", ftLabel);

                String getEntityValue="SELECT uuid as ftid, attributename as ftattr, measure as ftval, freetext as ftannotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rFT=s2.executeQuery(getEntityValue);
                if(!rFT.next()){continue;}

                while(rFT.next()){
                    if(!rFT.getString("ftattr").equals("FieldTripID")){
                        ftObject.put(rFT.getString("ftattr"), rFT.getString("ftval"));
                    }
                    if(rFT.getString("ftannotation")!=null){
                        ftObject.put(rFT.getString("ftattr")+"Annotation",rFT.getString("ftannotation"));
                    }
                }
                String getAllSessionFortheFT="select uuid as sid, measure as slabel from latestNonDeletedAentValue "+
                        "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionID') "+
                        "and uuid in "+
                        "(select uuid from AentReln where RelationshipID in "+
                        "(select RelationshipID from AEntReln where AEntReln.uuid='"+uuid+"' "+
                        "AND RelationshipID in "+
                        "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                        "(select RelnTypeID from RelnType where RelnTypeName='Session and FieldTrip') "+
                        "and latestNonDeletedRelationship.Deleted IS NULL)))";
                Statement s3=conn.createStatement();
                ResultSet rFTSession=s3.executeQuery(getAllSessionFortheFT);
                JSONArray sessionLs=new JSONArray();
                while(rFTSession.next()){
                    JSONObject objSss=new JSONObject();
                    objSss.put("SessionUuid",rFTSession.getString("sid"));
                    objSss.put("DBSessionLabel",rFTSession.getString("slabel"));
                    sessionLs.add(objSss);
                }
                ftObject.put("SessionList",sessionLs);
                File newFile=new File("files/"+ftLabel+".json");
                try {
                    FileWriter fw=new FileWriter(newFile);
                    fw.write(ftObject.toJSONString());
                    fw.flush();
                    fw.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void generateQuestionnaire(Connection conn){
        String allQuesnirQuery="select uuid as qid, measure as qlabel from latestNonDeletedArchEntIdentifiers "+
                "where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuestionnaireID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllRes=s1.executeQuery(allQuesnirQuery);
            while(rAllRes.next()){
                JSONObject obj=new JSONObject();
                uuid=rAllRes.getString("qid");
                String qlabel=rAllRes.getString("qlabel");
                obj.put("QuestionnaireUuid",uuid);
                obj.put("DBQuesnirLabel", qlabel);
                qlabel=cleanFileName(qlabel);

                obj.put("QuesnirLabel", qlabel);
                String getEntityValue="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rRes=s2.executeQuery(getEntityValue);
                if(!rRes.next()){continue;}

                while(rRes.next()){
                    if(!rRes.getString("attr").equals("QuestionnaireID")){
                        obj.put(rRes.getString("attr"), rRes.getString("val"));
                    }

                    if(rRes.getString("annotation")!=null){
                        obj.put(rRes.getString("attr")+"Annotation",rRes.getString("annotation"));
                    }
                }

                String loadQuesContentandOrderQuery="select t1.quesId as qid, t1.quesOrder as qorder, t2.quesContent as qContent from "+
                        "(select quId.measure as quesId, qOrder.measure as quesOrder "+
                        "from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder "+
                        "where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') "+
                        "and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') "+
                        "and quId.uuid=qOrder.uuid "+
                        "and quId.measure in "+
                        "(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir "+
                        "where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') "+
                        "and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') "+
                        "and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='"+uuid+"') "+
                        "and qOrder.uuid in "+
                        "(select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') "+
                        "and measure='"+uuid+"')) t1 "+
                        "inner join "+
                        "(select qId.measure as quesId, qContent.measure as quesContent "+
                        "from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent, latestNonDeletedAentValue as qLanguage "+
                        "where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') "+
                        "and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') "+
                        "and qLanguage.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesLangUuid') "+
                        "and qId.uuid=qContent.uuid "+
                        "and qId.uuid=qLanguage.uuid "+
                        "and qLanguage.measure IN (select eng.uuid from latestNonDeletedAentValue as eng where eng.AttributeID=(select AttributeID from AttributeKey where AttributeName='LanguageName') "+
                        "and eng.measure='English' and eng.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='LanguageID'))) "+
                        "and qId.measure in "+
                        "(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir "+
                        "where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') "+
                        "and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') "+
                        "and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='"+uuid+"') "+
                        "and qId.uuid in "+
                        "(select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))) t2 "+
                        "on t1.quesId=t2.quesId group by t1.quesId";
                Statement s3=conn.createStatement();
                ResultSet rQuesLs=s3.executeQuery(loadQuesContentandOrderQuery);
                JSONArray quesLs=new JSONArray();

                while(rQuesLs.next()){
                    JSONObject ques=new JSONObject();
                    ques.put("QuestionUuid", rQuesLs.getString("qid"));
                    ques.put("QuestionOrder",rQuesLs.getString("qorder"));
                    ques.put("Question",rQuesLs.getString("qContent"));
                    quesLs.add(ques);
                }
                obj.put("QuestionList", quesLs);
                FileWriter fw=new FileWriter("files/"+qlabel+"-Questionnaire"+".json");
                fw.write(obj.toJSONString());
                fw.flush();
                fw.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    /*
    private void writeSessionBasicInfo(Connection conn,  File csv){
        String getAllSessionIdQuery="SELECT sidloc.Sid AS Sid, Sname, Loc, sdsc  FROM "+
                "(((SELECT uuid as id, measure as Loc FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionLocation')) sloc "+
                "INNER JOIN "+
                "(SELECT uuid as Sid,measure as Sname FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')) sidentifier on sloc.id=sidentifier.sid) sidloc "+
                "INNER JOIN "+
                "(SELECT uuid as id, measure as sdsc FROM latestNonDeletedAentValue WHERE AttributeID=(SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionDescription')) sdesc on "+
                "sidloc.sid=sdesc.id)";
        try{
            Statement statement=conn.createStatement();
            try {
                ResultSet rcFile = statement.executeQuery(getAllSessionIdQuery);
                if(!csv.isFile()){
                    csv.createNewFile();
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
            HashMap<String, ArrayList<String>> fileInfo=new HashMap<>();// use for check all files
            if(!csv.isFile()){
                csv.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(csv);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
            String firstRow="sessionuuid, dc.title, dc.coverage.spatial, dc.description, dc.contributor.researcher, dc.contributor.speaker, filename, dc.relation.isPartOf,dc.relation.references";
            bw.write(firstRow);
            bw.newLine();

            for(String key: sssList.keySet()){
                sid=key;
                InfoTuple tmp=sssList.get(key);
                //create


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
                String answeQuery="select uuid,measure from latestNonDeletedAentValue "+
                        "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') "+
                        "and uuid in "+
                        "(select uuid from AentReln where RelationshipID in "+
                        "(select RelationshipID from AEntReln where AEntReln.uuid='"+sid+"' "+
                        "AND RelationshipID in "+
                        "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                        "(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') "+
                        "and latestNonDeletedRelationship.Deleted IS NULL)))";
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
                        sb.append(t.consultant.get(i) + "||");
                    }
                    else{
                        sb.append(t.consultant.get(i)+",");
                    }
                }

                for(int i=0; i<t.filename.size();i++){
                    if(i!=t.filename.size()-1){
                        sb.append(t.filename.get(i)+"||");
                    }
                    else{
                        sb.append(t.filename.get(i)+",");
                    }
                }
                sb.append(t.fieldTrip+",");
                for(int i=0; i<t.questionnaire.size();i++){
                    if(i!=t.questionnaire.size()-1){
                        sb.append(t.questionnaire.get(i)+"||");
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
            System.out.println("Renaming files...");
            renameFiles(fileInfo);
            System.out.println("Updating database...");
            populateUpdateToDB(conn);
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
        try {
            if(filels.containsKey(filename)){
                dupFile=true;
                //rename existing file
                ArrayList<String> tmp=filels.get(filename);
                filels.remove(filename);
                String udfilename=filename+"(1)";
                filels.put(udfilename, tmp);
                for(int i=0;i<tuple.filename.size();i++){
                    if(tuple.filename.get(i).equals(filename)){
                        tuple.filename.set(i,filename+"(1)");
                    }
                }
                fileUpdate.put(filels.get(udfilename).get(1), udfilename);
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
                fileUpdate.put(fileId, newFilename);
            }
            else{
                ArrayList fileInfo=new ArrayList<>();
                fileInfo.add(filePath);
                fileInfo.add(fileId);
                filels.put(filename,fileInfo);
                tuple.filename.add(filename);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void populateUpdateToDB(Connection conn){
        String attributeIDQuery="SELECT AttributeID as aid FROM AttributeKey WHERE AttributeName='FileID'";
        String attributeID="";
        try{
            Statement s1=conn.createStatement();
            ResultSet ra=s1.executeQuery(attributeIDQuery);
            while(ra.next()){
                attributeID=ra.getString("aid");
            }
            for(String key: fileUpdate.keySet()){
                String updateQuery="UPDATE AentValue SET Measure='"+ fileUpdate.get(key)+ "' WHERE AttributeID='"
                        +attributeID+"' AND UUID='"+key+"'";
                Statement u1=conn.createStatement();
                u1.executeUpdate(updateQuery);
                u1.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    private String getFileExtension(String filePath){
        int i=filePath.lastIndexOf(".");
        if (i>0){
            return filePath.substring(i);
        }
        return null;
    }
    */

    /*
    private void  generateSessionMetaFile(Connection conn){
        String allSessionQuery="SELECT uuid as Sid,measure as Sname FROM latestNonDeletedArchEntIdentifiers WHERE AttributeID = (SELECT AttributeID FROM AttributeKey WHERE AttributeName='SessionID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllSSS=s1.executeQuery(allSessionQuery);
            while(rAllSSS.next()){
                uuid=rAllSSS.getString("Sid");

                String loadAnswerForSessionQuery="select uuid as aid,measure as alabel from latestNonDeletedAentValue "+
                        "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') "+
                        "and uuid in "+
                        "(select uuid from AentReln where RelationshipID in "+
                        "(select RelationshipID from AEntReln where AEntReln.uuid='"+uuid+"' "+
                        "AND RelationshipID in "+
                        "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                        "(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') "+
                        "and latestNonDeletedRelationship.Deleted IS NULL)))";
                Statement s2=conn.createStatement();
                ResultSet rAnsLs=s2.executeQuery(loadAnswerForSessionQuery);
                if(!rAnsLs.next()){continue;}

                File ftFile=createMetaDataFile(rAllSSS.getString("Sname")+".json");
                if(ftFile==null){
                    //TODO: write log here
                    continue;
                }
                FileOutputStream fos=new FileOutputStream(ftFile);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("{");
                bw.newLine();
                bw.write("\"UUID\":\""+uuid+"\",");
                bw.newLine();
                bw.write("\"answerList\":[");
                bw.newLine();
                while(rAnsLs.next()){
                    bw.write("{");
                    bw.write("\"answerUuid\":\""+rAnsLs.getString("aid")+"\",");
                    bw.newLine();
                    bw.write("\"answerLabel\":\""+rAnsLs.getString("alabel")+"\"");
                    bw.newLine();
                    bw.write("}");
                    break;
                }

                while(rAnsLs.next()){
                    bw.write(",");
                    bw.newLine();
                    bw.write("{");
                    bw.newLine();
                    bw.write("\"answerUuid\":\""+rAnsLs.getString("aid")+"\",");
                    bw.newLine();
                    bw.write("\"answerLabel\":\""+rAnsLs.getString("alabel")+"\"");
                    bw.newLine();
                    bw.write("}");
                }
                bw.newLine();
                bw.write("],");
                bw.newLine();
                String loadSssPersonRoleQuery="select t5.relnId, t6.personRoleUuid as prid, t5.personUuid as psId, t6.personRoleName as prname, t5.persName as psname "+
                        "from (select t1.personId as personUuid, t1.personName as persName, t2.psssId as relnId from (select pId.uuid as personId, pId.measure as personName "+
                        "from latestNonDeletedAentValue as pId where pId.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='PersonName') "+
                        "and pId.uuid in (select psName.measure from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln "+
                        "where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') "+
                        "and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psName.uuid=psReln.uuid and psName.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey "+
                        "where AttributeName='SessionIDforPerson')))) t1 inner join "+
                        "(select psName.uuid as psssId, psName.measure as psId from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln "+
                        "where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') "+
                        "and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psName.uuid=psReln.uuid) t2 on t1.personId=t2.psId) t5 "+
                        "inner join "+
                        "(select t3.roleId as personRoleUuid, t3.roleName as personRoleName, t4.rsssId as relnId from (select rId.uuid as roleId, rId.measure as roleName "+
                        "from latestNonDeletedAentValue as rId where rId.AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonRoleName') "+
                        "and rId.uuid in (select psRole.measure from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln "+
                        "where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID "+
                        "from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' and psRole.uuid=psReln.uuid))t3 "+
                        "inner join (select psRole.uuid as rsssId, psRole.measure as tempPsRoleID from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln "+
                        "where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID "+
                        "from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='"+uuid+"' "+
                        "and psRole.uuid=psReln.uuid)t4 on t3.roleId=t4.tempPsRoleID) t6 on t5.relnId =t6.relnId";
                Statement s3=conn.createStatement();
                ResultSet rPsLs=s3.executeQuery(loadSssPersonRoleQuery);
                bw.write("\"personList\":[");
                bw.newLine();
                while(rPsLs.next()){
                    bw.write("{");
                    bw.write("\"personUuid\":\""+rPsLs.getString("psId")+"\",");
                    bw.newLine();
                    bw.write("\"personRoleUuid\":\""+rPsLs.getString("prid")+"\",");
                    bw.newLine();
                    bw.write("\"personName\":\""+rPsLs.getString("psname")+"\",");
                    bw.newLine();
                    bw.write("\"personRoleName\":\""+rPsLs.getString("prname")+"\"");
                    bw.newLine();
                    bw.write("}");
                    break;
                }
                while(rPsLs.next()){
                    bw.write(",");
                    bw.newLine();
                    bw.write("{");
                    bw.newLine();
                    bw.write("\"personUuid\":\""+rPsLs.getString("psId")+"\",");
                    bw.newLine();
                    bw.write("\"personRoleUuid\":\""+rPsLs.getString("prid")+"\",");
                    bw.newLine();
                    bw.write("\"personName\":\""+rPsLs.getString("psname")+"\",");
                    bw.newLine();
                    bw.write("\"personRoleName\":\""+rPsLs.getString("prname")+"\"");
                    bw.newLine();
                    bw.write("}");
                }
                bw.newLine();
                bw.write("],");
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
                bw.write("\"UUID\":\""+uuid+"\"");
                while(rFT.next()){
                    bw.write(",");
                    bw.newLine();
                    String data="\""+rFT.getString("pattr")+"\":\""+rFT.getString("psval")+"\"";
                    bw.write(data);
                    if(rFT.getString("annotation")!=null){
                        bw.write(",");
                        bw.newLine();
                        bw.write("\"annotation\":\""+rFT.getString("annotation")+"\"");
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
    private void generateAnswerMetaFile(Connection conn){// todo: generate file list
        String allPersonQuery="SELECT uuid as id,measure as label FROM latestNonDeletedArchEntIdentifiers " +
                "WHERE latestNonDeletedArchEntIdentifiers.AttributeID "+
                "= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='AnswerLabel')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllPS=s1.executeQuery(allPersonQuery);
            while(rAllPS.next()){
                uuid=rAllPS.getString("id");

                String getEntityValue="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rFT=s2.executeQuery(getEntityValue);
                if(!rFT.next()){continue;}

                File psFile=createMetaDataFile(rAllPS.getString("label")+".json");
                if(psFile==null){
                    //TODO: write log here
                    continue;
                }
                FileOutputStream fos=new FileOutputStream(psFile);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("{");
                bw.newLine();
                bw.write("\"UUID\":\""+uuid+"\"");
                while(rFT.next()){
                    bw.write(",");
                    bw.newLine();
                    String data="\""+rFT.getString("attr")+"\":\""+rFT.getString("val")+"\"";
                    bw.write(data);
                    if(rFT.getString("annotation")!=null){
                        bw.write(",");
                        bw.newLine();
                        bw.write("\"annotation\":\""+rFT.getString("annotation")+"\"");
                    }
                }
                bw.write(",");
                bw.newLine();
                String loadFileForAnswerQuery="select uuid as fid ,measure as flabel from latestNonDeletedAentValue "+
                        "where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileID') "+
                        "and uuid in "+
                        "(select uuid from AentReln where RelationshipID in "+
                        "(select RelationshipID from AEntReln where AEntReln.uuid="+uuid+" "+
                        "AND RelationshipID in "+
                        "(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
                        "(select RelnTypeID from RelnType where RelnTypeName='Answer and File') "+
                        "and latestNonDeletedRelationship.Deleted IS NULL)))";
                Statement s3=conn.createStatement();
                ResultSet rFileLs=s3.executeQuery(loadFileForAnswerQuery);
                bw.write("\"answerFileList\":[");
                bw.newLine();
                while (rFileLs.next()){
                    bw.write("{");
                    bw.write("\"fileId\":\""+rFileLs.getString("fid")+"\",");
                    bw.newLine();
                    bw.write("\"fileLabel\":\""+rFileLs.getString("flabel")+"\"");
                    bw.newLine();
                    bw.write("}");
                    break;
                }

                while (rFileLs.next()){
                    bw.write(",");
                    bw.newLine();
                    bw.write("{");
                    bw.newLine();
                    bw.write("\"fileId\":\""+rFileLs.getString("fid")+"\",");
                    bw.newLine();
                    bw.write("\"fileLabel\":\""+rFileLs.getString("flabel")+"\"");
                    bw.newLine();
                    bw.write("}");
                }
                bw.newLine();
                bw.write("],");
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
    private void generateFileMetaFile(Connection conn){
        String allFileQuery="SELECT uuid as id,measure as label FROM latestNonDeletedArchEntIdentifiers " +
                "WHERE latestNonDeletedArchEntIdentifiers.AttributeID "+
                "= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='FileID')";
        String uuid="";
        try {
            Statement s1=conn.createStatement();
            ResultSet rAllRes=s1.executeQuery(allFileQuery);
            while(rAllRes.next()){
                uuid=rAllRes.getString("id");

                String getEntityValue="SELECT uuid as id, attributename as attr, measure as val, freetext as annotation, attributetype, attributeisfile " +
                        "FROM latestNonDeletedArchent JOIN latestNonDeletedAentvalue AS av using (uuid) JOIN attributekey using (attributeid) " +
                        "WHERE uuid = '"+uuid+"'";
                Statement s2=conn.createStatement();
                ResultSet rRes=s2.executeQuery(getEntityValue);
                if(!rRes.next()){continue;}

                File fFile=createMetaDataFile(rAllRes.getString("label")+".json");
                if(fFile==null){
                    //TODO: write log here
                    continue;
                }
                FileOutputStream fos=new FileOutputStream(fFile);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(fos));
                bw.write("{");
                bw.newLine();
                bw.write("\"UUID\":\""+uuid+"\"");
                while(rRes.next()){
                    bw.write(",");
                    bw.newLine();
                    String data="\""+rRes.getString("attr")+"\":\""+rRes.getString("val")+"\"";
                    bw.write(data);
                    if(rRes.getString("annotation")!=null){
                        bw.write(",");
                        bw.newLine();
                        bw.write("\"annotation\":\""+rRes.getString("annotation")+"\"");
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
    */

}