package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

@Component
public class BulkScanFormValidatorFactory {

    @Autowired
    private NewDivorceCaseValidator newDivorceCaseValidator;

    private static Map<String, BulkScanFormValidator> validators;

    @PostConstruct
    public void initBean() {
        validators = new HashMap<>();
        validators.put(BulkScanForms.NEW_DIVORCE_CASE, newDivorceCaseValidator);
    }

    public BulkScanFormValidator getValidator(final String formType) throws UnsupportedFormTypeException {
        if (!validators.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return validators.get(formType);
    }

}