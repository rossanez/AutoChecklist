package com.autochecklist.utils.nlp;

import java.util.Properties;
import java.util.Set;

import com.autochecklist.utils.Utils;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Singleton class providing access to Natural Language Processing methods and entities.
 * 
 * @author Anderson Rossanez
 */
public class NLPTools {

	private static NLPTools mPipelineInstance;
	
	private StanfordCoreNLP mPipeline;
	private LexicalizedParser mParser;
	private EventActionDetector mEventActionDetector;
	private MissingNumericValueIndicativesDetector mMissingNumericValueDetector;
	
	private NLPTools() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("ssplit.newlineIsSentenceBreak", "two_consecutive");

		mPipeline = new StanfordCoreNLP(props);

		// Keeping the parser separated from the pipeline so that parsing
		// does not gets influenced by the PoS tagging results.
		mParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

		mEventActionDetector = new EventActionDetector(mParser);
		mMissingNumericValueDetector = new MissingNumericValueIndicativesDetector();
	}

	public static NLPTools getInstance() {
		if (mPipelineInstance == null) {
			Utils.println("Initializing NLP tools...");
			mPipelineInstance = new NLPTools();
			Utils.print("done!");
		}

		return mPipelineInstance;
	}

	public static IRequirementsInfoOutBuildable extractPreprocInfoAndWrite(String text, IRequirementsInfoOutBuildable outputBuilder) {
		new DocumentSectionsExtractor(text, outputBuilder).extract();
        new RequirementsInfoExtractor(text, outputBuilder).extract();

        return outputBuilder;
	}

	public static IExpressionExtractable createExpressionExtractor(String extractorRulesResource) {
		return new ExpressionExtractor(extractorRulesResource);
	}

	public static IExpressionExtractable createExpressionExtractor(String extractorRulesMainResorce, String... composingResources) {
		return new ExpressionExtractor(extractorRulesMainResorce, composingResources);
	}

	public LexicalizedParser getParser() {
		return mParser;
	}

	
	public Set<String> getActions(String text) {
		return mEventActionDetector.getActionsInText(text);
	}

	public Set<String> checkIfHasActionsAndGetEvents(String text) {
		return mEventActionDetector.checkIfHasActionsAndGetEvents(text);
	}

	public Set<String> getMissingNumericValueIndicators(String text) {
		return mMissingNumericValueDetector.detect(text);
	}

	public Annotation annotate(String text) {
		Annotation document = new Annotation(text);
		mPipeline.annotate(document);

		return document;
	}
}
