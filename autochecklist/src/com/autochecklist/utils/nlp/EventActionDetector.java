package com.autochecklist.utils.nlp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.autochecklist.utils.Utils;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class EventActionDetector {

	private LexicalizedParser mParser;
	private Set<String> mWeakActionIndicators;
	private static IDictionary mDictionary;

	public EventActionDetector() {
		mParser = CoreNLP.getInstance().getParser();
		initActionEventIndicators();
	}

	private void initActionEventIndicators() {
		mWeakActionIndicators = new HashSet<String>();
		mWeakActionIndicators.add("if");
		mWeakActionIndicators.add("else");
	}

	public Set<String> detect(String text) {
		Set<String> eventsFound = new HashSet<String>();
		Annotation document = CoreNLP.getInstance().annotate(text);

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			Tree parseTree = mParser.parse(sentence.toString());
			if (!sentenceContainsActions(parseTree)) continue;

			List<CoreLabel> verbalEventCandidates = new ArrayList<CoreLabel>();
			List<CoreLabel> nounEventCandidates = new ArrayList<CoreLabel>();
			List<CoreLabel> adjectiveEventCandidates = new ArrayList<CoreLabel>();

			boolean hasEventCandidates = false;
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String pos = token.get(PartOfSpeechAnnotation.class);

				if (pos.startsWith("VB")) {
					hasEventCandidates = true;
					if (!isWeakStativeVerb(token.get(LemmaAnnotation.class))) {
					    verbalEventCandidates.add(token);
					}
				} else if (pos.startsWith("NN")) {
				    hasEventCandidates = true;
				    nounEventCandidates.add(token);
				} else if (pos.startsWith("JJ")) {
					hasEventCandidates = true;
					adjectiveEventCandidates.add(token);
				}
			}

			if (hasEventCandidates) {
				eventsFound.addAll(handleEventCandidates(verbalEventCandidates, nounEventCandidates, adjectiveEventCandidates,
						mParser.parse(sentence.toString())));
			}

			//if (detectOnSentece(sentence.toString())) return true;
		}

		return eventsFound;
	}
	
	private Set<String> handleEventCandidates(List<CoreLabel> verbalEventCandidates,
			List<CoreLabel> nounEventCandidates, List<CoreLabel> adjectiveEventCandidates, Tree parseTree) {
		Set<String> ret = new HashSet<String>();
		ret.addAll(handleVerbalEventCandidates(verbalEventCandidates, parseTree));
		ret.addAll(handleNounEventCandidates(nounEventCandidates, parseTree));
		ret.addAll(handleAdjectiveEventCandidates(adjectiveEventCandidates, parseTree));
		return ret;
	}

	private Set<String> handleVerbalEventCandidates(List<CoreLabel> verbalEventCandidates, Tree parseTree) {
		Set<String> ret = new HashSet<String>();
		TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << VP=vp").matcher(parseTree);

		while (sentenceMatcher.matches()) {
			Tree verbalPhraseTree = sentenceMatcher.getNode("vp");
            for (Tree subTree : verbalPhraseTree) {
        		if (subTree.isPhrasal() && !"VP".equals(subTree.value())) {
        			break;
        		}
            	CoreLabel foundToken = getTokenFromTreeRoot(verbalEventCandidates, subTree);
            	if (foundToken != null) {
            		verbalEventCandidates.remove(foundToken);
            		ret.add(Sentence.listToString(verbalPhraseTree.yield()));
            		
            		if (verbalEventCandidates.isEmpty()) {
            			return ret;
            		}
            	}
            }
		}

		return ret;
	}

	private CoreLabel getTokenFromTreeRoot(List<CoreLabel> verbalEventCandidates, Tree verbalPhraseSubTree) {
		for (CoreLabel token : verbalEventCandidates) {
			if (token.get(PartOfSpeechAnnotation.class).equals(verbalPhraseSubTree.label().value()) &&
				token.get(TextAnnotation.class).equals(verbalPhraseSubTree.firstChild().value())) {
				return token;
			}
		}

		return null;
	}
	
	private boolean isWeakStativeVerb(String verb) {
		if ("be".equals(verb) || "have".equals(verb) || "contain".equals(verb)) {
			return true;
		}

		return false;
	}

	private Set<String> handleNounEventCandidates(List<CoreLabel> nounEventCandidates, Tree parseTree) {
		Set<String> ret = new HashSet<String>();
		// Use WordNet and check if the candidates are in the subtrees of "event" and "phenomenon".
		for (CoreLabel token : nounEventCandidates) {
			if (isEventOnWordNet(token.get(LemmaAnnotation.class))) {
				for (Tree subTree : parseTree) {
	        		if (subTree.isPhrasal() && !"NP".equals(subTree.value())) {
	        			break;
	        		}
	            	CoreLabel foundToken = getTokenFromTreeRoot(nounEventCandidates, subTree);
	            	if (foundToken != null) {
	            		nounEventCandidates.remove(foundToken);
	            	}
                    ret.add(Sentence.listToString(subTree.yield()));
            		
            		if (nounEventCandidates.isEmpty()) {
            			return ret;
            		}
				}
			}
		}

		return ret;
	}

	private boolean isEventOnWordNet(String word) {
		try {
			IDictionary dict = dicitionaryFactory();
			IIndexWord idw = dict.getIndexWord(word, POS.NOUN);
			if (idw != null) {
				IWordID wordID = idw.getWordIDs().get(0);
				IWord dictWord = dict.getWord(wordID);
				ISynset synset = dictWord.getSynset();
				List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

				List<IWord> words;
				for (ISynsetID sid : hypernyms) {
					words = dict.getSynset(sid).getWords();

					for (Iterator<IWord> i = words.iterator(); i.hasNext();) {
						String lemma = i.next().getLemma();
						if ("event".equals(lemma) ||
							"phenomenon".equals(lemma)) {
							return true;
						}
						
					}
				}
			}
		} catch (IOException e) {
			return false;
		}
		
		return false;
	}

	private static IDictionary dicitionaryFactory() throws IOException {
        if (mDictionary == null) {
           mDictionary = new Dictionary(Utils.getResourceAsURL("WordNet/dict")); 
           mDictionary.open();
        }

        return mDictionary;
    }

	private Set<String> handleAdjectiveEventCandidates(List<CoreLabel> adjectiveEventCandidates, Tree parseTree) {
		Set<String> ret = new HashSet<String>();

		return ret;
	}
	
	private boolean sentenceContainsActions(Tree parseTree) {
		TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << SBAR=sbar").matcher(parseTree);

        while (sentenceMatcher.find()) {
        	Tree sbarPhraseTree = sentenceMatcher.getNode("sbar");

            TregexMatcher sbarPhraseMatcher = TregexPattern.compile("SBAR << VB=vb").matcher(sbarPhraseTree);

            while (sbarPhraseMatcher.find()) {
            	List<Word> words = sbarPhraseMatcher.getNode("vb").yieldWords();
            	if ((words.size() == 1) && !isWeakVerbForAction(words.get(0).toString())) {
            		return true;
            	}
            }
        }

		return false;
	}

	private boolean isWeakVerbForAction(String word) {
		if (Utils.isTextEmpty(word)) return false;

		return mWeakActionIndicators.contains(word.toLowerCase());
	}
}
