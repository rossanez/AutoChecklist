package com.autochecklist.utils.nlp;

import java.util.List;

import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.interfaces.IRequirementsInfoOutBuildable;

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

/* package */ class RequirementsInfoExtractor {

	private String mPlainText;
	private IRequirementsInfoOutBuildable mOutputBuilder;
	private ExpressionExtractor mReqIdExtractor;
	
	public RequirementsInfoExtractor(String text, IRequirementsInfoOutBuildable outputBuilder) {
		this.mPlainText = text;
		this.mOutputBuilder = outputBuilder;
		this.mReqIdExtractor = new ExpressionExtractor("RegexRules/requirementid.rules");
	}
	
	public void extract() {
    	Annotation document = NLPTools.getInstance().annotate(mPlainText);

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
				if ("MD".equals(pos)) {
					if (foundARequirement = hasRequirementStructure(sentence.toString())) {
						boolean newReq = createNewRequirement(previousSentence, sentence);
		            	if (processingARequirement && !newReq) {
		            		appendSentenceToCurrentRequirement(sentence);
		            	} else {
		            		processingARequirement = newReq;
		            	}

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
    }

	/**
	 * Checks if the sentence has the structure of a requirement.
	 * Requirements consist of a noun phrase, followed by a verbal phrase, which starts with the "shall" modal:
	 * (S (NP ...) (VP (MD shall) (VP ...))
	 * @param sentence The sentence.
	 * @return true if the sentence tokens have a requirement structure, false otherwise.
	 */
	private boolean hasRequirementStructure(String sentence) {
		Tree parseTree = NLPTools.getInstance().getParser().parse(sentence);

		// http://tides.umiacs.umd.edu/webtrec/stanfordParser/javadoc/index.html?edu/stanford/nlp/trees/tregex/TregexPattern.html
        TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << (NP=np $++ VP=vp)").matcher(parseTree);

        while (sentenceMatcher.find()) {
            //Tree sentenceTree = sentenceMatcher.getMatch();
            //Tree nounPhraseTree = sentenceMatcher.getNode("np");
			Tree verbalPhraseTree = sentenceMatcher.getNode("vp");

            TregexMatcher verbalPhraseMatcher = TregexPattern.compile("VP <<, MD=md").matcher(verbalPhraseTree);

            while (verbalPhraseMatcher.find()) {
            	// Consistency check.
            	List<Word> words = verbalPhraseMatcher.getNode("md").yieldWords();
            	if (words.size() != 1) {
            		continue;
            	}

            	// Found a requirement; Adding it to the output builder.
            	return true;
            }
        }

        return false;
	}

	private boolean isARequirementId(String str) {
		if (Utils.isTextEmpty(str)) return false;
		str = str.trim();
		String[] splitStr = str.split(" ");
		if (splitStr.length != 1) return false;

		List<Pair<String, String>> matched = mReqIdExtractor.extract(str);
		if (matched != null) {
			for (Pair<String, String> match : matched) {
				// To make sure the requirement ID is the first token, as there may be "[", "{", or others.
				if (str.startsWith(match.second)) return true;
			}
		}

		return false;
	}

	private Pair<String, String> retrieveRequirementId(String previousSentence, String requirement) {
		if (isARequirementId(previousSentence)) {
		    return new Pair<String, String>(previousSentence, requirement);
		} else {
			String[] tokens = requirement.split(" ");
			if ((tokens != null) && (tokens.length > 0)
				&& isARequirementId(tokens[0])) {
				requirement = requirement.substring(tokens[0].length() + 1);
				return new Pair<String, String>(tokens[0], requirement);
		    }			
		}

		return null;
	}
	
	private boolean createNewRequirement(String previousSentence, CoreMap sentence) {
		String newReq = sentence.toString().replaceAll("\n", " ");
		Pair<String, String> reqIdAndReqText = retrieveRequirementId(previousSentence.replaceAll("\n", " "), newReq);
		if (reqIdAndReqText == null) return false;

		mOutputBuilder.createNewRequirement(reqIdAndReqText.first);
		Utils.println("New req. ID: " + reqIdAndReqText.first);
        mOutputBuilder.addRequirementText(reqIdAndReqText.second);
        Utils.println("Req. text: " + reqIdAndReqText.second);

        return true;
	}

	private void appendSentenceToCurrentRequirement(CoreMap sentence) {
		String appendedReq = sentence.toString().replaceAll("\n", " ");
		Utils.println("Appending: " + appendedReq);
		mOutputBuilder.appendRequirementText(appendedReq);
	}
}
