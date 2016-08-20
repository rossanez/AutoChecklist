package com.autochecklist.utils.nlp;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ExpressionExtractor {

	private CoreMapExpressionExtractor<MatchedExpression> mExpressionExtractor;

	public ExpressionExtractor(String extractorRulesResource) {
		mExpressionExtractor = CoreNLP.createExpressionExtractor(Utils.getResourceAsString(extractorRulesResource));
	}

	public ExpressionExtractor(String extractorRulesMainResorce, String... composingResources) {
		mExpressionExtractor = CoreNLP.createExpressionExtractor(
				Utils.getCompositeResourceAsString(extractorRulesMainResorce, composingResources));
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

				// This is a workaround for dealing with nested tokens.
				if (hasPotentialNumbersAndUnits(matchType)) {
					int initPos = text.indexOf(value);
					String newText = text.substring(initPos);
					
					int initNumberPos = value.lastIndexOf(' ');
					newText = newText.substring(initNumberPos).trim();
					
					List<Pair<String, String>> innerRetList = extract(newText);
					if (!innerRetList.isEmpty()) {
						retList.addAll(innerRetList);
					}
					
					return retList;
				}
			}
		}

		return retList;
	}

	private String formatMatchValue(String value) {
		int init = value.lastIndexOf('(');
		int end = value.lastIndexOf(')');
		return value.substring(init + 1, end);
	}

	private boolean hasPotentialNumbersAndUnits(String matchType) {
		if ("IN_MORE_THAN".equals(matchType)
			|| "IN_LESS_THAN".equals(matchType)
			|| "IN_SOME".equals(matchType)
			|| "A_FEW".equals(matchType)) {
			return true;
		}

		return false;
	}
}
