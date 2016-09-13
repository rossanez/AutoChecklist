package com.autochecklist.utils.nlp;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class EventActionDetector {

	private LexicalizedParser mParser;

	public EventActionDetector() {
		mParser = CoreNLP.getInstance().getParser();
	}

	public boolean detect(String text) {
		Annotation document = CoreNLP.getInstance().annotate(text);

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			if (detectOnSentece(sentence.toString())) return true;
		}

		return false;
	}

	private boolean detectOnSentece(String sentence) {
		// TODO implement it!
		Tree parseTree = null;//mParser.parse(sentence);
		return parseTree != null;
	}
}
