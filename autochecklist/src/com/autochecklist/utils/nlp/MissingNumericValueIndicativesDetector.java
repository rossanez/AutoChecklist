package com.autochecklist.utils.nlp;

import java.util.HashSet;
import java.util.Set;

import com.autochecklist.utils.Utils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/* package */ class MissingNumericValueIndicativesDetector {

	private Set<String> mIndicatives;
	
	public MissingNumericValueIndicativesDetector() {
		mIndicatives = new HashSet<String>();

		mIndicatives.add("divide");
		mIndicatives.add("break");
		mIndicatives.add("break down");
		mIndicatives.add("carve");
		mIndicatives.add("cross");
		mIndicatives.add("cut");
		mIndicatives.add("isolate");
		mIndicatives.add("partition");
		mIndicatives.add("segregate");
		mIndicatives.add("split");
		mIndicatives.add("subdivide");
		mIndicatives.add("tear");
		mIndicatives.add("bisect");
		mIndicatives.add("branch");
		mIndicatives.add("chop");
		mIndicatives.add("cleave");
		mIndicatives.add("demarcate");
		mIndicatives.add("detach");
		mIndicatives.add("dichotomize");
		mIndicatives.add("disengage");
		mIndicatives.add("disentangle");
		mIndicatives.add("disjoin");
		mIndicatives.add("dislocate");
		mIndicatives.add("dismember");
		mIndicatives.add("dissect");
		mIndicatives.add("dissever");
		mIndicatives.add("dissociate");
		mIndicatives.add("dissolve");
		mIndicatives.add("disunite");
		mIndicatives.add("divorce");
		mIndicatives.add("halve");
		mIndicatives.add("intersect");
		mIndicatives.add("loose");
		mIndicatives.add("part");
		mIndicatives.add("quarter");
		mIndicatives.add("rend");
		mIndicatives.add("rupture");
//		mIndicatives.add("section"); // Too generic!
		mIndicatives.add("segment");
		mIndicatives.add("sever");
		mIndicatives.add("shear");
		mIndicatives.add("sunder");
		mIndicatives.add("unbind");
		mIndicatives.add("undo");
		mIndicatives.add("abscind");
		mIndicatives.add("cut up");
		mIndicatives.add("pull away");
		
		mIndicatives.add("add");
		mIndicatives.add("calculate");
		mIndicatives.add("cast");
		mIndicatives.add("compute");
		mIndicatives.add("count");
		mIndicatives.add("enumerate");
		mIndicatives.add("figure");
		mIndicatives.add("reckon");
		mIndicatives.add("sum");
		mIndicatives.add("summate");
		mIndicatives.add("tally");
		mIndicatives.add("tot");
		mIndicatives.add("total");
		mIndicatives.add("tote");
		mIndicatives.add("count up");
		mIndicatives.add("do addition");
		mIndicatives.add("reckon up");
		mIndicatives.add("tot up");

		mIndicatives.add("multiply");
		mIndicatives.add("accumulate");
		mIndicatives.add("augment");
		mIndicatives.add("boost");
		mIndicatives.add("breed");
		mIndicatives.add("build");
		mIndicatives.add("build up");
		mIndicatives.add("enlarge");
		mIndicatives.add("expand");
		mIndicatives.add("generate");
		mIndicatives.add("heighten");
		mIndicatives.add("magnify");
		mIndicatives.add("proliferate");
		mIndicatives.add("propagate");
		mIndicatives.add("spread");
		mIndicatives.add("add");
		mIndicatives.add("aggrandize");
		mIndicatives.add("aggregate");
		mIndicatives.add("compound");
		mIndicatives.add("cube");
		mIndicatives.add("double");
		mIndicatives.add("extend");
		mIndicatives.add("manifold");
		mIndicatives.add("mount");
		mIndicatives.add("populate");
		mIndicatives.add("procreate");
		mIndicatives.add("produce");
		mIndicatives.add("raise");
		mIndicatives.add("repeat");
		mIndicatives.add("rise");
		mIndicatives.add("square");

		mIndicatives.add("subtract");
		mIndicatives.add("deduct");
		mIndicatives.add("withhold");
		mIndicatives.add("decrease");
		mIndicatives.add("detract");
		mIndicatives.add("diminish");
		mIndicatives.add("discount");
		mIndicatives.add("remove");
		mIndicatives.add("take");
		mIndicatives.add("withdraw");
		mIndicatives.add("draw back");
		mIndicatives.add("knock off");
		mIndicatives.add("take from");
		mIndicatives.add("take off");
		mIndicatives.add("take out");
	}

	public Set<String> detect(String text) {
		Set<String> indicativesFound = new HashSet<String>();
		Annotation annotatedText = NLPTools.getInstance().annotate(text);

		for (CoreMap sentence : annotatedText.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				if (isIndicative(token.get(LemmaAnnotation.class).toLowerCase())) {
					indicativesFound.add(token.get(TextAnnotation.class));
				}
			}
		}

		return indicativesFound;
	}

	private boolean isIndicative(String text) {
		if (Utils.isTextEmpty(text)) return false;

		return mIndicatives.contains(text);
	}
}
