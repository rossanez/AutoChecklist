package com.autochecklist.utils.nlp;

import java.util.List;

import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class RequirementsInfoExtractor {

	private String mPlainText;
	private IRequirementsInfoOutBuildable mOutputBuilder;
	
	public RequirementsInfoExtractor(String text, IRequirementsInfoOutBuildable outputBuilder) {
		this.mPlainText = text;
		this.mOutputBuilder = outputBuilder;
	}
	
	public IRequirementsInfoOutBuildable extract() {
    	Annotation document = CoreNLP.getInstance().annotate(mPlainText);

    	Utils.println("Extracting requirements...");

    	// This flag will tell if we are currently processing a requirement.
    	boolean processingARequirement = false;
    	String previousSentence = null;
    	for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			List<CoreLabel> sentenceTokens = sentence.get(TokensAnnotation.class);

			boolean foundARequirement = false;
			String lastWord = null;
			for (CoreLabel token : sentenceTokens) {
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);

				// Is it a requirement candidate?
				if ("MD".equals(pos) && "shall".equals(word)) {
					if (foundARequirement = hasRequirementStructure(sentenceTokens)) {
		            	if (processingARequirement) {
		            		appendSentenceToCurrentRequirement(sentence);
		            	} else {
		            		createNewRequirement(previousSentence, sentence);
		            	}

		            	processingARequirement = true;
		                break;
					}
				}

				// Update lastWord
				lastWord = word;
			}

			if (!foundARequirement) {
				if (processingARequirement && ".".equals(lastWord)) {
					appendSentenceToCurrentRequirement(sentence);
				} else {
				    processingARequirement = false;
				}
			}
			
			// TODO Workaround! This should ideally be implemented
			// at the plain text converter level.
			String previousSentenceCandidate = sentence.toString();
			if (!previousSentenceCandidate.startsWith("Page ")) {
			    previousSentence = previousSentenceCandidate;
			}
		}
    
    	return mOutputBuilder;
    }

	/**
	 * Checks if the sentence (already converted to a list of tokens) has the structure of a requirement.
	 * Requirements consist of a noun phrase, followed by a verbal phrase, which starts with the "shall" modal:
	 * (S (NP ...) (VP (MD shall) (VP ...))
	 * @param sentenceTokens The list of tokens.
	 * @return true if the sentence tokens have a requirement structure, false otherwise.
	 */
	private boolean hasRequirementStructure(List<CoreLabel> sentenceTokens) {
		Tree parseTree = CoreNLP.getInstance().getParser().apply(sentenceTokens);

		// http://tides.umiacs.umd.edu/webtrec/stanfordParser/javadoc/index.html?edu/stanford/nlp/trees/tregex/TregexPattern.html
        TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << (NP=np $++ VP=vp)").matcher(parseTree);

        while (sentenceMatcher.find()) {
            //Tree sentenceTree = sentenceMatcher.getMatch();
            //Tree nounPhraseTree = sentenceMatcher.getNode("np");
			Tree verbalPhraseTree = sentenceMatcher.getNode("vp");

            TregexMatcher verbalPhraseMatcher = TregexPattern.compile("VP <<, MD=md").matcher(verbalPhraseTree);

            while (verbalPhraseMatcher.find()) {

            	// Consistency check - it should be "shall"!
            	List<Word> words = verbalPhraseMatcher.getNode("md").yieldWords();
            	if ((words.size() != 1) || !"shall".equals(words.get(0).toString())) {
            		break;
            	}

            	// Found a requirement; Adding it to the output builder.
            	return true;
            }
        }

        return false;
	}

	private void createNewRequirement(String previousSentence, CoreMap sentence) {
		mOutputBuilder.createNewRequirement(previousSentence.replaceAll("\n", " "));
		String newReq = sentence.toString().replaceAll("\n", " ");
		Utils.println("New req.: " + newReq);
        mOutputBuilder.addRequirementText(newReq);
	}

	private void appendSentenceToCurrentRequirement(CoreMap sentence) {
		String appendedReq = sentence.toString().replaceAll("\n", " ");
		Utils.println("Appending: " + appendedReq);
		mOutputBuilder.appendRequirementText(appendedReq);
	}
}
