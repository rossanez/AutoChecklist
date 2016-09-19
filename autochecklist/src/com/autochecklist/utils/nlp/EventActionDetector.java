package com.autochecklist.utils.nlp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class EventActionDetector {

	private LexicalizedParser mParser;
	private Set<String> mActionEventIndicators;

	public EventActionDetector() {
		mParser = CoreNLP.getInstance().getParser();
		initActionEventIndicators();
	}

	private void initActionEventIndicators() {
		mActionEventIndicators = new HashSet<String>();
		mActionEventIndicators.add("if");
		mActionEventIndicators.add("else");
	}

	public boolean detect(String text) {
		Annotation document = CoreNLP.getInstance().annotate(text);

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			if (detectOnSentece(sentence.toString())) return true;
		}

		return false;
	}

	private boolean detectOnSentece(String sentence) {
		Tree parseTree = mParser.parse(sentence);

		TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << SBAR=sbar").matcher(parseTree);

        while (sentenceMatcher.find()) {
        	Tree sbarPhraseTree = sentenceMatcher.getNode("sbar");

            TregexMatcher sbarPhraseMatcher = TregexPattern.compile("SBAR <<, IN=in").matcher(sbarPhraseTree);

            while (sbarPhraseMatcher.find()) {
            	List<Word> words = sbarPhraseMatcher.getNode("in").yieldWords();
            	if ((words.size() != 1) || !isActionEventIndicator(words.get(0).toString())) {
            		break;
            	}

            	return true;
            }
        }

		return false;
	}

	private boolean isActionEventIndicator(String word) {
		if (Utils.isTextEmpty(word)) return false;

		return mActionEventIndicators.contains(word.toLowerCase());
	}
}
