package ceos.ipx.domain.terms.service;

import ceos.ipx.domain.terms.dto.TermsAgreementRequest;
import ceos.ipx.domain.terms.entity.TermsAgreementType;
import ceos.ipx.domain.terms.entity.UserTermsAgreement;
import ceos.ipx.domain.terms.repository.UserTermsAgreementRepository;
import ceos.ipx.domain.user.entity.User;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TermsAgreementService {

    private static final String CURRENT_TERMS_VERSION = "1.1";

    private final UserTermsAgreementRepository userTermsAgreementRepository;

    public void validateRequiredTermsAgreements(List<TermsAgreementRequest> termsAgreements) {
        Map<TermsAgreementType, Boolean> agreementMap = toAgreementMap(termsAgreements);

        if (!Boolean.TRUE.equals(agreementMap.get(TermsAgreementType.SERVICE_TERMS))
                || !Boolean.TRUE.equals(agreementMap.get(TermsAgreementType.PRIVACY_POLICY))) {
            throw new BusinessException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    public void saveTermsAgreements(User user, List<TermsAgreementRequest> termsAgreements) {
        Map<TermsAgreementType, Boolean> agreementMap = toAgreementMap(termsAgreements);

        List<UserTermsAgreement> userTermsAgreements = List.of(
                createUserTermsAgreement(user, TermsAgreementType.SERVICE_TERMS, agreementMap),
                createUserTermsAgreement(user, TermsAgreementType.PRIVACY_POLICY, agreementMap),
                createUserTermsAgreement(user, TermsAgreementType.MARKETING, agreementMap)
        );

        userTermsAgreementRepository.saveAll(userTermsAgreements);
    }

    private UserTermsAgreement createUserTermsAgreement(
            User user,
            TermsAgreementType type,
            Map<TermsAgreementType, Boolean> agreementMap
    ) {
        return UserTermsAgreement.builder()
                .user(user)
                .type(type)
                .agreed(Boolean.TRUE.equals(agreementMap.get(type)))
                .termsVersion(CURRENT_TERMS_VERSION)
                .build();
    }

    private Map<TermsAgreementType, Boolean> toAgreementMap(List<TermsAgreementRequest> termsAgreements) {
        Map<TermsAgreementType, Boolean> agreementMap = new EnumMap<>(TermsAgreementType.class);

        for (TermsAgreementRequest termsAgreement : termsAgreements) {
            agreementMap.put(termsAgreement.type(), termsAgreement.agreed());
        }

        return agreementMap;
    }
}