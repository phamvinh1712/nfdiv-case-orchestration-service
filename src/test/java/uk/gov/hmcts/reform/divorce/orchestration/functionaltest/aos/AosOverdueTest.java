package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aos;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOT_RECEIVED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@TestPropertySource(properties = {
    "AOS_OVERDUE_GRACE_PERIOD=0"
})
public class AosOverdueTest extends MockedFunctionalTest {

    private static final String TEST_CONFIGURED_AOS_OVERDUE_GRACE_PERIOD = "0d";

    @MockBean
    private AuthUtil authUtil;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);

        maintenanceServiceServer.stubFor(
            WireMock.post(urlPathMatching("/casemaintenance/version/1/updateCase/.*/" + NOT_RECEIVED_AOS_EVENT_ID))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .willReturn(ok()));
    }

    @Test
    public void shouldMoveEligibleCasesToAosOverdue() throws Exception {
        QueryBuilder expectedQuery = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, String.join(SPACE, AOS_AWAITING, AOS_STARTED)).operator(Operator.OR))
            .filter(QueryBuilders.rangeQuery("data." + DUE_DATE).lt(buildDateForTodayMinusGivenPeriod(TEST_CONFIGURED_AOS_OVERDUE_GRACE_PERIOD)));
        stubCaseMaintenanceSearchEndpoint(asList(
            CaseDetails.builder().state(AOS_STARTED).caseId("123").build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("456").build(),
            CaseDetails.builder().state(AOS_AWAITING).caseId("789").build()
        ), expectedQuery);

        mockMvc.perform(post("/cases/aos/make-overdue").header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());

        await().untilAsserted(() -> {
            verifyCaseWasUpdated("456");
            verifyCaseWasUpdated("789");
        });
        verifyCaseWasNotUpdated("123");
    }

    private void verifyCaseWasUpdated(String caseId) {
        verifyCaseWasMovedGivenNumberOfTimes(caseId, 1);
    }

    private void verifyCaseWasNotUpdated(String caseId) {
        verifyCaseWasMovedGivenNumberOfTimes(caseId, 0);
    }

    private void verifyCaseWasMovedGivenNumberOfTimes(String caseId, int expectedRequestCount) {
        maintenanceServiceServer.verify(expectedRequestCount, RequestPatternBuilder.newRequestPattern()
            .withUrl(format("/casemaintenance/version/1/updateCase/%s/%s", caseId, NOT_RECEIVED_AOS_EVENT_ID))
            .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
            .withRequestBody(equalToJson("{}"))
        );
    }

}