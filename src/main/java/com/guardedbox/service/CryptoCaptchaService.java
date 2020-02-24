package com.guardedbox.service;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.guardedbox.constants.SessionAttributes;
import com.guardedbox.dto.ChallengeDto;
import com.guardedbox.dto.MinedChallengeResponseDto;
import com.guardedbox.exception.ServiceException;

import lombok.RequiredArgsConstructor;

/**
 * Captcha verification service based on challenge mining.
 *
 * @author s3curitybug@gmail.com
 *
 */
@Service
@RequiredArgsConstructor
public class CryptoCaptchaService {

    /** ChallengeService. */
    private final ChallengeService challengeService;

    /** Current Session. */
    private final HttpSession session;

    /**
     * Verifies a mined challenge response to the challenge stored in the current session.
     * Throws a ServiceException in case it is not verified.
     *
     * @param minedChallengeResponseDto Mined challenge response object.
     */
    public void verify(
            MinedChallengeResponseDto minedChallengeResponseDto) {

        // Check if a challenge was previously requested.
        ChallengeDto challengeDto = (ChallengeDto) session.getAttribute(SessionAttributes.CHALLENGE);
        if (challengeDto == null) {
            throw new ServiceException("Challenge is not stored in session");
        }

        // Remove the challenge from the current session.
        session.removeAttribute(SessionAttributes.CHALLENGE);

        // Verify the mined challenge response.
        if (!challengeService.verifyMinedChallengeResponse(minedChallengeResponseDto, challengeDto)) {
            throw new ServiceException("Challenge response is incorrect");
        }

    }

}
