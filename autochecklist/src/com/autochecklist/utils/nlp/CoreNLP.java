package com.autochecklist.utils.nlp;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.ling.tokensregex.parser.ParseException;
import edu.stanford.nlp.ling.tokensregex.parser.TokenSequenceParseException;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Singleton class providing access to methods from Stanford's CoreNLP pipeline.
 * 
 * @author Anderson Rossanez
 */
public class CoreNLP {

	private static CoreNLP mPipelineInstance;
	
	private StanfordCoreNLP mPipeline;
	private LexicalizedParser mParser; 
	
	private CoreNLP() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("ssplit.newlineIsSentenceBreak", "two_consecutive");

		mPipeline = new StanfordCoreNLP(props);

		// Keeping the parser separated from the pipeline so that parsing
		// does not gets influenced by the PoS tagging results.
		mParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	}

	public static CoreNLP getInstance() {
		if (mPipelineInstance == null) {
			Utils.println("Initializing NLP tools...");
			mPipelineInstance = new CoreNLP();
			Utils.print("done!");
		}

		return mPipelineInstance;
	}

	public LexicalizedParser getParser() {
		return mParser;
	}

	public Annotation annotate(String text) {
		Annotation document = new Annotation(text);
		mPipeline.annotate(document);

		return document;
	}

	@SuppressWarnings("unchecked")
	public static CoreMapExpressionExtractor<MatchedExpression> createExpressionExtractor(String extractionRules) {
		try {
			Env env = TokenSequencePattern.getNewEnv();
			env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE);

			return CoreMapExpressionExtractor.createExtractorFromString(env, extractionRules);
		} catch (IOException | ParseException | TokenSequenceParseException e) {
			Utils.printError("Error when creating expression extractor!");
			throw new RuntimeException("Unable to create extractor from the passed rules! - " + e.getMessage());
		}
	}
}
