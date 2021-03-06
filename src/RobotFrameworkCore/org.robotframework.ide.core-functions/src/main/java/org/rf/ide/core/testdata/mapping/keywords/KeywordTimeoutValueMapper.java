/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class KeywordTimeoutValueMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public KeywordTimeoutValueMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        rt.setText(text);

        final KeywordTable keywordTable = robotFileOutput.getFileModel().getKeywordTable();
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        final UserKeyword keyword = keywords.get(keywords.size() - 1);
        final List<KeywordTimeout> timeouts = keyword.getTimeouts();
        if (timeouts.size() == 1) {
            timeouts.get(0).setTimeout(rt);
        } else {
            for (final KeywordTimeout timeout : timeouts) {
                if (timeout.getTimeout() != null && !timeout.getTimeout().getFilePosition().isNotSet()) {
                    timeout.setTimeout(rt);
                    break;
                }
            }
        }

        processingState.push(ParsingState.KEYWORD_SETTING_TIMEOUT_VALUE);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);

        if (state == ParsingState.KEYWORD_SETTING_TIMEOUT) {
            final KeywordTable keywordTable = robotFileOutput.getFileModel().getKeywordTable();
            final List<UserKeyword> keywords = keywordTable.getKeywords();
            final UserKeyword keyword = keywords.get(keywords.size() - 1);
            final List<KeywordTimeout> timeouts = keyword.getTimeouts();

            result = !checkIfHasAlreadyValue(timeouts);
        }
        return result;
    }

    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(final List<KeywordTimeout> keywordTimeouts) {
        boolean result = false;
        for (final KeywordTimeout setting : keywordTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessage().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}
