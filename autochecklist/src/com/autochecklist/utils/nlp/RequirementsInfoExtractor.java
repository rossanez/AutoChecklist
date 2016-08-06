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
			for (CoreLabel token : sentenceTokens) {
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);

				// Requirement: (S (NP ...) (VP (MD shall) (VP ...))
				if ("MD".equals(pos) && "shall".equals(word)) {
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
		                	foundARequirement = true;

		                	String rawRequirement = sentence.toString();
		                	if (processingARequirement) {
		                		String appendedReq = rawRequirement.replaceAll("\n", " ");
		                		Utils.println("Appending req.: " + appendedReq);
		                		mOutputBuilder.appendRequirementText(appendedReq);
		                	} else {
		                		mOutputBuilder.createNewRequirement(previousSentence.replaceAll("\n", " "));
		                		String newReq = rawRequirement.replaceAll("\n", " ");
		                		Utils.println("New req.: " + newReq);
                                mOutputBuilder.addRequirementText(newReq);
		                	}

		                	processingARequirement = true;
                            break;
		                }

		                // Skip the remaining parse tree.
		                if (foundARequirement) break;
		            }
		            
		            // Skip the remaining sentence tokens.
		            if (foundARequirement) break;
				}
			}

			if (!foundARequirement) processingARequirement = false;
			
			// TODO Workaround! This should ideally be implemented
			// at the plain text converter level.
			String previousSentenceCandidate = sentence.toString();
			if (!previousSentenceCandidate.startsWith("Page ")) {
			    previousSentence = previousSentenceCandidate;
			}
		}
    
    	return mOutputBuilder;
    }
}
