/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.rflint;

import java.io.File;

public interface RfLintClientEventsListener {

    void filesToProcess(int numberOfFiles);

    void processingStarted(File filepath);

    void processingEnded(File filepath);

    void violationFound(File filepath, int line, int character, String ruleName, RfLintViolationSeverity severity,
            String message);

    void analysisFinished();

    void analysisFinished(String errorMsg);
}
