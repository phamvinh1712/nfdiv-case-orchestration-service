package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS;

@Component
public class CoRespondentAnswersGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;
    private final DocumentTemplateService documentTemplateService;

    @Autowired
    public CoRespondentAnswersGenerator(DocumentGeneratorClient documentGeneratorClient, DocumentTemplateService documentTemplateService) {
        this.documentGeneratorClient = documentGeneratorClient;
        this.documentTemplateService = documentTemplateService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        CaseDetails caseDataForDoc = CaseDetails.builder().caseData(payload).build();
        final String templateId = getTemplateId(documentTemplateService, DocumentType.CO_RESPONDENT_ANSWERS,
                payload);

        try {
            GeneratedDocumentInfo coRespondentAnswers =
                    documentGeneratorClient.generatePDF(
                            GenerateDocumentRequest.builder()
                                    .template(templateId)
                                    .values(ImmutableMap.of(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDataForDoc))
                                    .build(),
                            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
                    );

            coRespondentAnswers.setDocumentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS);
            coRespondentAnswers.setFileName(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS);

            final HashSet<GeneratedDocumentInfo> documentCollection =
                    context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new LinkedHashSet<>());
            documentCollection.add(coRespondentAnswers);
        } catch (Exception e) {
            throw new TaskException("Unable to generate or store Co-Respondent answers.", e);
        }

        return payload;
    }
}
