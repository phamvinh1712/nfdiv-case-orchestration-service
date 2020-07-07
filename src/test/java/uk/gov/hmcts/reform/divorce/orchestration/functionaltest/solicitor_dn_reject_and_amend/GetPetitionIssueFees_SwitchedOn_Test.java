package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.solicitor_dn_reject_and_amend;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@SpringBootTest(properties = {"feature-toggle.toggle.solicitor_dn_reject_and_amend=true"})
public class GetPetitionIssueFees_SwitchedOn_Test extends GetPetitionIssueFeesAbstractTest {

    @Test
    public void givenCaseData_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(550d)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubGetFeeFromFeesAndPayments(feeResponse, false);
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(
                PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary,
                SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, "550"
            ))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenCaseAmendment_whenGetPetitionIssueFee_thenReturnUpdatedResponseWithFees() throws Exception {
        callbackRequest.getCaseDetails().getCaseData().put(PREVIOUS_CASE_ID_CCD_KEY, new CaseLink("1234567890123456"));

        FeeResponse feeResponse = FeeResponse.builder()
            .amount(95d)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        stubGetFeeFromFeesAndPayments(feeResponse, true);
        stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus.OK);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(ImmutableMap.of(
                PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary,
                SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, "95"
            ))
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(callbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

}