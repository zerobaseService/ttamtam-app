package com.example.zero.healthcare.exception.journal;

import com.example.zero.healthcare.exception.common.AbstractException;
import com.example.zero.healthcare.exception.common.ErrorCode;

public class JournalForbiddenException extends AbstractException {

    public JournalForbiddenException() {
        super(ErrorCode.JOURNAL_FORBIDDEN);
    }
}
