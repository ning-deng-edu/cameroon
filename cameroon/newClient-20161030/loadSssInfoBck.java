loadSessionInfo(String typeFlag){

		sss_id=getListItemValue();
		if(isNull(sss_id)){
			showWarning("Invalid session","No session is selected or session is not available");
			return;
		}
		createMainSessionPage("s");
		loadAnswerForSessionQuery="select uuid,measure from latestNonDeletedAentValue "+ 
				"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerLabel') "+
				"and uuid in "+
	 			"(select uuid from AentReln where RelationshipID in "+
				"(select RelationshipID from AEntReln where AEntReln.uuid="+sss_id+" "+
	 			"AND RelationshipID in "+
				"(select RelationshipID from latestNonDeletedRelationship where RelnTypeID="+
	 			"(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') "+
				"and latestNonDeletedRelationship.Deleted IS NULL)))";

		loadSessionTypeNamesQuery="select measure from latestNonDeletedAentValue "+
			"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirTypeName') "+
			"and uuid in "+
			"(select measure from latestNonDeletedAentValue "+
			"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionnaireType') "+
			"and uuid in (select measure from latestNonDeletedAentValue "+
			"where latestNonDeletedAentValue.AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerQuestionnaireID') "+
			"and uuid in (select uuid from AentReln where RelationshipID in "+
			"(select RelationshipID from AEntReln where AEntReln.uuid='"+sss_id+"' "+
			"AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship "+
			"where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and Session') "+
			"and latestNonDeletedRelationship.Deleted IS NULL))) group by measure))";

		loadAnsSssRelnQuery="select RelationshipID, uuid from latestNonDeletedAentReln where uuid <> '"+sss_id+
		"' and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sss_id+
			"' and RelationshipID in(select RelationshipID from latestNonDeletedRelationship "+
				"where RelnTypeID= (select RelnTypeID from RelnType where RelnTypeName='Answer and Session')"+
				"and latestNonDeletedRelationship.Deleted IS NULL))";

		//Query out the reln id of person and person roles in a session
		loadSssPersonRelnQuery="select uuid from latestNonDeletedArchEntIdentifiers "+
		"where AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') "+
		"and measure='"+sss_id+"' ";

		loadSssConslRelnQuery="select RelationshipID, uuid from latestNonDeletedAentReln where uuid <>'"+sss_id+"' "+
		"and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sss_id+
			"' and RelationshipID in (select RelationshipID from latestNonDeletedRelationship "+
				"where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and Consultant') "+
				"and latestNonDeletedRelationship.Deleted IS NULL))";

		loadSssIntvRelnQuery="select RelationshipID, uuid from latestNonDeletedAentReln where uuid <>'"+sss_id+"' "+
		"and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sss_id+
			"' and RelationshipID in (select RelationshipID from latestNonDeletedRelationship "+
				"where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and Interviewer') "+
				"and latestNonDeletedRelationship.Deleted IS NULL))";


		loadSssPsRelnQuery="select RelationshipID, uuid from latestNonDeletedAentReln where uuid <>'"+sss_id+"' "+
		"and RelationshipID in (select RelationshipID from latestNonDeletedAentReln where uuid='"+sss_id+
			"' and RelationshipID in (select RelationshipID from latestNonDeletedRelationship "+
				"where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Session and Person') "+
				"and latestNonDeletedRelationship.Deleted IS NULL))";
		
		fetchAll(loadAnsSssRelnQuery, new FetchCallback() {
	        onFetch(result) {
	        	sssAnsRelnOrigin.clear();
	        	sssAnsRelnOrigin.addAll(result);
	        }
	    });

		fetchAll(loadSssConslRelnQuery, new FetchCallback() {
	        onFetch(result) {
	        	sssOldCoslReln.clear();
	        	sssOldCoslReln.addAll(result);
	        }
	    });
	    fetchAll(loadSssIntvRelnQuery, new FetchCallback() {
	        onFetch(result) {
	        	sssOldIntvReln.clear();
	        	sssOldIntvReln.addAll(result);
	        }
	    });
	    fetchAll(loadSssPsRelnQuery, new FetchCallback() {
	        onFetch(result) {
	        	sssOldPsReln.clear();
	        	sssOldPsReln.addAll(result);
	        }
	    });


		showTabGroup("sessionForAnswer", sss_id, new FetchCallback() {
	        onFetch(result) {
				sssOriginInfo.clear();
	        	sssLabelOld=getFieldValue("sessionForAnswer/sssHidden/sssID");
	        	sssOriginInfo.add(sssLabelOld);

	        	fetchAll(loadAnswerForSessionQuery, new FetchCallback() {
	                onFetch(answers) {
	                	original_sss_answer_list.clear();
	                	sss_answer_list.clear();
	               		original_sss_answer_list.addAll(answers);
	               		sss_answer_list.addAll(answers);
	                	populateList("sessionForAnswer/sssAnsList/sssAnswerList",original_sss_answer_list);
	                	checkSssQuesnirType("answer");
	                }

	                onError(message) {
	                    showToast(message);
	                }
	            });
	        	fetchAll(loadSessionTypeNamesQuery, new FetchCallback() {
			        onFetch(result) {
			        	if(!isNull(result)){
			        		StringBuilder sb=new StringBuilder();
			        		for (re:result){
			        			if (sb.length()>0)
			        			{
			        				sb.append(",");
			        			}
			        			sb.append(re.toString());
			        		}
			        		setFieldValue("sessionForAnswer/sssAnsBasicInfo/sssType",sb.toString());
			        	}
			        	else{
			        		setFieldValue("sessionForAnswer/sssAnsBasicInfo/sssType","");
			        	}
			        }
			        onError(message) {
			            showToast(message);
			        }
			    });

				sssPersonRoleNameListOrigin.clear();
	            sssPersonRoleandNameList.clear();
	            sssPsInfoRelnOrigin.clear();
	            fetchAll(loadSssPersonRelnQuery, new FetchCallback() {
	                onFetch(sssPsReln) {
	                	if(!isNull(sssPsReln)){
	                	//sssPsInfoRelnOrigin.addAll(sssPsReln);
	                	sssPsRelnList=new ArrayList();
	                	sssPsRelnList.clear();
	                	sssPsRelnList.addAll(sssPsReln);
	                	for (psReln: sssPsRelnList){
	                		String psRelnId=psReln.get(0);

	                		loadSssPersonRoleQuery="select uuid,measure from latestNonDeletedAentValue "+
	                		"where AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonRoleName') "+
	                		"and uuid=(select measure from latestNonDeletedAentValue where AttributeID="+
	                			"(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') "+
	                			"and uuid='"+psRelnId+"')";
							
	            			fetchOne(loadSssPersonRoleQuery,new FetchCallback() {
								onFetch(sssPsRoleInfo) {
									if(!isNull(sssPsRoleInfo)){
										sssPsRoleInfoList=new ArrayList();
										sssPsRoleInfoList.clear();
										sssPsRoleInfoList.addAll(sssPsRoleInfo);
										String psRoleID=sssPsRoleInfoList.get(0);
										String psRoleName=sssPsRoleInfoList.get(1);

				            			loadSssPersonNameQuery="select uuid,measure from latestNonDeletedAentValue "+
				                		"where AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonName') "+
				                		"and uuid=(select measure from latestNonDeletedAentValue where AttributeID="+
				                			"(select AttributeID from AttributeKey where AttributeName='SessionPersonName') "+
				                			"and uuid='"+psRelnId+"')";

										fetchOne(loadSssPersonNameQuery,new FetchCallback() {
											onFetch(sssPsNameInfo) {
												if(!isNull(sssPsNameInfo)){
													sssPsNameInfoList=new ArrayList();
													sssPsNameInfoList.clear();
													sssPsNameInfoList.addAll(sssPsNameInfo);
													String psNameID=sssPsNameInfoList.get(0);
													String psName=sssPsNameInfoList.get(1);
													psRoleNamePair=new ArrayList();
													psRoleNamePair.clear();
													psRoleNamePair.add(psRoleID+"_"+psNameID);
													psRoleNamePair.add(psRoleName+"_"+psName);
													psInfTriplePair=new ArrayList();
													psInfTriplePair.clear();
													psInfTriplePair.add(psRelnId);
													psInfTriplePair.add(psRoleID+"_"+psNameID);	
													sssPersonRoleNameListOrigin.add(psRoleNamePair);
													sssPersonRoleandNameList.add(psRoleNamePair);
													populateList("sessionForAnswer/sssPersonInfo/sssPersonList",sssPersonRoleandNameList);
													sssPsInfoRelnOrigin.add(psInfTriplePair);
												}
												else{
													populateList("sessionForAnswer/sssPersonInfo/sssPersonList",sssPersonRoleandNameList);
													showWarning("Out-of-date Data","Some person in this session may not be used anymore\n"+"Please contact the admin for further information");
												}
												
											    }  
										    });
								    }
								    else{
								    	populateList("sessionForAnswer/sssPersonInfo/sssPersonList",sssPersonRoleandNameList);
								    	showWarning("Out-of-date Data","Some person roles in this session may not be used anymore\n"+"Please contact the admin for further information");
								    }
								}  
							});
	                	}
	                  }

	                  else{
	                  	populateList("sessionForAnswer/sssPersonInfo/sssPersonList",sssPersonRoleandNameList);
	                  	showWarning("Out-of-date Data","This session may not be used anymore\n"+"Please contact the admin for further information");
	                  }
	                }

	                onError(message) {
	                    showToast(message);
	                }
	            });

				fetchAll(loadAllRoleQuery, new FetchCallback() {
		        	onFetch(result) {
						if (!isNull(result)) {
							personRoleList.clear();
							personRoleList.addAll(result);
							populateDropDown("sessionForAnswer/sssPersonInfo/sssPersonRole", personRoleList);	

							fetchAll(loadAllPersonQuery,new FetchCallback() {
					        	onFetch(fetchResult) {
									if (!isNull(fetchResult)) {
										personList.clear();
										personList.addAll(fetchResult);
										populateDropDown("sessionForAnswer/sssPersonInfo/sssPersonName", personList);	
										
									}	
									else{
								    	showWarning("No person data","No person data is available, please contact the admin");
										return;
									}
					       	 }

					        	onError(message) {
					            	showToast(message);
					        	}
					    	});
						}	
						else{
					    	showWarning("No Role data","No role data is available, please contact the admin");
							return;
						}
		       	    }

		        	onError(message) {
		            	showToast(message);
		        	}
		    	});

	        	
	            showToast("Loaded session"+result.getId());            
	        }
	        onError(message) {
	            showToast(message);
	        }
	    });
		
}