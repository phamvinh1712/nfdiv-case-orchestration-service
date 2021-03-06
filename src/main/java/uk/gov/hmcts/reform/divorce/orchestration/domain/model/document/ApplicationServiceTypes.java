package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationServiceTypes {
    public static final String DISPENSED = "dispensed";
    public static final String DEEMED = "deemed";
    public static final String BAILIFF = "bailiff";
}
