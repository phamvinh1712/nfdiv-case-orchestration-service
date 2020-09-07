package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import org.junit.Before;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.GeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;

public abstract class AbstractGeneralOrderGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    public static final String TEST_JUDGE_NAME = "Test Harrison-Test";
    public static final String TEST_JUDGE_TYPE = "judge type";
    public static final String TEST_GENERAL_ORDER_RECITALS = "general order recitals";
    public static final String TEST_GENERAL_ORDER_DATE_FORMATTED = "20 May 2010";
    public static final String TEST_GENERAL_ORDER_DATE = "2010-05-20";
    public static final String TEST_GENERAL_ORDER_DETAILS = "general order details";

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    public abstract GeneralOrderGenerationTask getTask();

    public Map<String, Object> executeShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = buildCaseData();

        GeneralOrder generalOrder = GeneralOrder.generalOrderBuilder()
            .ctscContactDetails(CTSC_CONTACT)
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_FAMILY_MAN_ID)
            .generalOrderDetails(TEST_GENERAL_ORDER_DETAILS)
            .generalOrderDate(TEST_GENERAL_ORDER_DATE_FORMATTED)
            .generalOrderRecitals(TEST_GENERAL_ORDER_RECITALS)
            .judgeType(TEST_JUDGE_TYPE)
            .judgeName(TEST_JUDGE_NAME)
            .coRespondentFullName(TEST_CO_RESPONDENT_FULL_NAME)
            .hasCoRespondent(true)
            .build();

        Map<String, Object> returnedCaseData = getTask().execute(prepareTaskContext(), caseData);

        runVerifications(
            caseData,
            returnedCaseData,
            getTask().getDocumentType(),
            getTask().getTemplateId(),
            generalOrder
        );

        assertNotNull(returnedCaseData);

        return returnedCaseData;
    }

    protected abstract void runVerifications(Map<String, Object> expectedIncomingCaseData,
                                             Map<String, Object> returnedCaseData,
                                             String expectedDocumentType,
                                             String expectedTemplateId,
                                             DocmosisTemplateVars expectedDocmosisTemplateVars
    );

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);

        caseData.put(CO_RESPONDENT_LINKED_TO_CASE, YES_VALUE);
        caseData.put(JUDGE_TYPE, TEST_JUDGE_TYPE);
        caseData.put(JUDGE_NAME, TEST_JUDGE_NAME);
        caseData.put(GENERAL_ORDER_DETAILS, TEST_GENERAL_ORDER_DETAILS);
        caseData.put(GENERAL_ORDER_DATE, TEST_GENERAL_ORDER_DATE);
        caseData.put(GENERAL_ORDER_RECITALS, TEST_GENERAL_ORDER_RECITALS);

        caseData.put(GENERAL_ORDER_DRAFT, new HashMap<>());

        return caseData;
    }
}