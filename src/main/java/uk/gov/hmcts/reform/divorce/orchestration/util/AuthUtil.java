package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.ForbiddenException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings("squid:S1118")
@Component
public class AuthUtil {

    private final AuthTokenValidator authTokenValidator;
    private final List<String> allowedToUpdatePayment;
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CODE = "code";

    @Value("${idam.client.redirect_uri}")
    private String authRedirectUrl;

    @Value("${idam.client.id}")
    private String authClientId;

    @Value("${idam.client.secret}")
    private String authClientSecret;

    @Value("${idam.citizen.username}")
    private String citizenUserName;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    private final IdamClient idamClient;

    @Autowired
    public AuthUtil(
                    IdamClient idamClient,
                    AuthTokenValidator authTokenValidator,
                    @Value("${idam.s2s-auth.services-allowed-to-update-payment-update}") List<String> allowedToUpdatePayment
    ) {
        this.idamClient = idamClient;
        this.authTokenValidator = authTokenValidator;
        this.allowedToUpdatePayment = allowedToUpdatePayment;
    }

    public String getCitizenToken() {
        return getIdamOauth2Token(citizenUserName, citizenPassword);
    }

    public String getCaseworkerToken() {
        return getIdamOauth2Token(caseworkerUserName, caseworkerPassword);
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.authenticateUser(username, password);
    }

    public String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

    public void assertIsServiceAllowedToUpdate(String token) throws AuthenticationError {
        String serviceName = this.authenticate(token);

        if (!allowedToUpdatePayment.contains(serviceName)) {
            throw new ForbiddenException(serviceName + " is not authorised to access this endpoint");
        }
    }

    private String authenticate(String authHeader) throws AuthenticationError {
        if (isBlank(authHeader)) {
            throw new AuthenticationError("Provided S2S token is missing or invalid");
        }

        return authTokenValidator.getServiceName(authHeader);
    }

}
