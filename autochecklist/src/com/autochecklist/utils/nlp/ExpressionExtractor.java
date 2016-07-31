package com.autochecklist.utils.nlp;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.utils.Pair;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ExpressionExtractor {

	private CoreMapExpressionExtractor<MatchedExpression> mExpressionExtractor;

	@SuppressWarnings("unchecked")
	public ExpressionExtractor(String extractorRulesFile) {
		mExpressionExtractor = CoreMapExpressionExtractor.createExtractorFromFiles(TokenSequencePattern.getNewEnv(), extractorRulesFile);
	}

	public List<Pair<String, String>> extract(String text) {
		List<Pair<String, String>> retList = new ArrayList<Pair<String, String>>();
		
		Annotation annotated = CoreNLP.getInstance().annotate(text);
		List<CoreMap> sentences = annotated.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (MatchedExpression match : mExpressionExtractor.extractExpressions(sentence)) {
				String matchType = formatMatchValue(match.getValue().toString());
				String value = match.toString();
				retList.add(new Pair<String, String>(matchType, value));
			}
		}

		return retList;
	}

	private String formatMatchValue(String value) {
		int init = value.lastIndexOf('(');
		int end = value.lastIndexOf(')');
		return value.substring(init + 1, end);
	}
}
