public class DataResponse {
    int transaction;
    int flags;
    int questionsSection;
    int answersSection;
    int authorityRecordSection;
    int additionalRecordSection;
    String[] questionName;
    int questionType;
    int questionClass;
    int resourceRecord;
    int recordType;
    int recordClass;
    int TTL;
    int responseDataLength;
    int[] responseData;

    DataResponse(int transaction, int flags, int questionsSection, int answersSection, int authorityRecordSection,
                 int additionalRecordSection, String[] questionName, int questionType, int questionClass, int resourceRecord,
                 int recordType, int recordClass, int TTL, int responseDataLength, int[] responseData) {
        this.transaction = transaction;
        this.flags = flags;
        this.questionsSection = questionsSection;
        this.answersSection = answersSection;
        this.authorityRecordSection = authorityRecordSection;
        this.additionalRecordSection = additionalRecordSection;
        this.questionName = questionName;
        this.questionType = questionType;
        this.questionClass = questionClass;
        this.resourceRecord = resourceRecord;
        this.recordType = recordType;
        this.recordClass = recordClass;
        this.TTL = TTL;
        this.responseDataLength = responseDataLength;
        this.responseData = responseData;
    }
}
