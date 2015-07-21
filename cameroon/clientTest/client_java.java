import java.util.concurrent.Callable;
import java.util.*;
/*** 'Editable' - you can edit the code below based on the needs ***/
User user; // don't touch
String userid;

/***query***/
loadAllQuestionnaireQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='QuestionnaireName') "+
	"GROUP BY uuid;";

loadAllPersonQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
	"WHERE latestNonDeletedAentValue.AttributeID "+
	"= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='PersonName') "+
	"GROUP BY uuid;";

loadAllLanguageQuery="SELECT uuid,measure FROM latestNonDeletedAentValue " +
		"WHERE latestNonDeletedAentValue.AttributeID "+
		"= (SELECT AttributeID FROM AttributeKey WHERE AttributeName='LanguageName') "+
		"GROUP BY uuid;";

loadAllSurveyQuery="select uuid,measure from "+
		"((select measure as quesnirid from latestNonDeletedAentValue where AttributeID = "+
			"(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') group by measure) t1 "+
		"inner join "+
		"(select uuid, measure from latestNonDeletedAentValue where AttributeID= "+
			"(select AttributeID from AttributeKey where AttributeName='QuestionnaireName')) t2 "+
		"on t2.uuid=t1.quesnirid );";

//loadFilesForAnswer="select uuid, measure from AentValue where AentValue.AttributeID =(select AttributeID from AttributeKey where AttributeKey.AttributeName='AnswerText') and AentValue.uuid in (select uuid from (SELECT uuid FROM AEntValue where AEntValue.AttributeID=(select AttributeKey.AttributeID from AttributeKey where AttributeKey.AttributeName='AnswerQuestionID') and AEntValue.freetext='1000011437080460685') t1 inner join (SELECT uuid FROM AEntValue where AEntValue.AttributeID=(select AttributeKey.AttributeID from AttributeKey where AttributeKey.AttributeName='AnswerQuestionnaireID') and AEntValue.freetext='1000011437080512135') t2 using(uuid))"
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

onSyncEvent("onSyncStart()", "onSyncSuccess()", "onSyncFailure()");


onSyncStart() {
    showToast("sync started");
}
 
onSyncSuccess() {
    showToast("sync success");
}
 
onSyncFailure() {
    showToast("sync failed");
}

/***Save relationships among entities***/
saveEntitiesToRel(String type, String entity1, String entity2) {
	/*
    if (isNull(entity1) || isNull(entity2)) return;
    saveRel(null, type, null, null, new SaveCallback() {
        onSave(rel_id, newRecord) {
            addReln(entity1, rel_id, null);
            addReln(entity2, rel_id, null);
        }
        onError(message) {
			showWarning("error saveEntitiesToRel",message);
		}  
    });*/
	 if (isNull(entity1) || isNull(entity2)) return;
	    saveRel(null, type, null, null, new SaveCallback() {
	        onSave(reln_id, newRecord) {
	            addReln(entity1, reln_id, null);
	            addReln(entity2, reln_id, null);
	        }
	        onError(message) {
	            //Log.e("saveEntitiesToHierRel", message);
	        	showWarning("error saveEntitiesToRel",message);
	        }
	    });
}
/*
saveThreeEntitiesToRel(String type, String entity1, String entity2, String entity3) {
    if (isNull(entity1) || isNull(entity2) || isNull(entity3)) return;
    saveRel(null, type, null, null, new SaveCallback() {
        onSave(rel_id, newRecord) {
            addReln(entity1, rel_id, null);
            addReln(entity2, rel_id, null);
            addReln(entity3, rel_id, null);
        }
        onError(message) {
			showWarning("error saveThreeEntitiesToRel",message);
		} 
    });
}
*/
/***User***/
onEvent("user/usertab/users", "click", "login()");

loadUsers() {
    fetchAll("select userid, fname || ' ' || lname from user where userdeleted is null", new FetchCallback() {
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
/***Survey Control***/
onEvent("control/survey_control/New_Survey","click","showQuestionnaireList()");
onEvent("control/survey_control","show","loadExistSurvey()");
onEvent("control/survey_control/surveyList","click","loadAnswersForQuestionnaire(\"New\")");
answer_id=null;//new answer_id
survey_id=null;
current_quesnir_id=null;
current_question_id=null;
current_quesnir_name=null;
current_question_content=null;
current_answer_file_id=null;
current_answer_id=null;
questionnaire_list=new ArrayList();
ques_in_current_quesnir=new ArrayList();
survey_list=new ArrayList();
//all_quesnir_list=new ArrayList();
answer_quesnir_list=new ArrayList();

showQuestionnaireList(){
	survey_id=null;
	current_quesnir_id=null;
	current_question_id=null;
	current_quesnir_name=null;
	current_question_content=null;
	current_answer_file_id=null;
	current_answer_id=null;
	
	ques_in_current_quesnir.clear();
	answer_quesnir_list.clear();
	survey_list.clear();
	questionnaire_list.clear();
	
	newTabGroup("questionnaireListAll");
	onEvent("questionnaireListAll","show","loadAllQuesnir()");

}

loadAllQuesnir(){
	fetchAll(
		loadAllQuestionnaireQuery,
		new FetchCallback() {
        	onFetch(result) {
				if (!isNull(result)) {
					//questionnaire_list.addAll(result);
					populateList("questionnaireListAll/questionnaireListInfo/questionnaireListInDB", result);	
				}	
				else{
			    	showWarning("No questionnaire","No questionnaire is available, please contact the admin");
					return;
				}
       	 }

        	onError(message) {
            	showToast(message);
        	}
    	});
}

loadExistSurvey(){
	survey_list.clear();
	fetchAll(loadAllSurveyQuery,
			new FetchCallback() {
	        	onFetch(result) {
					if (!isNull(result)) {
						survey_list.addAll(result);	
						}
					populateList("control/survey_control/surveyList", survey_list);	
	       	 }
	        	onError(message) {
	            	showToast(message);
	        	}
	    	});
}

loadAnswersForQuestionnaire(String typeFlag){
	if(typeFlag.equals("New")){
	current_quesnir_id=getListItemValue();
	if(isNull(current_quesnir_id)){
		showWarning("Survey not selected","Survey not selected or not available");
		return;
	}
	else{
	newTabGroup("answerToQuestionnaire");
	setFieldValue("answerToQuestionnaire/answerQuesnirInfo/answerListQuesnirID",current_quesnir_id);
	loadAnswersForQuesnirQuery="select uuid,measure from latestNonDeletedAentValue "+
	"where latestNonDeletedAentValue.AttributeID="+
			"(select AttributeID from Attributekey where AttributeKey.AttributeName='AnswerText') "+
	"and latestNonDeletedAentValue.uuid in "+
			"(select uuid from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
				"(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') "+
			"and latestNonDeletedAentValue.measure="+current_quesnir_id+");";
	fetchAll(loadAnswersForQuesnirQuery,
				new FetchCallback() {
					onFetch(result) {
						if (!isNull(result)) {		
							answer_quesnir_list.clear();
							answer_quesnir_list.addAll(result);
							populateList("answerToQuestionnaire/answerQuesnirInfo/answerInQuesnir",answer_quesnir_list);
						}
						else{
							showWarning("No answer in this questionnaire","This questionnaire is not ready yet");
							return;
						}
					}
			       
				onError(message) {
					showToast(message);
				}
				});
		
	questionnaire_question_query="SELECT uuid, measure "+
			"from latestNonDeletedAentValue "+
			"where latestNonDeletedAentValue.AttributeID = "+
			"(select AttributeID from AttributeKey where AttributeName='QuestionContent') "+
			"and uuid in "+
			"(select AEntReln.uuid from AEntReln where AEntReln.uuid<>"+current_quesnir_id+" "+
			"and AEntReln.RelationshipID IN "+
			"(select  relationshipID from AEntReln aer "+
			"inner join "+
			"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_quesnir_id+") tm "+
			"on aer.AentRelnTimestamp=tm.maxtime group by relationshipID)) "+
			"group by uuid;";
	fetchAll(questionnaire_question_query,
			new FetchCallback() {
				onFetch(result) {
					if (!isNull(result)) {		
					ques_in_current_quesnir.clear();
					ques_in_current_quesnir.addAll(result);
					populateList("answerToQuestionnaire/answerQuesnirInfo/quesInQuesnirList",ques_in_current_quesnir);
					}
					else{
						showWarning("No question in this questionnaire","This questionnaire is not ready yet");
						return;
					}
				}
		       
			onError(message) {
				showToast(message);
			}
			});
	for(survey:survey_list){
		if(survey.get(0).equals(current_quesnir_id))
		{
			current_quesnir_name=survey.get(1);
			setFieldValue("answerToQuestionnaire/answerQuesnirInfo/answerListQuesnirName",current_quesnir_name);
		}
		}
		}
	/*
		for(int i=0;i<survey_list.size();i++){
		if(survey_list.get(i).get(0).equals(current_quesnir_id))
		{
			current_quesnir_name=survey_list.get(i).get(1);
			setFieldValue("answerToQuestionnaire/answerQuesnirInfo/answerListQuesnirName",current_quesnir_name);
		}
		}
		}
		*/
	}
	else{
		loadAnswersForQuesnirQuery="select uuid,measure from latestNonDeletedAentValue "+
				"where latestNonDeletedAentValue.AttributeID="+
						"(select AttributeID from Attributekey where AttributeKey.AttributeName='AnswerText') "+
				"and latestNonDeletedAentValue.uuid in "+
						"(select uuid from latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
							"(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') "+
						"and latestNonDeletedAentValue.measure="+current_quesnir_id+");";
				fetchAll(loadAnswersForQuesnirQuery,
							new FetchCallback() {
								onFetch(result) {
									if (!isNull(result)) {		
										answer_quesnir_list.clear();
										answer_quesnir_list.addAll(result);
										populateList("answerToQuestionnaire/answerQuesnirInfo/answerInQuesnir",answer_quesnir_list);
									}
									else{
										showWarning("No answer in this questionnaire","This questionnaire is not ready yet");
										return;
									}
								}
						       
							onError(message) {
								showToast(message);
							}
							});
	}
}

onEvent("questionnaireListAll/questionnaireListInfo/questionnaireListInDB","click","loadQuesnirInfo()");
loadQuesnirInfo(){
	current_quesnir_id=getListItemValue();
	if(isNull(current_quesnir_id)){
		showWarning("No questionnaire selected","No questionnaire is selected, please select a questionnaire");
		return;
	}
	if(current_quesnir_id.equals("placeholder")){
		showWarning("No questionnaire available","No questionnaire available, please contact the admin");
		return;
	}
	else{
		questionnaire_question_query="SELECT uuid, measure "+
		"from latestNonDeletedAentValue "+
		"where latestNonDeletedAentValue.AttributeID = "+
		"(select AttributeID from AttributeKey where AttributeName='QuestionContent') "+
		"and uuid in "+
		"(select AEntReln.uuid from AEntReln where AEntReln.uuid<>"+current_quesnir_id+" "+
		"and AEntReln.RelationshipID IN "+
		"(select  relationshipID from AEntReln aer "+
		"inner join "+
		"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_quesnir_id+") tm "+
		"on aer.AentRelnTimestamp=tm.maxtime group by relationshipID)) "+
		"group by uuid;";
		ques_in_current_quesnir.clear();
		fetchAll(questionnaire_question_query,
				new FetchCallback() {
					onFetch(result) {
						if (!isNull(result)) {	
							ques_in_current_quesnir.clear();	
						ques_in_current_quesnir.addAll(result);
						}
						else{
							showWarning("No question in this questionnaire","This questionnaire is not ready yet");
							return;
						}
					}
			       
				onError(message) {
					showToast(message);
				}
				});
				
		showTabGroup("questionnaireInfo", current_quesnir_id, new FetchCallback() {
        	onFetch(result) {	
        		populateList("questionnaireInfo/surveyQuestionnaire/surveyQuestionInQuestionnaire", ques_in_current_quesnir);
        		current_quesnir_name=getFieldValue("questionnaireInfo/surveyQuestionnaire/surveyQuestionnaireName");
            	showToast("Loaded questionnaire"+result.getId());   	
        	}
        	onError(message) {
            showToast(message);
        	}
   	 	});
		
		
	}
}

onEvent("answerToQuestionnaire/answerQuesnirInfo/answerInQuesnir","click","loadAnswerInfo()");
onEvent("answerToQuestionnaire/answerQuesnirInfo/quesInQuesnirList","click","ViewAnswersOfQuesion()");
onEvent("answerToQuestionnaire/answerQuesnirInfo/View_All_Answer","click","loadAnswersForQuestionnaire(\"Refresh\")");

files_in_current_ques=new ArrayList();
files_origin=new ArrayList();

loadAnswerInfo(){
	current_answer_id=null;
	current_answer_id=getListItemValue();
	if(isNull(current_answer_id)){
		showWarning("Error","Can't find this answer, please contact the admin");
		return;
	}
	else{
		/*
	loadFileForAnswerQuery="select uuid,measure from latestNonDeletedAentValue "+ 
			"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileName') "+
			"and uuid in "+
				"(select uuid from AentReln where uuid <>"+current_answer_id+" "+
				"and RelationshipID in "+
					"(select RelationshipID from Relationship where RelnTypeID = "+
						"(select RelnTypeID from RelnType where RelnTypeName='Answer and File')"+ 
							"and RelationshipID in (select RelationshipID from AentReln where AentReln.uuid="+current_answer_id+" )))";
		 */
	loadFileForAnswerQuery="select uuid,measure from latestNonDeletedAentValue "+ 
				"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='FileName') "+
				"and uuid in "+
	 			"(select uuid from AentReln where RelationshipID in "+
	 				"(select RelationshipID from "+
	 					"(select RelationshipID, AEntRelnTimestamp from AentReln where AentReln.uuid="+current_answer_id+" "+
	 					"and RelationshipID in "+
	 						"(select RelationshipID from Relationship where RelnTypeID="+
	 							"(select RelnTypeID from RelnType where RelnTypeName='Answer and File'))) t1 "+
	 					"inner join "+
	 					"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_answer_id+" "+
	 					"and AentReln.RelationshipID in (select RelationshipID from Relationship where RelnTypeID="+
	 					"(select RelnTypeID from RelnType where RelnTypeName='Answer and File'))) t2 "+
	 					"on t1.AentRelnTimestamp=t2.maxtime group by relationshipID))";
	
	loadAnswerInterviewerQuery="select uuid, measure from latestNonDeletedAentValue "+
			 		"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonName') "+
			 		"and uuid in "+
			 			"(select uuid from AentReln where RelationshipID in "+
			 				"(select RelationshipID from "+
			 					"(select RelationshipID, AEntRelnTimestamp from AentReln where AentReln.uuid="+current_answer_id+" "+
			 					"and RelationshipID in "+
			 						"(select RelationshipID from Relationship where RelnTypeID="+
			 							"(select RelnTypeID from RelnType where RelnTypeName='Answer and Interviewer'))) t1 "+
			 					"inner join "+
			 					"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_answer_id+" "+
			 					"and AentReln.RelationshipID in (select RelationshipID from Relationship where RelnTypeID="+
			 					"(select RelnTypeID from RelnType where RelnTypeName='Answer and Interviewer'))) t2 "+
			 					"on t1.AentRelnTimestamp=t2.maxtime group by relationshipID))";
	
	loadAnswerIntervieweeQuery="select uuid, measure from latestNonDeletedAentValue "+
	 		"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonName') "+
	 		"and uuid in "+
	 			"(select uuid from AentReln where RelationshipID in "+
	 				"(select RelationshipID from "+
	 					"(select RelationshipID, AEntRelnTimestamp from AentReln where AentReln.uuid="+current_answer_id+" "+
	 					"and RelationshipID in "+
	 						"(select RelationshipID from Relationship where RelnTypeID="+
	 							"(select RelnTypeID from RelnType where RelnTypeName='Answer and Interviewee'))) t1 "+
	 					"inner join "+
	 					"(select max(AEntRelnTimestamp) as maxtime from AEntReln where AEntReln.uuid ="+current_answer_id+" "+
	 					"and AentReln.RelationshipID in (select RelationshipID from Relationship where RelnTypeID="+
	 					"(select RelnTypeID from RelnType where RelnTypeName='Answer and Interviewee'))) t2 "+
	 					"on t1.AentRelnTimestamp=t2.maxtime group by relationshipID))";
	
	fetchAll(loadFileForAnswerQuery, new FetchCallback() {
        onFetch(result) {
        	files_in_current_ques.clear();
        	files_origin.clear();
        	files_in_current_ques.addAll(result);
        	files_origin.addAll(result);
        }

        onError(message) {
            showToast(message);
        }
    });
	fetchAll(loadAnswerInterviewerQuery, new FetchCallback() {
        onFetch(result) {
        	selected_answer_interviewer.clear();
        	origin_selected_interviewer.clear();
        	selected_answer_interviewer.addAll(result);
        	origin_selected_interviewer.addAll(result);
        }

        onError(message) {
            showToast(message);
        }
    });
	fetchAll(loadAnswerIntervieweeQuery, new FetchCallback() {
        onFetch(result) {
        	selected_answer_interviewee.clear();
        	origin_selected_interviewee.clear();
        	selected_answer_interviewee.addAll(result);
        	origin_selected_interviewee.addAll(result);

        }

        onError(message) {
            showToast(message);
        }
    });
	fetchAll(loadAllPersonQuery, new FetchCallback() {
        onFetch(result) {
        	candidate_answer_interviewee.clear();
        	candidate_answer_interviewee.addAll(result);
        	candidate_answer_interviewer.clear();
        	candidate_answer_interviewer.addAll(result);
        	candidate_answer_interviewer.removeAll(selected_answer_interviewer);
        	candidate_answer_interviewee.removeAll(selected_answer_interviewee);    	
        }

        onError(message) {
            showToast(message);
        }
    });
	
	showTabGroup("survey", current_answer_id, new FetchCallback() {
        onFetch(result) {
            //person=result;
        	populateList("survey/answer/answerFileList", files_in_current_ques);
        	populateList("survey/answer/answerInterviewerList", selected_answer_interviewer);
        	populateList("survey/answer/answerIntervieweeList", selected_answer_interviewee);
        	populateList("survey/answer/answerInterviewerSelectionList", candidate_answer_interviewer);
        	populateList("survey/answer/answerIntervieweeSelectionList", candidate_answer_interviewee);
        	populateDropDown("survey/answer/file_Category",categoryTypes);
        	answerInfoOriginal.add(getFieldValue("survey/answer/answerChoice"));
        	answerInfoOriginal.add(getFieldValue("survey/answer/answerText"));
            showToast("Loaded answer"+result.getId());            
        }
        onError(message) {
            showToast(message);
        }
    });
	}
	
}

ViewAnswersOfQuesion(){
	current_question_id=getListItemValue();
	current_quesnir_id=getFieldValue("answerToQuestionnaire/answerQuesnirInfo/answerListQuesnirID");
	loadAnswersForQuesion="SELECT uuid, measure FROM latestNonDeletedAentValue "+
			"WHERE latestNonDeletedAentValue.AttributeID = "+
			"(SELECT AttributeID FROM AttributeKey WHERE AttributeKey.AttributeName='AnswerText') "+
			"AND latestNonDeletedAentValue.uuid IN "+
			"(select uuid from "+
				"(SELECT uuid FROM latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
					"(select AttributeKey.AttributeID from AttributeKey "+
					"where AttributeKey.AttributeName='AnswerQuestionnaireID') "+
					"and latestNonDeletedAentValue.measure="+current_quesnir_id+") t1 "+
			 "inner join "+
			 	"(SELECT uuid FROM latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
			 		"(select AttributeKey.AttributeID from AttributeKey "+
			 		"where AttributeKey.AttributeName='AnswerQuestionID') "+
			 		"and latestNonDeletedAentValue.measure="+current_question_id+") t2 "+
			 		"using(uuid))";	
	
	fetchAll(loadAnswersForQuesion,
			new FetchCallback() {
		        onFetch(result) {
						populateList("answerToQuestionnaire/answerQuesnirInfo/answerInQuesnir", result);	
					
		        }

		        onError(message) {
		            showToast(message);
		        }
		    });
}

onEvent("answerToQuestion/answerInfo/Create_New_Answer","click","startNewAnswer()");
onEvent("answerToQuestion/answerInfo","show","loadAnswerListForQuesion()");
onEvent("answerToQuestion/answerInfo/answerList","click","loadAnswerInfo()");

origin_selected_interviewer=new ArrayList();
selected_answer_interviewer=new ArrayList();
candidate_answer_interviewer=new ArrayList();

origin_selected_interviewee=new ArrayList();
selected_answer_interviewee=new ArrayList();
candidate_answer_interviewee=new ArrayList();
answerInfoOriginal=new ArrayList();
answerInfoNew=new ArrayList();

categoryTypes=new ArrayList();
categoryTypes.add(new NameValuePair("{Audio}", "Audio"));
categoryTypes.add(new NameValuePair("{Video}", "Video"));
categoryTypes.add(new NameValuePair("{Photo}", "Photo"));
categoryTypes.add(new NameValuePair("{Other}", "Other"));

startNewAnswer(){
	//current_question_id=getListItemValue();
	String current_start_time=getCurrentTime();
	
	
	origin_selected_interviewer.clear();
	selected_answer_interviewer.clear();
	candidate_answer_interviewer.clear();

	origin_selected_interviewee.clear();
	selected_answer_interviewee.clear();
	candidate_answer_interviewee.clear();
	
	answerInfoOriginal.clear();
	answerInfoNew.clear();
	
	files_in_current_ques.clear();
	files_origin.clear();
	
	answer_id=null;
	current_answer_file_id=null;
	current_answer_id=null;
	
	
	newTabGroup("survey");
	setFieldValue("survey/answer/answerQuestionnaireID", current_quesnir_id);
	setFieldValue("survey/answer/answerQuestionID", current_question_id);
	setFieldValue("survey/answer/answerStartTimestamp", current_start_time);
	populateList("survey/answer/answerFileList",files_in_current_ques);

	populateDropDown("survey/answer/file_Category",categoryTypes);
	
	fetchAll(loadAllPersonQuery,
				new FetchCallback() {
					onFetch(result) {
						if (!isNull(result)) {	
							candidate_answer_interviewer.addAll(result);
							candidate_answer_interviewee.addAll(result);
							populateList("survey/answer/answerInterviewerList",selected_answer_interviewer);
							populateList("survey/answer/answerIntervieweeList",selected_answer_interviewee);
							populateList("survey/answer/answerInterviewerSelectionList",candidate_answer_interviewer);
							populateList("survey/answer/answerIntervieweeSelectionList",candidate_answer_interviewee);
						}
						else{
							showWarning("No person info available","No person info available\n"+"Please add person info first");
						}
					}
			       
				onError(message) {
					showToast(message);
				}
			});
}

loadAnswerListForQuesion(){
	loadAnswersForQuesion="SELECT uuid, measure FROM latestNonDeletedAentValue "+
			"WHERE latestNonDeletedAentValue.AttributeID = "+
			"(SELECT AttributeID FROM AttributeKey WHERE AttributeKey.AttributeName='AnswerText') "+
			"AND latestNonDeletedAentValue.uuid IN "+
			"(select uuid from "+
				"(SELECT uuid FROM latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
					"(select AttributeKey.AttributeID from AttributeKey "+
					"where AttributeKey.AttributeName='AnswerQuestionnaireID') "+
					"and latestNonDeletedAentValue.measure="+current_quesnir_id+") t1 "+
			 "inner join "+
			 	"(SELECT uuid FROM latestNonDeletedAentValue where latestNonDeletedAentValue.AttributeID="+
			 		"(select AttributeKey.AttributeID from AttributeKey "+
			 		"where AttributeKey.AttributeName='AnswerQuestionID') "+
			 		"and latestNonDeletedAentValue.measure="+current_question_id+") t2 "+
			 		"using(uuid))";
	
	fetchAll(loadAnswersForQuesion,
			new FetchCallback() {
		        onFetch(result) {
					//if (!isNull(result)) {
						populateList("answerToQuestion/answerInfo/answerList", result);	
					//}
					
		        }

		        onError(message) {
		            showToast(message);
		        }
		    });
}

onEvent("questionnaireInfo/surveyQuestionnaire/surveyQuestionInQuestionnaire","click","loadAnswerFromQuesInQuesnir()");

loadAnswerFromQuesInQuesnir(){
	if(isNull(current_quesnir_name)){
		showWarning("No questionnaire name available","Can't get the questionnaire name, please contact the admin");
		return;
	}
	current_question_id=getListItemValue();
	for(ques: ques_in_current_quesnir){
		if(ques.get(0).equals(current_question_id))
		{
			current_question_content=ques.get(1);
			break;
		}
	}
	/*
	for(int i=0;i<ques_in_current_quesnir.size();i++){
		if(ques_in_current_quesnir.get(i).get(0).equals(current_question_id))
		{
			current_question_content=ques_in_current_quesnir.get(i).get(1);
			break;
		}
	}
	*/
	if(isNull(current_question_content)){
		showWarning("No question content available","Can't get the question content, please contact the admin");
		return;
	}
	newTabGroup("answerToQuestion");
	setFieldValue("answerToQuestion/answerInfo/answerListQuestionnaireID", current_quesnir_id);
	setFieldValue("answerToQuestion/answerInfo/answerListQuestionID", current_question_id);
	setFieldValue("answerToQuestion/answerInfo/answerListQuestionnaireName", current_quesnir_name);
	setFieldValue("answerToQuestion/answerInfo/answerListQuestionContent", current_question_content);
}

onEvent("survey/answer/answerInterviewerSelectionList","click","addItemToTargetList(candidate_answer_interviewer,\"interviewer\")");
onEvent("survey/answer/answerIntervieweeSelectionList","click","addItemToTargetList(candidate_answer_interviewee,\"interviewee\")");
onEvent("survey/answer/answerInterviewerList","click","deleteItemFromTargetList(selected_answer_interviewer,\"interviewer\")");
onEvent("survey/answer/answerIntervieweeList","click","deleteItemFromTargetList(selected_answer_interviewee,\"interviewee\")");
onEvent("survey/answer/Finish_New_Answer","click","saveNewAnswer()");
onEvent("survey/answer/Add_New_File","click","newFileFromQuestion()");
onEvent("survey/answer/answerFileList","click","viewOrDeleteFileReln()");


addItemToTargetList(ArrayList sourceList, String type_flag){	
	itemId=getListItemValue();	
	int idx_item=-1;
		
	if(isNull(itemId)){
		showToast("No Item selected");
		return;
	}
	if(itemId.equals("placeholder")){
		showToast("Invalid item");
		return;
	}
	else{	
		
		
		for (int i=0; i<sourceList.size();i++){
			if (sourceList.get(i).get(0).equals(itemId)) {
				idx_item=i;
				break;
			}
		}
		
		if (idx_item>=0) {
			switch (type_flag){
			case "interviewer":		
					selected_answer_interviewer.add(sourceList.get(idx_item));
					candidate_answer_interviewer.remove(idx_item);
					populateList("survey/answer/answerInterviewerList", selected_answer_interviewer);
					populateList("survey/answer/answerInterviewerSelectionList", candidate_answer_interviewer);
					break;
			case "interviewee":
					selected_answer_interviewee.add(sourceList.get(idx_item));
					candidate_answer_interviewee.remove(idx_item);
					populateList("survey/answer/answerIntervieweeList", selected_answer_interviewee);
					populateList("survey/answer/answerIntervieweeSelectionList", candidate_answer_interviewee);
					break;
			
			}
		}
		else{
			showWarning("Item Not Found","Oops! Can't find this item, it could be not valid anymore, please contact the Admin");
		}
		
	}
}


deleteItemFromTargetList(ArrayList targetList, String type_flag){
	deleteItemId=getListItemValue();
	int idx_delete=-1;
	if(isNull(deleteItemId)){
		showToast("No Item selected");
		return;
	}
	if(deleteItemId.equals("placeholder")){
		showToast("Invalid item");
		return;
	}
	else{	
		
		for (int i=0; i<targetList.size();i++){
			if (targetList.get(i).get(0).equals(deleteItemId)) {
				idx_delete=i;
				break;
			}
		}
		
		if (idx_delete>=0) {
			switch (type_flag){
			case "interviewer":		
				candidate_answer_interviewer.add(targetList.get(idx_delete));
				selected_answer_interviewer.remove(idx_delete);
				populateList("survey/answer/answerInterviewerList", selected_answer_interviewer);
				populateList("survey/answer/answerInterviewerSelectionList", candidate_answer_interviewer);
				break;
			case "interviewee":
				candidate_answer_interviewee.add(targetList.get(idx_delete));
				selected_answer_interviewee.remove(idx_delete);
				populateList("survey/answer/answerIntervieweeList", selected_answer_interviewee);
				populateList("survey/answer/answerIntervieweeSelectionList", candidate_answer_interviewee);
				break;
			
			}
		}
		else{
			showWarning("Item Not Found","Oops! Can't find this item, it could be not valid anymore, please contact the Admin");
		}
		
	}
}

saveNewAnswer(){
	if((isNull(selected_answer_interviewer)) || (isNull(selected_answer_interviewee))){
		showWarning("Warning","Please select interviewers and interviewees");
		return;
	}
	if((selected_answer_interviewer.isEmpty()) || (selected_answer_interviewee.isEmpty())){
		showWarning("Warning","No available interviwer or interviewee\n Please create person info");
		return;
	}
	if(isNull(getFieldValue("survey/answer/answerText"))){
		showWarning("Warning","Please input answer text");
		return;
	}
	if(isNull(current_answer_id)){//create new answer
	setFieldValue("survey/answer/answerEndTimestamp",getCurrentTime());
	saveTabGroup("survey", answer_id, null, null, new SaveCallback() {
		onSave(uuid, newRecord) {
			answer_id = uuid;
			if (newRecord) {	
				//saveThreeEntitiesToRel("Answer and Question",current_quesnir_id,current_question_id,answer_id);	
				
				for(interviewer : selected_answer_interviewer){
					saveEntitiesToRel("Answer and Interviewer",answer_id,interviewer.get(0));
				}
				for(interviewee : selected_answer_interviewee){
					saveEntitiesToRel("Answer and Interviewee",answer_id,interviewee.get(0));		
				}
				for(file : files_in_current_ques){
					saveEntitiesToRel("Answer and File",answer_id,file.get(0));		
				}
				showToast("new answer created");
			}
			else{
				for(interviewer : selected_answer_interviewer){
					saveEntitiesToRel("Answer and Interviewer",answer_id,interviewer.get(0));
				}
				for(interviewee : selected_answer_interviewee){
					saveEntitiesToRel("Answer and Interviewee",answer_id,interviewee.get(0));		
				}
				for(file : files_in_current_ques){
					saveEntitiesToRel("Answer and File",answer_id,file.get(0));		
				}
			}
			current_answer_id=answer_id;
		}
		onError(message) {
			showWarning("error",message);
		}  
		});
	}
	else{
		
		answerInfoNew.add(getFieldValue("survey/answer/answerChoice"));
		answerInfoNew.add(getFieldValue("survey/answer/answerText"));
		
		Hashtable interviewerChange=listChange(selected_answer_interviewer,origin_selected_interviewer);
		Hashtable intervieweeChange=listChange(selected_answer_interviewee,origin_selected_interviewee);
		Hashtable fileListChange=listChange(files_in_current_ques,files_origin);
		Hashtable answerBasicInfoChange=listChange(answerInfoNew,answerInfoOriginal);
		

		if(answerBasicInfoChange.containsKey("EQUAL")){//no basic info is changed in the answer basic info
			if((interviewerChange.containsKey("EQUAL"))&&(intervieweeChange.containsKey("EQUAL"))&&(fileListChange.containsKey("EQUAL"))){
				showWarning("Answer Modification","No data is changed");
				return;
			}
			else if((interviewerChange.containsKey("EQUAL"))&&(intervieweeChange.containsKey("EQUAL"))&&(!fileListChange.containsKey("EQUAL"))){
				for(file : files_in_current_ques){
					saveEntitiesToRel("Answer and File",current_answer_id,file.get(0));		
				}
				files_origin.clear();
				files_origin.addAll(files_in_current_ques);
				showToast("file list changed");
			}
			else{
				if(!fileListChange.containsKey("EQUAL")){
					for(file : files_in_current_ques){
						saveEntitiesToRel("Answer and File",current_answer_id,file.get(0));		
					}
					files_origin.clear();
					files_origin.addAll(files_in_current_ques);
					showToast("file list changed");
				}
				for(interviewer : selected_answer_interviewer){
					saveEntitiesToRel("Answer and Interviewer",current_answer_id,interviewer.get(0));
				}
				for(interviewee : selected_answer_interviewee){
					saveEntitiesToRel("Answer and Interviewee",current_answer_id,interviewee.get(0));		
				}
				origin_selected_interviewer.clear();
				origin_selected_interviewer.addAll(selected_answer_interviewer);
				origin_selected_interviewee.clear();
				origin_selected_interviewee.addAll(selected_answer_interviewee);			
				showToast("Interviewee and interviewer lists changed");
			}
		}
		else{//basic info is changed
			setFieldValue("survey/answer/answerEndTimestamp",getCurrentTime());
			saveTabGroup("survey", current_answer_id, null, null, new SaveCallback() {
				onSave(uuid, newRecord) {
					answer_id = uuid;					
						for(interviewer : selected_answer_interviewer){
							saveEntitiesToRel("Answer and Interviewer",current_answer_id,interviewer.get(0));
						}
						for(interviewee : selected_answer_interviewee){
							saveEntitiesToRel("Answer and Interviewee",current_answer_id,interviewee.get(0));		
						}
						for(file : files_in_current_ques){
							saveEntitiesToRel("Answer and File",current_answer_id,file.get(0));		
						}
						answerInfoOriginal.clear();
						answerInfoOriginal.addAll(answerInfoNew);
						origin_selected_interviewer.clear();
						origin_selected_interviewer.addAll(selected_answer_interviewer);
						origin_selected_interviewee.clear();
						origin_selected_interviewee.addAll(selected_answer_interviewee);
						showToast("Answer Info Changed");
				}
				onError(message) {
					showWarning("error",message);
				}  
				});
		}
		
	}
}
//measure whether two arraylists are identical, if not, recording what kinds of operation have been done
listChange(ArrayList targetList,ArrayList sourceList){
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
	target_list.addAll(targetList);
	source_list.addAll(sourceList);
	target_list.removeAll(source_list);
	target_diff_on_source.addAll(target_list);//target - source
	target_list.clear();
	target_list.addAll(targetList);
	source_list.removeAll(target_list);
	source_diff_on_target.addAll(source_list);//source - target
	if((target_diff_on_source.isEmpty()) && (source_diff_on_target.isEmpty())){
		listChanges.put("EQUAL",targetList);//here can not put ("EQUAL",null)
	}
	else if((!target_diff_on_source.isEmpty()) && (source_diff_on_target.isEmpty())){
		listChanges.put("PUREADD",target_diff_on_source);

	}
	else if((target_diff_on_source.isEmpty()) && (!source_diff_on_target.isEmpty())){
		listChanges.put("PUREDELETE",source_diff_on_target);
	}
	else {
		listChanges.put("ADD",target_diff_on_source);
		listChanges.put("DELETE",source_diff_on_target);
	}
	return listChanges;
}

newFileFromQuestion(){
	if((isNull(selected_answer_interviewer)) || (isNull(selected_answer_interviewee))){
		showWarning("Warning","Please select interviewers and interviewees");
		return;
	}
	if((selected_answer_interviewer.isEmpty()) || (selected_answer_interviewee.isEmpty())){
		showWarning("Warning","No available interviwer or interviewee\n Please create person info");
		return;
	}
	String fileCategory=getFieldValue("survey/answer/file_Category");
	switch (fileCategory){
	case "Audio":		
		newTabGroup("audioFile");
		setFieldValue("audioFile/audioFileInfo/audioFileID","AudioFile-"+username+getCurrentTime());
		setFieldValue("audioFile/audioFileInfo/audioFileCreator",username);
		setFieldValue("audioFile/audioFileInfo/audioFileType","Audio");
		break;
	case "Video":
		newTabGroup("videoFile");
		setFieldValue("videoFile/videoFileInfo/videoFileID","VideoFile-"+username+getCurrentTime());
		setFieldValue("videoFile/videoFileInfo/videoFileCreator",username);
		setFieldValue("videoFile/videoFileInfo/videoFileType","Video");
		break;
	case "Photo":
		newTabGroup("photoFile");
		setFieldValue("photoFile/photoFileInfo/photoFileID","PhotoFile-"+username+getCurrentTime());
		setFieldValue("photoFile/photoFileInfo/photoFileCreator",username);
		setFieldValue("photoFile/photoFileInfo/photoFileType","Photo");
		break;
	case "Other":
		newTabGroup("sketchFile");
		setFieldValue("sketchFile/sketchFileInfo/sketchFileID","SketchFile-"+username+getCurrentTime());
		setFieldValue("sketchFile/sketchFileInfo/sketchFileCreator",username);
		setFieldValue("sketchFile/sketchFileInfo/sketchFileType","Sketch");
		break;
	}
}
viewOrDeleteFileReln(){
	showAlert("View File Info","Do you want to view file info?","loadAnswerFileInfo()","deleteRelnAlert()");
}
deleteRelnAlert(){
	showAlert("Delete File","Do you want to delete this file from this answer?","deleteFileRelation()","returnToCurrentPage()");
}
deleteFileRelation(){		
	delete_file_id=getListItemValue();
	if(isNull(delete_file_id)){
		showWarning("Error","No file selected or file is not available,please contact the admin");
		return;
	}
	if(files_in_current_ques.size()==1){
		placeholder=new ArrayList();
		placeholder.add("0000");
		placeholder.add("No-file-placeholder");
		files_in_current_ques.add(placeholder);
	}
	for(deleteFile:files_in_current_ques){
		if(deleteFile.get(0).equals(delete_file_id))
		{
			files_in_current_ques.remove(deleteFile);
			populateList("survey/answer/answerFileList",files_in_current_ques);
			break;
		}
	}
	/*
	for(int i=0;i<files_in_current_ques.size();i++){
		if(files_in_current_ques.get(i).get(0).equals(delete_file_id))
		{
			files_in_current_ques.remove(i);
			populateList("survey/answer/answerFileList",files_in_current_ques);
			break;
		}
	}
	*/
}
loadAnswerFileInfo(){
	current_answer_file_id=getListItemValue();
	checkFileTypeQuery="select measure from latestNonDeletedAentValue where latestNonDeletedAentValue.uuid="+current_answer_file_id+" "+
	"and latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeKey.AttributeName='FileType');";
	showWarning("checkFileTypeQuery",checkFileTypeQuery);
	fetchAll(checkFileTypeQuery,
			new FetchCallback() {
		        onFetch(result) {
					if (!isNull(result)) {
						//showWarning("fetchAll",result.getClass.getName());				
						String currentType=result.get(0).get(0);
						//showWarning("fetchAll",currentType);
						switch (currentType){
						case "Audio":		
							showTabGroup("audioFile", current_answer_file_id, new FetchCallback() {
						        onFetch(result) {						  
						            showToast("Loaded audio file"+result.getId());            
						        }
						        onError(message) {
						            showToast(message);
						        }
						    });
							break;
						case "Video":
							showTabGroup("videoFile", current_answer_file_id, new FetchCallback() {
						        onFetch(result) {						  
						            showToast("Loaded video file"+result.getId());            
						        }
						        onError(message) {
						            showToast(message);
						        }
						    });
							break;
						case "Photo":
							showTabGroup("photoFile", current_answer_file_id, new FetchCallback() {
						        onFetch(result) {						  
						            showToast("Loaded photo file"+result.getId());            
						        }
						        onError(message) {
						            showToast(message);
						        }
						    });
							break;
						case "Other":
							showTabGroup("sketchFile", current_answer_file_id, new FetchCallback() {
						        onFetch(result) {						  
						            showToast("Loaded sketch file"+result.getId());            
						        }
						        onError(message) {
						            showToast(message);
						        }
						    });
							break;
						}
					}
					
		        }

		        onError(message) {
		            showToast(message);
		        }
		    });
	
}


onEvent("audioFile/audioFileInfo/Take_Audio_File","click","attachAudioToField(\"audioFile/audioFileInfo/audioFileContent\")");
onEvent("audioFile/audioFileInfo/Save_New_Audio","click","saveFileFromAnswer(\"audioFile/audioFileInfo/audioFileName\",\"audioFile/audioFileInfo/audioFileContent\",\"audioFile\")");

saveFileFromAnswer(String ref, String fileListViewRef, String tabGroupRef){
	if(isNull(getFieldValue(ref))){
		showWarning("Warning","File name can not be null");
		return;
	}
	if(isNull(getFieldValue(fileListViewRef))){	
		showWarning("Warning","File not recorded");
		return;
	}
	else{
		saveTabGroup(tabGroupRef,current_answer_file_id, null, null, new SaveCallback() {
			onSave(uuid, newRecord) {
				current_answer_file_id = uuid;
				if (newRecord) {	
					newFile=new ArrayList();
					newFile.add(current_answer_file_id);
					newFile.add(getFieldValue(ref));
					files_in_current_ques.add(newFile);
					/*
					placeholder=new ArrayList();
					placeholder.add("0000");
					placeholder.add("No-file-placeholder");*/
					for(file:files_in_current_ques){
						if(file.get(0).equals("0000")){
							files_in_current_ques.remove(file);
							break;
						}
					}
					//showWarning("add file",files_in_current_ques.size().toString());
					populateList("survey/answer/answerFileList",files_in_current_ques);
					//saveEntitiesToRel("Answer and File",answer_id,current_answer_file_id);			
					showToast("New file record created");
				}
			}
			onError(message) {
				showWarning("error",message);
			}  
			});
		//showAlert("Notification","Do you want to save this file to the answer?\n(This will also save the answer)","saveFileToAnswer(\""+ref+"\",\""+tabGroupRef+"\")","saveFileOnly()");
	}
}

returnToCurrentPage(){
	return;
}

attachAudioToField(String ref) {
	if(!isNull(getFieldValue(ref))){
		showWarning("File exists","File exists, please Create New File");
		return;
	}
	recordAudio("setAudioToField(\""+ref+"\")");
}

/**
  */
setAudioToField(String ref) {
	String filePath = getLastAudioFilePath();
	List selectedFiles = null;
    if (isNull(getFieldValue(ref))) {
    	selectedFiles = new ArrayList();
    	selectedFiles.add(filePath);
    	addFile(ref, filePath);  	
    } else {
    	//selectedFiles = _convertPairsToList(getFieldValue(ref));
    	showWarning("File exists","File exists, please Create New File");
    }
    setFieldValue(ref, _convertListToPairs(selectedFiles));
    setFieldValue("audioFile/audioFileInfo/audioFilePath",filePath);
}

onEvent("videoFile/videoFileInfo/Take_Video_File","click","attachVideoToField(\"videoFile/videoFileInfo/videoFileContent\")");
onEvent("videoFile/videoFileInfo/Save_New_Video","click","saveFileFromAnswer(\"videoFile/videoFileInfo/videoFileName\",\"videoFile/videoFileInfo/videoFileContent\",\"videoFile\")");

attachVideoToField(String ref) {
	if(!isNull(getFieldValue(ref))){
		showWarning("File exists","File exists, please Create New File");
		return;
	}
	openVideo("setVideoToField(\""+ref+"\")");
}


setVideoToField(String ref) {
	String filePath = getLastVideoFilePath();
	List selectedFiles = null;
    if (isNull(getFieldValue(ref))) {
    	selectedFiles = new ArrayList();
    	selectedFiles.add(filePath);
    	addVideo(ref, filePath);
    } else {
    	//selectedFiles = _convertPairsToList(getFieldValue(ref));
    	showWarning("File exists","File exists, please Create New File");
    }
    setFieldValue(ref, _convertListToPairs(selectedFiles));
    setFieldValue("videoFile/videoFileInfo/videoFilePath",filePath);
}

onEvent("photoFile/photoFileInfo/Take_Photo_File","click","attachPictureToField(\"photoFile/photoFileInfo/photoFileContent\")");
onEvent("photoFile/photoFileInfo/Save_New_Photo","click","saveFileFromAnswer(\"photoFile/photoFileInfo/photoFileName\",\"photoFile/photoFileInfo/photoFileContent\",\"photoFile\")");

attachPictureToField(String ref) {
	if(!isNull(getFieldValue(ref))){
		showWarning("File exists","File exists, please Create New File");
		return;
	}
	openCamera("setPictureToField(\""+ref+"\")");
}

/**
  */
setPictureToField(String ref) {
	String filePath = getLastPictureFilePath();
	List selectedFiles = null;
    if (isNull(getFieldValue(ref))) {
    	selectedFiles = new ArrayList();
    	selectedFiles.add(filePath);
    	addPicture(ref, filePath);
    } else {
    	//selectedFiles = _convertPairsToList(getFieldValue(ref));
    	showWarning("File exists","File exists, please Create New File");
    }
    setFieldValue(ref, _convertListToPairs(selectedFiles));
    setFieldValue("photoFile/photoFileInfo/photoFilePath",filePath);
}

onEvent("sketchFile/sketchFileInfo/Take_Sketch_File","click","attachFileToField(\"sketchFile/sketchFileInfo/sketchFileContent\")");
onEvent("sketchFile/sketchFileInfo/Save_New_Sketch","click","saveFileFromAnswer(\"sketchFile/sketchFileInfo/sketchFileName\",\"sketchFile/sketchFileInfo/sketchFileContent\",\"sketchFile\")");
attachFileToField(String ref) {
	if(!isNull(getFieldValue(ref))){
		showWarning("File exists","File exists, please Create New File");
		return;
	}
	showFileBrowser("setFileToField(\""+ref+"\")");
}


setFileToField(String ref) {
	String filePath = getLastSelectedFilepath();
	List selectedFiles = null;
    if (isNull(getFieldValue(ref))) {
    	selectedFiles = new ArrayList();
    	selectedFiles.add(filePath);
    	addFile(ref, filePath);
    } else {
    	//selectedFiles = _convertPairsToList(getFieldValue(ref));
    	showWarning("File exists","File exists, please Create New File");
    }
    setFieldValue(ref, _convertListToPairs(selectedFiles));
    setFieldValue("sketchFile/sketchFileInfo/sketchFilePath",filePath);
}



/*******Person********/
onEvent("control/user_control/New_User","click","newPerson()");
onEvent("control/user_control","show","loadPerson()");
onEvent("control/user_control/userList","click","loadPersonInfo()");
onEvent("person/personInfo/Finish_New_Person","click","saveNewPerson()");
onEvent("person/personInfo/Take_Photo","click","attachPictureTo(\"person/personInfo/personPhoto\")");

person_id=null;
newPerson(){
	person_id=null;
	newTabGroup("person");
	onEvent("person", "show", "showTab(\"person/personInfo\");");  
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

saveNewPerson(){

	if(isNull(getFieldValue("person/personInfo/personName"))){
		showWarning("Validation Error", "You must fill in the Person Name before you can continue");
        return;
	}
	setFieldValue("person/personInfo/personID", getFieldValue("person/personInfo/personName")+getCurrentTime());
	
	saveTabGroup("person", person_id, null, null, new SaveCallback() {
    onSave(uuid, newRecord) {
      person_id = uuid;
      if (newRecord) {
		newPerson();
		//person_id=null;
        showToast("New record created");
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
		newLanguage();
        showToast("New record created");
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

onEvent("control/querytest/Submit","click","testQuery()");

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
