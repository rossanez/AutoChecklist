package com.autochecklist.utils.nlp;

import java.io.IOException;
import java.util.Properties;

import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.interfaces.IEventActionDetectable;
import com.autochecklist.utils.nlp.interfaces.IExpressionExtractable;
import com.autochecklist.utils.nlp.interfaces.IMissingNumericValuesIndicativeDetectable;
import com.autochecklist.utils.nlp.interfaces.IRequirementsInfoOutBuildable;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
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

	private IDictionary mWordNetDictionary;

	private StanfordCoreNLP mPipeline;
	private LexicalizedParser mParser;
	
	private NLPTools() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.put("ssplit.newlineIsSentenceBreak", "two");

		mPipeline = new StanfordCoreNLP(props);

		// Keeping the parser separated from the pipeline so that parsing
		// does not gets influenced by the PoS tagging results.
		mParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
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

	public IEventActionDetectable getEventActionDetector() {
		return new EventActionDetector(getParser());
	}

	public IMissingNumericValuesIndicativeDetectable getMissingNumericValueIndicativesDetector() {
		return new MissingNumericValueIndicativesDetector();
	}

	/* package */ Annotation annotate(String text) {
		Annotation document = new Annotation(text);
		mPipeline.annotate(document);

		return document;
	}

	/* package */ LexicalizedParser getParser() {
		return mParser;
	}

	/* package */ IDictionary getWordNetDictionary() throws IOException {
	    if (mWordNetDictionary == null) {
	       mWordNetDictionary = new Dictionary(Utils.getResourceAsURL("WordNet/dict/")); 
	       mWordNetDictionary.open();
	    }
	
	    return mWordNetDictionary;
	}

	/* package */ boolean isOnWordNet(String word) {
		POS[] partsOfSpeech = {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
        try {
			IDictionary dict = getWordNetDictionary();
			for (POS pos : partsOfSpeech) {
				if (dict.getIndexWord(word, pos) != null) {
					return true;
				}
			}
		} catch (IOException e) {
			Utils.printError(e.getMessage());
		}

        return false;
	}
}
