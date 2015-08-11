package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author mmarzec
 */
public class KeywordsContentAssistProcessor implements IContentAssistProcessor {
    
    private Image settingImage = ImagesManager.getImage(RedImages.getRobotSettingImage());

    private String lastError = null;

    private TextEditorContextValidator validator = new TextEditorContextValidator(this);

    private Map<String, ContentAssistKeywordContext> keywordMap;

    public KeywordsContentAssistProcessor(final Map<String, ContentAssistKeywordContext> keywordMap) {
        this.keywordMap = keywordMap;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        lastError = null;
        final IDocument document = viewer.getDocument();
        int currentOffset = offset - 1;

        try {
            String currentWord = "";

            if (currentOffset < 0 || document.getChar(currentOffset) == '\n' || document.getChar(currentOffset) == '*') {
                currentWord = TextEditorContentAssist.readEnteredWord(currentOffset, document);
                return TextEditorContentAssist.buildSectionProposals(currentWord, offset - currentWord.length());
            } else {

                currentWord = TextEditorContentAssist.readEnteredKeyword(currentOffset, document);
                if (currentWord == null) {
                    return new ICompletionProposal[0];
                }
                
                if(currentWord.startsWith("[")) {
                    return TextEditorContentAssist.buildSimpleProposals(TextEditorContentAssist.getKeywordsSectionWords(),
                            currentWord, offset - currentWord.length(), settingImage);
                }
                
                if (TextEditorContentAssist.shouldShowVariablesProposals(currentWord)) {
                    currentWord = TextEditorContentAssist.readEnteredVariable(currentOffset, document);
                    final Map<String, String> filteredProposals = TextEditorContentAssist.filterVariablesProposals(
                            TextEditorContentAssist.getVariables(), currentWord);
                    if (!filteredProposals.isEmpty()) {
                        return TextEditorContentAssist.buildVariablesProposals(filteredProposals, currentWord, offset
                                - currentWord.length());
                    } else {
                        return new ICompletionProposal[0];
                    }
                }
                
                final Map<String, ContentAssistKeywordContext> keywordProposals = TextEditorContentAssist.filterKeywordsProposals(
                        keywordMap, currentWord);
                ICompletionProposal[] proposals = null;
                if (keywordProposals.size() > 0) {
                    proposals = TextEditorContentAssist.buildKeywordsProposals(keywordProposals, currentWord, offset
                            - currentWord.length());
                }
                return proposals;
            }
        } catch (final BadLocationException e) {
            e.printStackTrace();
            lastError = e.getMessage();
        }

        return null;
    }

    @Override
    public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {

        return new IContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return lastError;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return validator;
    }

}
