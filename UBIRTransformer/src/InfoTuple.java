import java.util.ArrayList;

/**
 * Created by ning on 7/13/17.
 */
public class InfoTuple {
    public String sid;
    public String sname;
    public String loc;
    public String desc;
    public ArrayList<String> interviewer;
    public ArrayList<String> consultant;
    public ArrayList<String> filename;
    public String fieldTrip;
    public ArrayList<String> questionnaire;
    public ArrayList<FileQuesnirPair> fileQuesnir;
    public InfoTuple(String sid, String sname, String loc, String desc, ArrayList<String> interviewer, ArrayList<String> consultant, ArrayList<String> filename, String fieldTrip, ArrayList<String> questionnaire){
        this.sid=sid;
        this.sname=sname;
        this.loc=loc;
        this.desc=desc;
        this.interviewer=interviewer;
        this.consultant=consultant;
        this.filename=filename;
        this.fieldTrip=fieldTrip;
        this.questionnaire=questionnaire;
    }
}
