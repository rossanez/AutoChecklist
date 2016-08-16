package com.autochecklist.utils.nlp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.ling.tokensregex.parser.ParseException;
import edu.stanford.nlp.ling.tokensregex.parser.TokenSequenceParseException;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ExpressionExtractor {

	private CoreMapExpressionExtractor<MatchedExpression> mExpressionExtractor;

	@SuppressWarnings("unchecked")
	public ExpressionExtractor(String extractorRulesResource) {
		String strContent = Utils.getResourceAsString(extractorRulesResource);
		try {
			mExpressionExtractor = CoreMapExpressionExtractor.createExtractorFromString(TokenSequencePattern.getNewEnv(), strContent);
		} catch (IOException | ParseException | TokenSequenceParseException e) {
			Utils.printError("Error when creating expression extractor!");
			throw new RuntimeException("Unable to create extractor from the passed rules! - " + e.getMessage());
		}
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
