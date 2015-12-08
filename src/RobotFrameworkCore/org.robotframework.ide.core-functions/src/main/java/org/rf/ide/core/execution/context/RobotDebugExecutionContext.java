/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.execution.context.ExecutableRowFindersManager;
import org.rf.ide.core.execution.context.IRobotExecutableRowFinder;


/**
 * @author mmarzec
 *
 */
public class RobotDebugExecutionContext {
    
    public static final String MAIN_KEYWORD_TYPE = "Keyword";

    public static final String LOOP_KEYWORD_TYPE = "Test For";

    public static final String TESTCASE_SETUP_KEYWORD_TYPE = "Test Setup";
    
    public static final String TESTCASE_TEARDOWN_KEYWORD_TYPE = "Test Teardown";
    
    public static final String SUITE_SETUP_KEYWORD_TYPE = "Suite Setup";
    
    public static final String SUITE_TEARDOWN_KEYWORD_TYPE = "Suite Teardown";
    
    private RobotFile currentModel;
    
    private final List<KeywordContext> currentKeywords;
    
    private final TestCaseExecutionRowCounter testCaseExecutionRowCounter;
    
    private final ExecutableRowFindersManager executableRowFindersManager;
    
    private boolean isSetupTeardownKeywordStarted;
    private boolean isForLoopStarted;
    private int forLoopsCounter = 0;
    
    public RobotDebugExecutionContext() {
        currentKeywords = new ArrayList<>();
        testCaseExecutionRowCounter = new TestCaseExecutionRowCounter();
        executableRowFindersManager = new ExecutableRowFindersManager();
    }
    
    public void startSuite(final RobotFileOutput robotFileOutput) {
        currentModel = robotFileOutput.getFileModel();
        executableRowFindersManager.initFindersAtSuiteStart(currentModel, currentModel.getKeywordTable().getKeywords(),
                robotFileOutput.getResourceImportReferences());
    }

    public boolean startTest(final String testName) {
        if (currentModel != null) {
            final TestCaseTable testCaseTable = currentModel.getTestCaseTable();
            final List<TestCase> testCases = testCaseTable.getTestCases();
            for (final TestCase testCase : testCases) {
                if (testCase.getTestName().getText().toString().equalsIgnoreCase(testName)) {
                    executableRowFindersManager.initFindersAtTestCaseStart(testCase);
                    return true;
                }
            }
        }
        return false;
    }

    public void startKeyword(final String name, final String type, final List<String> arguments) {
        currentKeywords.add(new KeywordContext(name, type));
        checkStartKeywordType(type);
    }

    public void endKeyword(final String type) {
        currentKeywords.remove(currentKeywords.size() - 1);
        checkEndKeywordType(type);
    }

    public void endTest() {
        testCaseExecutionRowCounter.reset();
    }
    
    public KeywordPosition findKeywordPosition() {
        final IRobotExecutableRowFinder executableRowFinder = getExecutableRowFinder();
        final RobotExecutableRow<?> executableRow = findExecutableRow(executableRowFinder);
        return createNewKeywordPosition(executableRow);
    }
  
    public IRobotExecutableRowFinder getExecutableRowFinder() {
        IRobotExecutableRowFinder executableRowFinder = null;
        if (isKeywordDirectlyFromTestCase()) {
            if (isSetupTeardownKeywordStarted) {
                executableRowFinder = executableRowFindersManager.provideSetupTeardownExecutableRowFinder();
            } else {
                executableRowFinder = executableRowFindersManager.provideTestCaseExecutableRowFinder(testCaseExecutionRowCounter);
            }
        } else if (isForLoopStarted) {
            executableRowFinder = executableRowFindersManager.provideForLoopExecutableRowFinder(testCaseExecutionRowCounter);
        } else { // keyword from Keywords section or resource file
            executableRowFinder = executableRowFindersManager.provideUserKeywordExecutableRowFinder();
        }
        
        return executableRowFinder;
    }
    
    private RobotExecutableRow<?> findExecutableRow(final IRobotExecutableRowFinder executableRowFinder) {
        RobotExecutableRow<?> executableRow = null;
        if (executableRowFinder != null) {
            executableRow = executableRowFinder.findExecutableRow(currentKeywords);
        }
        return executableRow;
    }
    
    private KeywordPosition createNewKeywordPosition(final RobotExecutableRow<?> executableRow) {

        final KeywordPosition keywordPosition = new KeywordPosition();
        final KeywordContext parentKeywordContext = getParentKeywordContext();
        if (parentKeywordContext != null && parentKeywordContext.getResourceImportReference() != null) {
            keywordPosition.setFilePath(parentKeywordContext.getResourceImportReference()
                    .getReference()
                    .getProcessedFile()
                    .getAbsolutePath());
        }
        keywordPosition.setLineNumber(executableRow != null ? executableRow.getAction().getLineNumber() : -1);
        return keywordPosition;
    }
    
    private KeywordContext getParentKeywordContext() {
        KeywordContext parentKeywordContext = null;
        if ((currentKeywords.size() - 2) >= 0) {
            parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);
        }
        return parentKeywordContext;
    }

    private void checkStartKeywordType(final String type) {
        if(type.equalsIgnoreCase(LOOP_KEYWORD_TYPE)) {
            forLoopsCounter++;
        } else if (forLoopsCounter > 0) {
            isForLoopStarted = true;
        } else if (isSetupTeardownStart(type)) {
            isSetupTeardownKeywordStarted = true;
        }
    }

    private boolean isSetupTeardownStart(final String keywordType) {
        return !keywordType.equalsIgnoreCase(MAIN_KEYWORD_TYPE) && !keywordType.equalsIgnoreCase(LOOP_KEYWORD_TYPE);
    }

    private void checkEndKeywordType(final String keywordType) {
        if (keywordType.equalsIgnoreCase(LOOP_KEYWORD_TYPE)) {
            forLoopsCounter--;
            if(forLoopsCounter == 0) {
                isForLoopStarted = false;
                executableRowFindersManager.clearForLoopState();
            }
        }
        if (keywordType.equalsIgnoreCase(TESTCASE_SETUP_KEYWORD_TYPE)
                || keywordType.equalsIgnoreCase(TESTCASE_TEARDOWN_KEYWORD_TYPE)
                || keywordType.equalsIgnoreCase(SUITE_SETUP_KEYWORD_TYPE)
                || keywordType.equalsIgnoreCase(SUITE_TEARDOWN_KEYWORD_TYPE)) {
            isSetupTeardownKeywordStarted = false;
        }
    }
    
    private boolean isKeywordDirectlyFromTestCase() {
        return currentKeywords.size() == 1;
    }

    protected static class KeywordContext {

        private final String name;
        
        private final String type;
        
        private int keywordExecutableRowCounter = 0;
        
        private ResourceImportReference resourceImportReference;
        
        private UserKeyword userKeyword;

        public KeywordContext(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getKeywordExecutableRowCounter() {
            return keywordExecutableRowCounter;
        }

        public void incrementKeywordExecutableRowCounter() {
            keywordExecutableRowCounter++;
        }

        public ResourceImportReference getResourceImportReference() {
            return resourceImportReference;
        }

        public void setResourceImportReference(final ResourceImportReference resourceImportReference) {
            this.resourceImportReference = resourceImportReference;
        }

        public UserKeyword getUserKeyword() {
            return userKeyword;
        }

        public void setUserKeyword(final UserKeyword userKeyword) {
            this.userKeyword = userKeyword;
        }

        public String getType() {
            return type;
        }

    }
    
    public class KeywordPosition {
        private int lineNumber;
        private String filePath;
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(final int lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(final String filePath) {
            this.filePath = filePath;
        }
    }
    
    static class TestCaseExecutionRowCounter {
        private int counter = 0;
        
        public void increment() {
            counter++;
        }
        
        public void reset() {
            counter = 0;
        }
        
        public int getCounter() {
            return counter;
        }
    }
}
