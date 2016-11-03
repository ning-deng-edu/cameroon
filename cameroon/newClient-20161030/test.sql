select t1.quesId, t1.quesOrder || '-'  || t2.quesContent as qOrderedContent from (select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')) t1 inner join (select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))) t2 on t1.quesId=t2.quesId order by quesOrder asc;

select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')

select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))

select t1.quesId, t1.quesOrder || '-' || t2.quesContent as qOrderedContent from (select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')) t1 inner join (select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))) t2 on t1.quesId=t2.quesId order by qOrderedContent asc

select uuid,RelationshipID from AentReln where uuid <>'1000011478038313179' and RelationshipID in (select RelationshipID from AEntReln where AEntReln.uuid='1000011478038313179' AND RelationshipID in (select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and File') and latestNonDeletedRelationship.Deleted IS NULL))

select RelationshipID from AentReln where AentReln.uuid='1000011478038313179' and RelationshipID in( select RelationshipID from latestNonDeletedRelationship where RelnTypeID=(select RelnTypeID from RelnType where RelnTypeName='Answer and File') and latestNonDeletedRelationship.Deleted IS NULL)


select t5.relnId, t5.personUuid, t5.persName, t6.personRoleUuid, t6.personRoleName from (select t1.personId as personUuid, t1.personName as persName, t2.psssId as relnId from (select pId.uuid as personId, pId.measure as personName from latestNonDeletedAentValue as pId where pId.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='PersonName') and pId.uuid in (select psName.measure from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='1000011478038316786' and psName.uuid=psReln.uuid)) t1 inner join (select psName.uuid as psssId, psName.measure as psId from latestNonDeletedAentValue as psName, latestNonDeletedAentValue as psReln where psName.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonName') and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='1000011478038316786' and psName.uuid=psReln.uuid) t2 on t1.personId=t2.psId) t5 inner join (select t3.roleId as personRoleUuid, t3.roleName as personRoleName, t4.rsssId as relnId from (select rId.uuid as roleId, rId.measure as roleName from latestNonDeletedAentValue as rId where rId.AttributeID=(select AttributeID from AttributeKey where AttributeName='PersonRoleName') and rId.uuid in (select psRole.measure from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='1000011478038316786' and psRole.uuid=psReln.uuid))t3 inner join (select psRole.uuid as rsssId, psRole.measure as tempPsRoleID from latestNonDeletedAentValue as psRole, latestNonDeletedAentValue as psReln where psRole.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionPersonRole') and psReln.AttributeID=(select AttributeID from AttributeKey where AttributeName='SessionIDforPerson') and psReln.measure='1000011478038316786' and psRole.uuid=psReln.uuid)t4 on t3.roleId=t4.tempPsRoleID) t6 on t5.relnId =t6.relnId

select langIntroQues.measure from latestNonDeletedAentValue as langIntroQues, latestNonDeletedAentValue as QuesType where langIntroQues.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesUuid') and QuesType.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesPropDefID') and langIntroQues.uuid=QuesType.uuid and QuesType.measure=(select QuesTypeID.uuid from latestNonDeletedAentValue as QuesTypeID where QuesTypeID.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesPropDefName') and QuesTypeID.measure='LangIntro' ) limit 1

select measure from latestNonDeletedAentValue where AttributeID=(select AttributeID from AttributeKey where AttributeName='AnswerQuestionID') and uuid='1000011478038313179'