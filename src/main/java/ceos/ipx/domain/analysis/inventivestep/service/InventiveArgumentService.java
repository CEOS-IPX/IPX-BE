package ceos.ipx.domain.analysis.inventivestep.service;

import ceos.ipx.domain.analysis.inventivestep.dto.request.InventiveArgumentUpdateRequest;
import ceos.ipx.domain.analysis.inventivestep.dto.response.InventiveArgumentUpdateResponse;
import ceos.ipx.domain.analysis.inventivestep.entity.InventiveArgument;
import ceos.ipx.domain.analysis.inventivestep.repository.InventiveArgumentRepository;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventiveArgumentService {

    private final InventiveArgumentRepository inventiveArgumentRepository;

    @Transactional
    public InventiveArgumentUpdateResponse update(
            Long userId,
            Long argumentId,
            InventiveArgumentUpdateRequest request
    ) {
        if (request.hasNoUpdates()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        InventiveArgument argument = inventiveArgumentRepository.findById(argumentId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.INVENTIVE_ARGUMENT_NOT_FOUND)
                );

        Case caseEntity = argument.getAnalysis().getCaseEntity();

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        argument.update(request.content());

        return InventiveArgumentUpdateResponse.from(argument);
    }
}
