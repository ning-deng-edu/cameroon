import java.util.concurrent.Callable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.util.Log;
import java.io.File;
/*** 'Editable' - you can edit the code below based on the needs ***/
User user; // don't touch
String userid;
/*Common queries*/
loadAllQuestionQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionContent') "+
	"GROUP BY uuid;";

loadAllQuestionnaireQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionnaireName') "+
	"GROUP BY uuid;";
	
loadAllPersonQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='PersonName') "+
	"GROUP BY uuid;";

loadAllLanguageQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='LanguageName') "+
	"GROUP BY uuid;";

loadAllUserQuery="select userid, fname || ' ' || lname from user where userdeleted is null";
/*Common queries end*/

/***Query instance examples***/
//SELECT uuid,measure FROM latestNonDeletedAentValue  WHERE latestNonDeletedAentValue.AttributeID  IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionContent')  GROUP BY uuid
//select uuid, group_concat(coalesce(measure,' '),'-') as response from AentValue where AentValue.AttributeID in (select AttributeID from AttributeKey where AttributeName="QuestionContent")
/*"SELECT uuid, measure as response " +
	//fetchAll("SELECT * " +
    "FROM AentValue " +
    "WHERE AentValue.AttributeID IN " +
    "(SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionnaireName') " +
	"GROUP BY uuid " +
    "order by response;"*/
/*
		select uuid,measure from latestNonDeletedAentValue
		where latestNonDeletedAentValue.AttributeID in
		(select AttributeID from AttributeKey where AttributeName='QuestionContent')
		and uuid in
		( select AEntReln.uuid from AEntReln where AEntReln.uuid <> "1000011435155888113"
		 and AEntReln.RelationshipID in (select AEntReln.RelationshipID from AEntReln where AEntReln.uuid="1000011435155888113"))
		questionList_show_in_questionnaire=new ArrayList();*/
		
		/*select  relationshipid from AEntReln aer
		inner join (select max(AEntRelnTimestamp) as maxtime
		from AEntReln where AEntReln.uuid ='1000011435604032139')  tm
		on aer.AentRelnTimestamp=tm.maxtime group by relationshipid*/
		
		/*select uuid, measure from latestNonDeletedAentValue
		where latestNonDeletedAentValue.AttributeID in
		(select AttributeID from AttributeKey where AttributeName='QuestionContent')
		and uuid in
		(select AEntReln.uuid from AEntReln where AEntReln.uuid<>'1000011435604032139' and AEntReln.RelationshipID IN
		(select  relationshipid from AEntReln aer inner join
		(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ='1000011435604032139')  tm
		on aer.AentRelnTimestamp=tm.maxtime group by relationshipid)) group by uuid*/
		
		
		/*select uuid, measure from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID in
		(select AttributeID from AttributeKey where AttributeName='QuestionContent')
		and uuid in (select AEntReln.uuid from AEntReln where AEntReln.uuid<>'1000011435604032139' and AEntReln.RelationshipID IN
		(select  relationshipID from AEntReln aer inner join(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ='1000011435604032139') tm
		on aer.AentRelnTimestamp=tm.maxtime group by relationshipID))group by uuid*/

/*
			questionnaire_question_query="SELECT uuid, measure "+
			"FROM latestNonDeletedAentValue "+
			"WHERE latestNonDeletedAentValue.AttributeID IN "+
			"(SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionContent') " +
			"AND uuid IN "+
			"(SELECT AEntReln.UUID FROM AEntReln WHERE AEntReln.UUID <> "+ current_questionnaire_id +" "+
			"AND AEntReln.RelationshipID IN "+
			"(SELECT AEntReln.RelationshipID FROM AEntReln aer INNER JOIN "+
			"(SELECT max(AEntRelnTimestamp) AS MaxTime FROM AEntReln "+
			"WHERE AEntReln.uuid ="+current_questionnaire_id+") tm "+
			"ON aer.AEntRelnTimestamp=tm.MaxTime "+
			"GROUP BY RelationshipID)) "+
			//"(SELECT relationshipID, max(AEntRelnTimestamp) as MaxTime "+
			//"FROM (select * from AEntReln where AEntReln.uuid=" +current_questionnaire_id +"))"+
			//"(SELECT AEntReln.RelationshipID FROM AEntReln WHERE AEntReln.uuid="+current_questionnaire_id+")) " +
			"GROUP BY uuid;";
			*/
//SELECT uuid, measure as response FROM AentValue WHERE AentValue.AttributeID IN (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionContent') GROUP BY uuid order by response;
//select uuid, group_concat(coalesce(measure,' '),'-') as response from AentValue where AentValue.AttributeID in (select AttributeID from AttributeKey where AttributeName="QuestionContent")
//search_star="select uuid, response from latestNonDeletedArchEntFormattedIdentifiers where aenttypename = '" + type + "' limit ? offset ?;";
//select uuid, measure as response from AentValue where AentValue.AttributeID in(select attributeID from AttributeKey where AttributeName='QuestionContent') AND uuid in (select AEntReln.UUID from AEntReln where AEntReln.UUID <> ' 1000011435168833835' AND AEntReln.RelationshipID in (select AEntReln.RelationshipID from AEntReln where AEntReln.uuid='1000011435168833835')) group by uuid order by response


/***Query instance example ends***/

/***Enable data and file syncing***/
addActionBarItem("sync", new ToggleActionButtonCallback() {
    actionOnLabel() {
        "Disable Sync";
    }
    actionOn() {
        setSyncEnabled(false);
        setFileSyncEnabled(false);
        showToast("Sync disabled.");
    }
    isActionOff() {
        isSyncEnabled();
    }
    actionOffLabel() {
        "Enable Sync";
    }
    actionOff() {
        setSyncEnabled(true);
        setFileSyncEnabled(true);
        showToast("Sync enabled.");
    }
});
/***Save relationships among entities***/
saveEntitiesToRel(String type, String entity1, String entity2) {
    if (isNull(entity1) || isNull(entity2)) return;
    saveRel(null, type, null, null, new SaveCallback() {
        onSave(rel_id, newRecord) {
            addReln(entity1, rel_id, null);
            addReln(entity2, rel_id, null);
        }
    });
}

/*** USER ***/
onEvent("user/usertab/users", "click", "login()");
onEvent("user/usertab/user_Search", "click", "userSearch()");

loadUsers() {
    fetchAll(loadAllUserQuery, new FetchCallback() {
        onFetch(result) {
            populateList("user/usertab/users", result);
        }
    });
}

loadUsers();

String username = "";
String device = "";

login() {
    fetchOne("select userid,fname,lname,email from user where userid='" + getListItemValue() + "';", new FetchCallback() {
        onFetch(result) {
            user = new User(result.get(0),result.get(1),result.get(2),result.get(3));
            setUser(user);
            username = result.get(1) + " " + result.get(2);
            showTabGroup("control");
        }
    });
}
userSearch(){
	String userKeyword=getFieldValue("user/usertab/user_keyword");
	if((isNull(userKeyword)) || userKeyword.equals("*")){
		 fetchAll(loadAllUserQuery, new FetchCallback() {
		        onFetch(result) {
		            populateList("user/usertab/users", result);
		        }
		    });
	}
	else{
		searchUserQuery="select userid, fname || ' ' || lname from user where userdeleted is null and fname like '%"
						+userKeyword+"%'or lname like '%"+userKeyword+"%'";
		fetchAll(searchUserQuery, new FetchCallback() {
	        onFetch(result) {
	            populateList("user/usertab/users", result);
	        }
	    });
	}
}
/***Questionnaire***/

onEvent("control/questionnaire_control/New_Questionnaire","click","newQuestionnaire()");
onEvent("control/questionnaire_control","show","loadQuestionnaire()");
onEvent("control/questionnaire_control/questionnaireList","click","loadQuestionnaireInfo()");
onEvent("questionnaire/questionnaire_info/Start_Question_Selection","click","startQuestionSelection()");
onEvent("questionnaire_question/questionnaire_question_info","show","initializeQuestionSelect()");
onEvent("questionnaire_question/questionnaire_question_info/questionList","click","questionSelection()");
onEvent("questionnaire_question/questionnaire_question_info/questionInQuestionnaire","click","deleteSelectedQuestion()");
onEvent("questionnaire_question/questionnaire_question_info/New_Question","click","newQuestionFromQuestionnaire()");
onEvent("questionnaire_question/questionnaire_question_info/Finish_Questionnaire_Creation","click","finishCreateQuestionnaire()");
onEvent("questionnaire_question/questionnaire_question_info/Change_Questionnaire","click","finishChangeQuestionnaire()");
onEvent("questionnaire_question/questionnaire_question_info/Search_Question","click","searchQuestion()");
onEvent("questionnaire", "show", "showTab(\"questionnaire/questionnaire_info\");");

finished_questionnaire_id=null;//flag for marking if any questionnaire is selected
questionCandidates=new ArrayList();//original questions to be selected in questionnaire
questionCandidatesContent=new ArrayList();//manipulatable question list to be selected in questionnaire
questionSelected=new ArrayList();//questions selected in questionnaire
questionOriginal=new ArrayList();//when changing questionnaire, this is for showing whether or not the questions in questionnaire are changed
questionnaireInfoOriginal=new ArrayList();//Comparing whether or not the questionnaire basic info is changed
questionnaireInfoNew=new ArrayList();//Comparing whether or not the questionnaire basic info is changed
quesListRelnOrigin=new ArrayList();//Storing the existing reln between questions and questionnaire, used for deletion
//flag_return_questionnaire=false;
//starting point of creating new questionnaire 
newQuestionnaire(){	
    newTabGroup("questionnaire");
    questionCandidates.clear();
    questionCandidatesContent.clear();
    questionSelected.clear();
    questionOriginal.clear();
    questionnaireInfoOriginal.clear();
    questionnaireInfoNew.clear();
    quesListRelnOrigin.clear();
	finished_questionnaire_id=null;
	setFieldValue("questionnaire/questionnaire_info/questionnaireID","Quesnir-"+username+"-"+getCurrentTime());
	//flag_return_questionnaire=false;
}

//load all questionnaire names
loadQuestionnaire(){
	finished_questionnaire_id=null;
	existing_questionnaires=new ArrayList();
	/*The fetchAll query returns the uuid and the question content of all the questions*/
	fetchAll(
	loadAllQuestionnaireQuery,
	new FetchCallback() {
        onFetch(result) {
			if (!isNull(result)) {
				existing_questionnaires.addAll(result);
				//for (re:result) {
				//existing_questionnaires.add(re);
				//}
			
			}
			populateList("control/questionnaire_control/questionnaireList", existing_questionnaires);
        }

        onError(message) {
            showToast(message);
        }
    });
}

//load the questionnaire basic info to the page
loadQuestionnaireInfo(){
	questionnaireInfoOriginal.clear();
	questionnaireInfoNew.clear();
	finished_questionnaire_id=getListItemValue();
	if(isNull(finished_questionnaire_id)){
		showToast("No Questionnaire selected");
		return;
	}
	//showWarning("questionnaireID",finished_questionnaire_id.toString());
	showTabGroup("questionnaire", finished_questionnaire_id, new FetchCallback() {
        onFetch(result) {
            questionnaire=result;		
            showToast("Loaded questionnaire"+questionnaire.getId());
			//showWarning("result class",result.getClass().getName());
			questionnaireInfoOriginal.add(getFieldValue("questionnaire/questionnaire_info/questionnaireID"));
			questionnaireInfoOriginal.add(getFieldValue("questionnaire/questionnaire_info/questionnaireName"));
			questionnaireInfoOriginal.add(getFieldValue("questionnaire/questionnaire_info/questionnaireDescription"));
        }
        onError(message) {
            showToast(message);
        }
    });
}

//preparation for the question selection
startQuestionSelection(){   
    if((isNull(getFieldValue("questionnaire/questionnaire_info/questionnaireName")))||(isNull(getFieldValue("questionnaire/questionnaire_info/questionnaireID")))) {
            showWarning("Validation Error", "You must fill in the Questionnaire ID/Name before you can continue");
            return;
        } else {
        	questionnaire_id=getFieldValue("questionnaire/questionnaire_info/questionnaireID");
            questionnaire_name = getFieldValue("questionnaire/questionnaire_info/questionnaireName");
			questionnaireInfoNew.add(questionnaire_id);
			questionnaireInfoNew.add(questionnaire_name);
			questionnaireInfoNew.add(getFieldValue("questionnaire/questionnaire_info/questionnaireDescription"));
            showTabGroup("questionnaire_question");
            //cancelTab(\"questionnaire/questionnaire_info\", false);
            //setFieldValue("questionnaire_question/questionnaire_question_info/questionnaireID", questionnaire_id);
            setFieldValue("questionnaire_question/questionnaire_question_info/questionnaireName", questionnaire_name);
			/*if(!isNull(finished_questionnaire_id)){
				flag_return_questionnaire=true;
			}*/
        }
}


		
//when there is a selected questionnaire, basically show all the data in this questionnaire		
//when there is no selected questionnaire, populate question candidate list for selection
newQuestionFromQuestionnaire(){
	//flag_return_questionnaire=true;
	newTabGroup("questionBank");
	onEvent("questionBank", "show", "showTab(\"questionBank/questionInfo\");");  
}


initializeQuestionSelect(){
	//the query result is an ArrayList, and each item in query result is an ArrayList too
		questionCandidates.clear();
		questionCandidatesContent.clear();
		questionSelected.clear();
		questionOriginal.clear();
		setFieldValue("questionnaire_question/questionnaire_question_info/keywordOfQuestion", "*");
		//showWarning("All clear","clear done");
	//the query result is an ArrayList, and each item in query result is an ArrayList too
	if(isNull(finished_questionnaire_id)){
		//showWarning("no id","new questionnaire");
		/*
		questionCandidates.clear();
		questionCandidatesContent.clear();
		questionSelected.clear();
		*/
		//showWarning("list cleared",finished_questionnaire_id);
		populateList("questionnaire_question/questionnaire_question_info/questionInQuestionnaire", questionSelected);
		fetchAll(loadAllQuestionQuery,
		new FetchCallback() {
			//showWarning("fetched",finished_questionnaire_id);
			onFetch(result) {
				if (!isNull(result)) {
					//showWarning("fetch result is not null",finished_questionnaire_id);
					questionCandidates.addAll(result);
					questionCandidatesContent.addAll(result);
					//for (re:result) {
					//questionCandidatesContent.add(re);
				//showWarning("resultclass",result.getClass().getName());
				//showWarning("class",re.get(0).toString());
				//questionBankList.add(re);
				//}
				populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
			}
			
        }

		onError(message) {
            showToast(message);
        }
		});
		//showWarning("questionCandidatesContent size ",questionCandidatesContent.size());
	}
	
	else{
		//showWarning("has id",finished_questionnaire_id);
		//Show questions in this questionnaire
		current_questionnaire_id=finished_questionnaire_id;
		tempPopulateQuestionList=new ArrayList();
		tempPopulateQuestionList.clear();
		if(isNull(current_questionnaire_id))
			{
				showWarning("Message","Something went wrong! please contact the Admin!");
				return;
			}
		quesnir_ques_reln_query="select RelationshipID from AentReln where AentReln.uuid="+current_questionnaire_id+" "+
				"and RelationshipID in "+
				"(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
					"(select RelnTypeID from RelnType where RelnTypeName='Questionnaire and Question') "+
				"and latestNonDeletedRelationship.Deleted IS NULL)";
		fetchAll(quesnir_ques_reln_query, new FetchCallback() {
	        onFetch(result) {
	        	quesListRelnOrigin.clear();
	        	quesListRelnOrigin.addAll(result);
	        }

	        onError(message) {
	            showToast(message);
	        }
	    });
		questionnaire_question_query="select uuid,measure from latestNonDeletedAentValue "+ 
				"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionContent') "+
				"and uuid in "+
	 			"(select uuid from AentReln where RelationshipID in "+
				"(select RelationshipID from AEntReln where AEntReln.uuid="+current_questionnaire_id+" "+
	 			"AND RelationshipID in "+
				"(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
	 			"(select RelnTypeID from RelnType where RelnTypeName='Questionnaire and Question') "+
				"and latestNonDeletedRelationship.Deleted IS NULL))) group by uuid";
		/*
		questionnaire_question_query="SELECT uuid, measure "+
		"from latestNonDeletedAentValue "+
		"where latestNonDeletedAentValue.AttributeID in "+
		"(select AttributeID from AttributeKey where AttributeName='QuestionContent') "+
		"and uuid in "+
		"(select AEntReln.uuid from AEntReln where AEntReln.uuid<>"+current_questionnaire_id+" "+
		"and AEntReln.RelationshipID IN "+
		"(select  relationshipID from AEntReln aer "+
		"inner join "+
		"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_questionnaire_id+") tm "+
		"on aer.AentRelnTimestamp=tm.maxtime group by relationshipID)) "+
		"group by uuid;";
		*/
			fetchAll(questionnaire_question_query,
				new FetchCallback() {
			//showWarning("fetched corresponding questions",current_questionnaire_id);
					onFetch(result) {
						if (!isNull(result)) {
				//showWarning("fetched result is not null",current_questionnaire_id);
							questionSelected.addAll(result);
							questionOriginal.addAll(result);
							//showWarning("questionSelected",questionSelected.size());
							//showWarning("questionOriginal",questionOriginal.size());
							
							populateList("questionnaire_question/questionnaire_question_info/questionInQuestionnaire", questionSelected);
						}
			//populateList("questionnaire_question/questionnaire_question_info/questionList", tempPopulateQuestionList);
					}
			       
				onError(message) {
					showToast(message);
				}
				});
			
			//questionOriginal.addAll(questionSelected);
			//showWarning("questionOriginal",questionOriginal.size());
		
			fetchAll(loadAllQuestionQuery,
				new FetchCallback() {
			//showWarning("fetched",finished_questionnaire_id);
					onFetch(result) {
						if (!isNull(result)) {
						//showWarning("changing questionnaire",finished_questionnaire_id);
						questionCandidates.addAll(result);
						questionCandidatesContent.addAll(result);
						//for (re:result) {
							//questionCandidatesContent.add(re);
				//showWarning("resultclass",result.getClass().getName());
				//showWarning("class",re.get(0).toString());
				//questionBankList.add(re);
						//}
					questionCandidatesContent.removeAll(questionSelected);
					populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
					}
			
				}

				onError(message) {
				    showToast(message);
				}
				});
			/*
			if(isNull(questionSelected)){
			showWarning("questionSelected Size","null");
			//showWarning("questionOriginal Size",questionOriginal.size());
			//showWarning("questionCandidatesContent Size",questionCandidatesContent.size());
	       }
	       */
		}

	
}

			
//after selected a question, add this question to questionSelected, and remove it from questionCandidatesContent
questionSelection(){
	questionnaire_question_id=getListItemValue();
	int idx_question=-1;
	if(isNull(questionnaire_question_id)){
		showToast("No Question selected");
		return;
	}
	else{
		//showWarning("start",questionCandidatesContent.size().toString());
		//showWarning("startsize",questionSelected.size().toString());
		for (int i=0; i<questionCandidatesContent.size();i++){
			if (questionCandidatesContent.get(i).toString().contains(questionnaire_question_id)) {
				idx_question=i;
				//showWarning("get",questionCandidatesContent.get(idx_question).toString());
				break;
			}
		}
		if (idx_question>=0) {
			//showWarning("index",idx_question.toString());
		//showWarning("get",questionCandidatesContent.get(idx_question).toString());
		questionSelected.add(questionCandidatesContent.get(idx_question));
		//showWarning("questionSelectedadded",questionSelected.size().toString());
		questionCandidatesContent.remove(idx_question);
		//showWarning("questionCandidatesContent",questionCandidatesContent.size().toString());
		populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
		populateList("questionnaire_question/questionnaire_question_info/questionInQuestionnaire", questionSelected);
			//code
		}
		else{
			showWarning("Question Not Found","Oops! Can't find this question, it could be not valid anymore, please contact the Admin");
		}
		
	}
}

//after unselected a question, add this question to questionCandidatesContent, and remove it from questionSelected
deleteSelectedQuestion(){
	question_in_questionnaire_id=getListItemValue();
	int idx_question_in_questionnaire=-1;
	if(isNull(question_in_questionnaire_id)){
		showToast("No Question selected");
		return;
	}
	else{
		//showWarning("start",questionCandidatesContent.size().toString());
		//showWarning("startsize",questionSelected.size().toString());
		for (int i=0; i<questionSelected.size();i++){
			if (questionSelected.get(i).toString().contains(question_in_questionnaire_id)) {
				idx_question_in_questionnaire=i;
				//showWarning("get",questionCandidatesContent.get(idx_question).toString());
				break;
			}
		}
		if (idx_question_in_questionnaire>=0) {
			//code
		//showWarning("index",idx_question.toString());
		//showWarning("get",questionCandidatesContent.get(idx_question).toString());
		//questionSelected.add(questionCandidatesContent.get(idx_question_in_questionnaire));
		questionCandidatesContent.add(questionSelected.get(idx_question_in_questionnaire));
		questionSelected.remove(idx_question_in_questionnaire);
		//showWarning("questionSelectedadded",questionSelected.size().toString());
		//questionCandidatesContent.remove(idx_question_in_questionnaire);
		//showWarning("questionCandidatesContent",questionCandidatesContent.size().toString());
		populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
		populateList("questionnaire_question/questionnaire_question_info/questionInQuestionnaire", questionSelected);
		}
		else{
			showWarning("Question Not Found","Oops! You can't manupulate this question,please contact the Admin");
		}
		
	}
}


			
//if there is selected questionnaire, then change the info, else create a new questionnaire
finishCreateQuestionnaire(){
	//question_basic_info_change=0;
	//showWarning("change question_basic_info_change","0");
	if(questionSelected.isEmpty())
	{
		showWarning("No question selected","No question selected in this questionnaire");
		return;
	}
	if (!isNull(questionnaireInfoOriginal)){
		Hashtable questionnaireInfoChanges=listChange(questionnaireInfoNew,questionnaireInfoOriginal);
		//showWarning("questionnaireInfoChanges","questionnaireInfoChanges done");
		if(questionnaireInfoChanges.containsKey("EQUAL")){
			showWarning("No basic info changed","Please change at least one basic info of questionniare\n"+"Or this is viwed as an existing record.");
			return;
		}
	}
	//showWarning("questionSelected","not null");
		finished_questionnaire_id=null;//Creating a new questionnaire
		//showWarning("save started","started");
		saveTabGroup("questionnaire", finished_questionnaire_id, null, null, new SaveCallback() {
		//showWarning("saveTabGroup()","started");
		onSave(uuid, newRecord) {
		//showWarning("onSave()","started");
			finished_questionnaire_id = uuid;
	  //showWarning("finished_questionnaire_id()",finished_questionnaire_id);
			if (newRecord) {
				for (question : questionSelected){
			 //showWarning("insertrelation()",question.get(0).toString());
				saveEntitiesToRel("Questionnaire and Question",finished_questionnaire_id,question.get(0));
			//showWarning("savedrel()",question.get(0).toString());
			}
			//questionOriginal.addAll(questionSelected);
			showToast("New record created");
			cancelTabGroup("questionnaire_question",true);
			cancelTabGroup("questionnaire",true);
			showTab("control/questionnaire_control");
			//finished_questionnaire_id=null;
			}
			else{
				showWarning("Attention!","Something went wrong!");
		//finished_questionnaire_id=null;
			}
		}
		onError(message) {
			showWarning("error",message);
		}  
		});
	
	//flag_return_questionnaire=false;
	
}

finishChangeQuestionnaire(){
	if(isNull(finished_questionnaire_id)){
		showWarning("Failed","Please create questionnaire then change");
		return;
	}
	if(questionSelected.isEmpty())
	{
		showWarning("No question selected","No question selected in this questionnaire");
		return;
	}
	else{//This is for changing questionnaire
		//showWarning("change started","before initialization");
		question_origin=new ArrayList();
		//question_origin.addAll();
		/*
		if(questionOriginal==null){
			showWarning("change started","questionOriginal is null");
		}
		else{
			showWarning("change started",questionOriginal.size());
		}
		*/
		//The onSave is executed parallel with the sentences following it, so be careful!  
		//showWarning("change started","going to start");
		question_origin.addAll(questionOriginal);
		//showWarning("change going","copied");
		Hashtable questionListChanges=listChange(questionSelected,questionOriginal);
		//showWarning("questionListChanges","questionListChanges done");
		if (!isNull(questionnaireInfoOriginal)){//change from the list
			Hashtable questionnaireInfoChanges=listChange(questionnaireInfoNew,questionnaireInfoOriginal);
		//showWarning("questionnaireInfoChanges","questionnaireInfoChanges done");
		if(questionnaireInfoChanges.containsKey("EQUAL")){		
			if(questionListChanges.containsKey("EQUAL")){
				showWarning("Questionnaire Modification","No data is changed");
				return;
			}
			else{				
				//showWarning("basic info not changed (else)","not changed basic info");
				for (questionDel : quesListRelnOrigin){
					//showWarning("basic info not changed",question.get(0).toString());
					deleteRel(questionDel.get(0));
					}
				for (question : questionSelected){
					//showWarning("basic info not changed",question.get(0).toString());
					saveEntitiesToRel("Questionnaire and Question",finished_questionnaire_id,question.get(0));
					}
				//reset the used arraylists
				//questionOriginal.clear();
				//questionOriginal.addAll(questionSelected);
				showToast("Questions in this questionnaire are changed");
				cancelTabGroup("questionnaire_question",true);
				cancelTabGroup("questionnaire",true);
				showTab("control/questionnaire_control");
				//showWarning("Attention!","Questions in quesitonnaire are not changeable, please create a new questionnaire!");
				}
		}
		else{
			saveTabGroup("questionnaire", finished_questionnaire_id, null, null, new SaveCallback() {
			//showWarning("saveTabGroup()","started");
			onSave(uuid, newRecord) {
				//question_basic_info_change=1;//questionnaire's basic info has been changed
				//showWarning("onSave()","started");
				//finished_questionnaire_id = uuid;
				//showWarning("finished_questionnaire_id()",finished_questionnaire_id);
				for (questionDel : quesListRelnOrigin){
					//showWarning("basic info not changed",question.get(0).toString());
					deleteRel(questionDel.get(0));
					}
				for (question : questionSelected){
					//showWarning("basic info changed (else)",question.get(0).toString());
					saveEntitiesToRel("Questionnaire and Question",finished_questionnaire_id,question.get(0));
				}
				//reset the used arraylists
				//questionOriginal.clear();
				//questionOriginal.add(questionSelected);
				//questionnaireInfoOriginal.clear();
				//questionnaireInfoOriginal.addAll(questionnaireInfoNew);
				showToast("Questionnaire data is changed");
				ancelTabGroup("questionnaire_question",true);
				cancelTabGroup("questionnaire",true);
				showTab("control/questionnaire_control");
			}
			onError(message) {
				showWarning("error",message);
				}  
			});
			
			}
		}
		else{
			if(questionListChanges.containsKey("EQUAL")){
				showWarning("Questionnaire Modification","No data is changed");
				return;
			}
			else{				
				//showWarning("basic info not changed (else)","not changed basic info");
				for (question : questionSelected){
					//showWarning("basic info not changed",question.get(0).toString());
					saveEntitiesToRel("Questionnaire and Question",finished_questionnaire_id,question.get(0));
					}
				//reset the used arraylists
				questionOriginal.clear();
				questionOriginal.addAll(questionSelected);
				showToast("Questions in this questionnaire are changed");
				//showWarning("Attention!","Questions in quesitonnaire are not changeable, please create a new questionnaire!");
			}
		}
		
	}
}

//Search questions when selecting questions to questionnaire
searchQuestion(){
	String keywordForQuestion=getFieldValue("questionnaire_question/questionnaire_question_info/keywordOfQuestion").trim();
	if((isNull(keywordForQuestion))||(keywordForQuestion.equals("*"))){
		fetchAll(loadAllQuestionQuery,
				new FetchCallback() {
				onFetch(result) {
					questionCandidatesContent.clear();
					questionCandidates.clear();
					questionCandidates.addAll(result);
					questionCandidatesContent.addAll(result);
					questionCandidatesContent.removeAll(questionSelected);
					populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
				}  
		});		
	}

	else {
		
		
		searchQuestionQueryFirstHalf="select uuid, measure from latestNonDeletedAentValue where " +
				"latestNonDeletedAentValue.Measure like '%"; 
		searchQuestionQueryLastHalf="%' and latestNonDeletedAentValue.AttributeID = "+
				"(select AttributeID from AttributeKey where AttributeName='QuestionContent')";
				
						//"(select AttributeID from IdealAent where IdealAent.AentTypeID in "+
						//"(select AEntTypeID from AEntType where AEntType.AEntTypeName= 'QuestionBank'));";
//String keywordForLanguage=getFieldValue("questionBank/questionInfo/keywordInput");
		fetchAll(searchQuestionQueryFirstHalf+keywordForQuestion+searchQuestionQueryLastHalf,
				new FetchCallback() {
			onFetch(result) {
				questionCandidatesContent.clear();
				questionCandidates.clear();
				questionCandidates.addAll(result);
				questionCandidatesContent.addAll(result);
				questionCandidatesContent.removeAll(questionSelected);
				populateList("questionnaire_question/questionnaire_question_info/questionList", questionCandidatesContent);
		
		}  
});
	}
}

//measure whether two arraylists are identical, if not, recording what kinds of operation have been done
listChange(ArrayList targetList,ArrayList sourceList){
	//showWarning("listChange","listChange");
	Hashtable listChanges=new Hashtable();
	target_list=new ArrayList();
	source_list=new ArrayList();
	target_diff_on_source=new ArrayList();
	source_diff_on_target=new ArrayList();
	listChanges.clear();
	target_list.clear();
	source_list.clear();
	target_diff_on_source.clear();
	source_diff_on_target.clear();
	//showWarning("listChange","initialization hashtable and arraylist done");
	target_list.addAll(targetList);
	//showWarning("target list size",target_list.size().toString());
	source_list.addAll(sourceList);
	//showWarning("source list size",source_list.size().toString());
	target_list.removeAll(source_list);
	//showWarning("target-source list size",target_list.size().toString());
	target_diff_on_source.addAll(target_list);//target - source
	//showWarning("target_diff_on_source size",target_diff_on_source.size().toString());
	target_list.clear();
	target_list.addAll(targetList);
	//showWarning("target_list",target_list.size().toString());
	source_list.removeAll(target_list);
	source_diff_on_target.addAll(source_list);//source - target
	//showWarning("source_diff_on_target",source_diff_on_target.size().toString());
	if((target_diff_on_source.isEmpty()) && (source_diff_on_target.isEmpty())){
		//showWarning("listChange","listChanges.put(EQUAL,null);");
		listChanges.put("EQUAL",targetList);//here can not put ("EQUAL",null)
		//showWarning("listChange","listChanges.put(EQUAL,null);");
	}
	else if((!target_diff_on_source.isEmpty()) && (source_diff_on_target.isEmpty())){
		//added question
		//return "PUREADD";
		//showWarning("listChange","listChanges.put(PUREADD,null);");
		listChanges.put("PUREADD",target_diff_on_source);
		//showWarning("listChange","listChanges.put(PUREADD,null);");

	}
	else if((target_diff_on_source.isEmpty()) && (!source_diff_on_target.isEmpty())){
		//return "PUREDELETE";
		//showWarning("listChange","listChanges.put(PUREDELETE,null);");
		listChanges.put("PUREDELETE",source_diff_on_target);
		//showWarning("listChange","listChanges.put(PUREDELETE,null);");
	}
	else {
		//showWarning("listChange","listChanges.put(DELETE,null);");
		listChanges.put("ADD",target_diff_on_source);
		//showWarning("listChange","listChanges.put(ADD,null);");
		//showWarning("listChange","listChanges.put(DELETE,null);");
		listChanges.put("DELETE",source_diff_on_target);
		//showWarning("listChange","listChanges.put(DELETE,null);");
	}
	return listChanges;
}

/*** QuestionBank ***/

onEvent("control/question_control/New_Question","click","newQuestion()");
onEvent("questionBank/questionInfo/Finish_New_Question","click","saveNewQuestion()");
onEvent("control/question_control","show","loadQuestionBank()");
//onEvent("control/querytest/Submit","click","testQuery()");
onEvent("questionBank/questionInfo/","show","initialQuestionCreation()");
onEvent("control/question_control/questionList","click","loadQuestionInfo()");
onEvent("questionBank/questionInfo/New_Language","click","newLanguage()");
onEvent("questionBank/questionInfo/questionLanguageSelectionList","click","setQuestionLanguageText()");
onEvent("questionBank/questionInfo/Search_Language","click","searchLanguage()");

//indicates of there is selected question
question_id=null;
//All candidate lanaguages
candidateLanguageList=new ArrayList();
//selected language
current_selected_language_id=null;
/*
testQuery(){
	query=getFieldValue("control/querytest/query");
	//fetchAll("SELECT sql FROM sqlite_master WHERE name='archentity';",
	fetchAll(query.toString()+";",
	new FetchCallback() {
        onFetch(result) {
			if (isNull(result)) {
				//code
				showWarning("no","no result");
			}
			else{
			for (re : result) {
				
				showWarning("yes",re.toString());
				//showWarning("me",re.toString());
				
			}		    
            //populateList("control/question_control/questionList", result);
        }
		}
        onError(message) {
            showToast(message);
        }
    });
	
}
*/
//Starting point of creating new question
newQuestion(){
	question_id=null;
	newTabGroup("questionBank");
	onEvent("questionBank", "show", "showTab(\"questionBank/questionInfo\");"); 
}

//save the question info and its relationship with language
saveNewQuestion(){

	if(isNull(getFieldValue("questionBank/questionInfo/questionContent"))){
		showWarning("Validation Error", "You must fill in the Question Content before you can continue");
        return;
	}
	
	saveTabGroup("questionBank", question_id, null, null, new SaveCallback() {
    onSave(uuid, newRecord) {
      question_id = uuid;
	  //setFieldValue("questionBank/questionInfo/questionID", question_id);
      if (newRecord) {
		saveEntitiesToRel("Question and Language",question_id,current_selected_language_id);
		//question_id=null;	
        showToast("New record created");
        cancelTabGroup("questionBank",true);
        showTab("control/question_control");
		//newQuestion();
		//initialQuestionCreation();
      }
	  else{
		saveEntitiesToRel("Question and Language",question_id,current_selected_language_id);
		showToast("Record changed");
		//question_id=null;
	  }
    }
    onError(message) {
        showWarning("error",message);
    }  
  });
  /*
	pushDatabaseToServer("onComplete()");
	onComplete() {
	    showToast("finished pushing database");
	}
	*/
}

//preparation for question creation
initialQuestionCreation(){
	candidateLanguageList.clear();
	//setFieldValue("questionBank/questionInfo/keywordInput", "*");
	if(isNull(question_id)){
		String autoQuestionId="Ques-"+username+"-"+getCurrentTime();
		setFieldValue("questionBank/questionInfo/questionID", autoQuestionId);
		setFieldValue("questionBank/questionInforHidden/questionType", "N/A");
		setFieldValue("questionBank/questionInforHidden/questionChoice", "N/A");
		//setFieldValue("questionBank/questionInfo/questionContent", "");
		fetchAll(loadAllLanguageQuery,
				 new FetchCallback() {
				onFetch(result) {
					//if(!isNull(result)){
					candidateLanguageList.addAll(result);
					//populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
					populateDropDown("questionBank/questionInfo/questionLanguageSelectionList", result);
				if(!isNull(result)){
				setFieldValue("questionBank/questionInfo/questionLanguage",candidateLanguageList.get(0).get(1).toString());
				}
				//}
		}  
	});		
	}
	setFieldValue("questionBank/questionInfo/keywordInput", "*");
	/*
	else{
		showWarning("initialQuestionCreation","else");
		placeholder=new ArrayList();
		placeholder.clear();
		showWarning("initialQuestionCreation","placeholder created");
		placeholder.add(new NameValuePair("001", "Change language"));
		showWarning("initialQuestionCreation","placeholder added element");
		fetchAll(loadAllLanguageQuery,
				 new FetchCallback() {
				onFetch(result) {
					//if(!isNull(result)){
					showWarning("initialQuestionCreation","onFetch");
					candidateLanguageList.add(placeholder);
					showWarning("initialQuestionCreation","placeholder onFetch");
					candidateLanguageList.addAll(result);
					showWarning("initialQuestionCreation","result onFetch");
					//populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
					populateDropDown("questionBank/questionInfo/questionLanguageSelectionList", candidateLanguageList);
				//}
		}  
	});		
		//showWarning("question id is",question_id);
	}*/
	
}

setQuestionLanguageText(){
	//String question_language_id=getListItemValue();
	String question_language_id=null;
	String question_language_id=getFieldValue("questionBank/questionInfo/questionLanguageSelectionList");
	//String current_language_text=getFieldValue("questionBank/questionInfo/questionLanguage");
	if(!question_language_id.equals("001")){
	for (int i=0; i<candidateLanguageList.size();i++){
			if (candidateLanguageList.get(i).get(0).toString().equals(question_language_id)) {
				//String selected_language_text=candidateLanguageList.get(i).get(1).toString();
				//if(!selected_language_text.equals(current_language_text)){
					setFieldValue("questionBank/questionInfo/questionLanguage",candidateLanguageList.get(i).get(1).toString());
				//}
				current_selected_language_id=question_language_id;
				//showWarning("get",questionCandidatesContent.get(idx_question).toString());
				break;
			}
		}
	}
}
//load all question contents
loadQuestionBank(){
	question_id=null;
	questionBankList=new ArrayList();
	
	//deprecated query: 
	/*The fetchAll query returns the uuid and the question content of all the questions*/
	fetchAll(loadAllQuestionQuery,
	new FetchCallback() {
        onFetch(result) {
			if (!isNull(result)) {
				questionBankList.addAll(result);
				//for (re:result) {
				//questionBankList.add(re);
			//}
			
			}
			populateList("control/question_control/questionList", questionBankList);
        }

        onError(message) {
            showToast(message);
        }
    });
	

	/*fetchAll("SELECT uuid, group_concat(coalesce(measure, ''),' - ') as response " +
	//fetchAll("SELECT * " +
    "FROM (select * from latestNonDeletedArchentIdentifiers) " +
    "WHERE aenttypename = 'QuestionBank' " +
    "GROUP BY uuid;" +
    "order by response;",
	new FetchCallback() {
        onFetch(result) {
			for (re : result) {
				showWarning("yes",re.toString());
				//showWarning("me",re.toString());
				//code
			}		    
            //populateList("control/question_control/questionList", result);
        }

        onError(message) {
            showToast(message);
        }
    });
    */
}

//load question specific info
loadQuestionInfo(){
//question=new ArrayList();
question_id=getListItemValue();
	if(isNull(question_id)){
		showToast("No Question selected");
		return;
	}
	showTabGroup("questionBank", question_id, new FetchCallback() {
        onFetch(result) {
            //question.addAll(result);
            showToast("Loaded question"+question_id);
			setFieldValue("questionBank/questionInfo/keywordInput", "*");
        }
        onError(message) {
            showToast(message);
        }
    });
	
		//showWarning("loadQuestionInfo","load language");
		//ArrayList<String> placeholderelement=new ArrayList<String>();
		//placeholderelement.add("001");
		//placeholderelement.add("Change language");
		placeholder=new ArrayList();
		placeholder.add("001");
		placeholder.add("Change language");
		//showWarning("loadQuestionInfo",placeholder.get(0).getClass().getName());
		fetchAll(loadAllLanguageQuery,
				 new FetchCallback() {
				onFetch(result) {
					//if(!isNull(result)){
					candidateLanguageList.clear();
					candidateLanguageList.add(placeholder);
					//showWarning("loadQuestionInfo pla type",candidateLanguageList.get(0).getClass().getName());
					candidateLanguageList.addAll(result);
					//showWarning("loadQuestionInfo result type",candidateLanguageList.get(2).get(0).getClass().getName());
					//showWarning("loadQuestionInfo result type",candidateLanguageList.get(2).get(1).getClass().getName());
					//showWarning("loadQuestionInfo","result candidateLanguageList");
					//populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
					populateDropDown("questionBank/questionInfo/questionLanguageSelectionList", candidateLanguageList);
				}
				});
		/*			
	fetchAll(loadAllLanguageQuery,
				new FetchCallback() {
				onFetch(result) {
				candidateLanguageList.clear();
				candidateLanguageList.addAll(result);
				populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
				//populateDropDown("questionBank/questionInfo/questionLanguageDropdown", result);
				}  
	});
	*/
}

//search language
searchLanguage(){
	String keywordForLanguage=getFieldValue("questionBank/questionInfo/keywordInput").trim();
	if((isNull(keywordForLanguage)) || (keywordForLanguage.equals("*"))){
		placeholder=new ArrayList();
		placeholder.add("001");
		placeholder.add("View result");
		fetchAll(loadAllLanguageQuery,
				new FetchCallback() {
				onFetch(result) {
				candidateLanguageList.clear();
				candidateLanguageList.add(placeholder);
				candidateLanguageList.addAll(result);
				//populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
				populateDropDown("questionBank/questionInfo/questionLanguageSelectionList", candidateLanguageList);
				}  
		});		
	}
	else{
	searchLanguageQueryFirstHalf="select uuid, measure from latestNonDeletedAentValue where " +
						"latestNonDeletedAentValue.Measure like '%"; 
	searchLanguageQueryLastHalf="%' and latestNonDeletedAentValue.AttributeID = "+
						"(select AttributeID from AttributeKey where AttributeName='LanguageName');";
								//"(select AttributeID from IdealAent where IdealAent.AentTypeID in "+
								//"(select AEntTypeID from AEntType where AEntType.AEntTypeName= 'Language'));";
	//String keywordForLanguage=getFieldValue("questionBank/questionInfo/keywordInput");
	placeholder=new ArrayList();
	
	placeholder.add("001");
	placeholder.add("View result");
	fetchAll(searchLanguageQueryFirstHalf+keywordForLanguage+searchLanguageQueryLastHalf,
				new FetchCallback() {
				onFetch(result) {
				candidateLanguageList.clear();
				candidateLanguageList.add(placeholder);
				candidateLanguageList.addAll(result);
				//populateList("questionBank/questionInfo/questionLanguageSelectionList",result);
				populateDropDown("questionBank/questionInfo/questionLanguageSelectionList", candidateLanguageList);
				}  
		});
	}

}


/*** Person ***/
onEvent("control/user_control/New_User","click","newPerson()");
onEvent("person/personInfo/Finish_New_Person","click","saveNewPerson()");
onEvent("control/user_control","show","loadPerson()");
onEvent("person/personInfo/Take_Photo","click","attachPictureTo(\"person/personInfo/personPhoto\")");
onEvent("control/user_control/userList","click","loadPersonInfo()");
person_id=null;
newPerson(){
	person_id=null;
	newTabGroup("person");
	onEvent("person", "show", "showTab(\"person/personInfo\");");  
}

timeValidation(String startDateTime){	
		String hyphenDateRegex="^\\d{4}[-]\\d{2}[-]\\d{2}$";
		Pattern hyphenDatePattern=Pattern.compile(hyphenDateRegex);
		Matcher hyphenDateMatcher=hyphenDatePattern.matcher(startDateTime);
		if (hyphenDateMatcher.find()){		
				DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
				df.setLenient(false);
				Date sdt=null;
				try{
					sdt=df.parse(startDateTime);	
				}
				catch(Exception ex){
					//Log.e("error", ex.getMessage().toString());
					//showWarning("ex","ex");
					return false;
				}
			return true;
		}
	return false;
}

saveNewPerson(){

	if(isNull(getFieldValue("person/personInfo/personName"))){
		showWarning("Validation Error", "You must fill in the Person Name before you can continue");
        return;
	}
	if(isNull(getFieldValue("person/personInfo/personID"))){
		setFieldValue("person/personInfo/personID", getFieldValue("person/personInfo/personName")+getCurrentTime());
	}
	String personDOB=getFieldValue("person/personInfo/personDOB");
	if(timeValidation(personDOB)){
	saveTabGroup("person", person_id, null, null, new SaveCallback() {
    onSave(uuid, newRecord) {
      person_id = uuid;
      if (newRecord) {
		//newPerson();
		//person_id=null;
		
        showToast("New record created");
        cancelTabGroup("person",true);
        showTab("control/user_control");
      }
	  else{
		showToast("Record changed");	
	  }
	  
    }
    onError(message) {
        showWarning("error",message);
    }  
  });
	}
	else{
		showWarning("Invalid DOB","1.Datetime format should be yyyy-MM-dd \n"+"2.The value for date should be valid");
					return;
	}
}

loadPerson(){
	person_id=null;
	fetchAll("SELECT uuid, group_concat(coalesce(measure, ''),' - ') as response " +
    "FROM (select * from latestNonDeletedArchentIdentifiers) " +
    "WHERE aenttypename = 'Person' " +
    "GROUP BY uuid " +
    "order by response;", new FetchCallback() {
        onFetch(result) {
            populateList("control/user_control/userList", result);
        }

        onError(message) {
            showToast(message);
        }
    });
}

loadPersonInfo(){
person_id=getListItemValue();
	if(isNull(person_id)){
		showToast("No Person selected");
		return;
	}

	showTabGroup("person", person_id, new FetchCallback() {
        onFetch(result) {
            person=result;
        
            showToast("Loaded person"+person.getId());            
        }
        onError(message) {
            showToast(message);
        }
    });
}
/*** Language ***/
onEvent("control/language_control/New_Language","click","newLanguage()");
onEvent("language/languageInfo/Finish_New_Language","click","saveNewLanguage()");
onEvent("control/language_control","show","loadLanguage()");
onEvent("control/language_control/languageList","click","loadLanguageInfo()");
language_id=null;
newLanguage(){
	language_id=null;
	newTabGroup("language");
	onEvent("language", "show", "showTab(\"language/languageInfo\");");  
}
saveNewLanguage(){

	if(isNull(getFieldValue("language/languageInfo/languageName"))){
		showWarning("Validation Error", "You must fill in the Language Name before you can continue");
        return;
	}
	if(isNull(getFieldValue("language/languageInfo/languageID"))){
		showWarning("Validation Error", "You must fill in the Language ID before you can continue");
        return;
	}
	//setFieldValue("person/personInfo/personID", getFieldValue("person/personInfo/personName")+getCurrentTime());
	
	saveTabGroup("language", language_id, null, null, new SaveCallback() {
    onSave(uuid, newRecord) {
      language_id = uuid;
      if (newRecord) {
    	 
        showToast("New record created");
        cancelTabGroup("language",true);
        showTab("control/language_control");
      }
	  else{
		//language_id=null;
		showToast("Record changed");
	  }
    }
    onError(message) {
        showWarning("error",message);
    }  
  });
}

loadLanguage(){
	language_id=null;
	//"SELECT uuid, group_concat(coalesce(measure, ''),' - ') as response " +
    //"FROM (select * from latestNonDeletedArchentIdentifiers) " +
    //"WHERE aenttypename = 'Language' " +
    //"GROUP BY uuid " +
    //"order by response;"
	fetchAll(loadAllLanguageQuery, new FetchCallback() {
        onFetch(result) {
            populateList("control/language_control/languageList", result);
        }

        onError(message) {
            showToast(message);
        }
    });
}
loadLanguageInfo(){
language_id=getListItemValue();
	if(isNull(language_id)){
		showToast("No Language selected");
		return;
	}
	showTabGroup("language", language_id, new FetchCallback() {
        onFetch(result) {
            language=result;
            showToast("Loaded language"+language.getId());            
        }
        onError(message) {
            showToast(message);
        }
    });
}


/*** Search Page ***/
//Add entity types
entityTypes = new ArrayList();
entityTypes.add(new NameValuePair("{Questionnaire}", "Questionnaire"));
entityTypes.add(new NameValuePair("{Question}", "QuestionBank"));
entityTypes.add(new NameValuePair("{Person}", "Person"));
entityTypes.add(new NameValuePair("{Language}", "Language"));

onEvent("control/search","show","initializeSearch()");
onEvent("control/search/Record_Search","click","recordSearch()");
onEvent("control/search/entityList","click","showEntity()");

type=null;// entity type recording

searchQuery_firstHalf="select uuid, measure from latestNonDeletedAentValue where " +
"latestNonDeletedAentValue.Measure like '%"; 
searchQuery_lastHalf="%' and latestNonDeletedAentValue.AttributeID in "+
"(select AttributeID from IdealAent where IdealAent.AentTypeID in "+
"(select AEntTypeID from AEntType where  AEntType.AEntTypeName= '";

initializeSearch(){
	populateDropDown("control/search/entityTypes",entityTypes);
}

recordSearch(){
	searchResult=new ArrayList();
	searchResult.clear();
	type = getFieldValue("control/search/entityTypes");
	String keyword=getFieldValue("control/search/keyword").trim();
	if((isNull(keyword)) || keyword.equals("*")){
		search_query="select uuid, measure from latestNonDeletedAentValue where "+
		"latestNonDeletedAentValue.AttributeID in "+
		"(select AttributeID from IdealAent where IdealAent.AentTypeID in "+
		"(select AEntTypeID from AEntType where  AEntType.AEntTypeName= '"+type+"'));";// without group by uuid, user can view every related details
		//showWarning("query",search_query);
		fetchAll(search_query, new FetchCallback() {
        onFetch(result) {
			if(!isNull(result)){
			searchResult.addAll(result);
            populateList("control/search/entityList", result);}
			else{
				showWarning("No result","Sorry, no result is found");
			}
        }

        onError(message) {
            showToast(message);
        }
    });
		 //populateCursorList("control/search/entityList", "select uuid, response from latestNonDeletedArchEntFormattedIdentifiers where aenttypename = '" + type + "' limit ? offset ?;", 25);
	}
	else{	
		search_query=searchQuery_firstHalf+keyword+searchQuery_lastHalf+type+"'));";//without group by uuid, user can view every related details
		fetchAll(search_query, new FetchCallback() {
        onFetch(result) {
			if(!isNull(result)){
			searchResult.addAll(result);
            populateList("control/search/entityList", result);}
			else{
				showWarning("No result","Sorry, no result is found");
			}
        }

        onError(message) {
            showToast(message);
        }
    });
		//showWarning("query",search_query);
		//populateCursorList("control/search/entityList", search_query, 25);
	}
}

showEntity(){
	entity_id=getListItemValue();
	switch (type){
		case "Questionnaire":
			loadQuestionnaireInfo();
			break;
		case "Person":
			loadPersonInfo();
			break;
		case "Language":
			loadLanguageInfo();
			break;
		case "QuestionBank":
			loadQuestionInfo();
			break;
			
	}
	
}


