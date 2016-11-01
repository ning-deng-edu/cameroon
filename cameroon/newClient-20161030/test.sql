select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054')

select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')

select t1.quesId, t1.quesOrder || '-'  || t2. quesContent as qOrderedContent from (select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')) t1 inner join 
(select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))) t2 on t1.quesId=t2.quesId order by quesOrder asc;

select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))

select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054'

select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054')

select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')

select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))

select  t1.quesId, t1.quesOrder || '-' || t2.quesContent as qOrderedContent from (select quId.measure as quesId, qOrder.measure as quesOrder from latestNonDeletedAentValue as quId, latestNonDeletedAentValue as qOrder where qOrder.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesOrderLocal') and quId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quId.uuid=qOrder.uuid and quId.measure in(select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qOrder.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID = (select AttributeID from AttributeKey where AttributeName='QuesnirID') and measure='1000011477966601054')) t1 inner join (select qId.measure as quesId, qContent.measure as quesContent from latestNonDeletedAentValue as qId, latestNonDeletedAentValue as qContent where qContent.AttributeID=(SELECT AttributeID from AttributeKey where AttributeName='QuesContent') and qId.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid') and qId.uuid=qContent.uuid and qId.measure in (select quesInQuesnir.measure from latestNonDeletedAentValue as quesInQuesnir, latestNonDeletedAentValue as quesnir where quesInQuesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesID') and quesnir.AttributeID=(select AttributeID from AttributeKey where AttributeName='QuesnirID') and quesInQuesnir.uuid=quesnir.uuid and quesnir.measure='1000011477966601054') and qId.uuid in (select uuid from latestNonDeletedArchEntIdentifiers where AttributeID=(select AttributeID from AttributeKey where AttributeName='QuestionUuid'))) t2 on t1.quesId=t2.quesId order by qOrderedContent asc