package com.autochecklist.utils.nlp;

import java.io.IOException;
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

/* package */ class EventActionDetector {

	// Maximum number of characters to represent an event in the final output.
	private static final int EVENT_TEXT_MAX_SIZE = 35;

	private LexicalizedParser mParser;

	private Set<String> mWeakActionVerbs;
	private Set<String> mWeakStativeVerbs;
	private Set<String> mAdjectiveEvents;

	private static IDictionary sDictionary; // Keeping it as static to avoid loading more than once.

	public EventActionDetector(LexicalizedParser parser) {
		mParser = parser;
		initActionEventIndicators();
	}

	private void initActionEventIndicators() {
		mWeakActionVerbs = new HashSet<String>();
		mWeakActionVerbs.add("be");
		mWeakActionVerbs.add("have");

		mWeakStativeVerbs = new HashSet<String>();
		mWeakStativeVerbs.add("be");
		mWeakStativeVerbs.add("have");
		mWeakStativeVerbs.add("contain");
		mWeakStativeVerbs.add("define");

		mAdjectiveEvents = new HashSet<String>();
		mAdjectiveEvents.add("amateur");
		mAdjectiveEvents.add("attempted");
		mAdjectiveEvents.add("commemorative");
		mAdjectiveEvents.add("desperate");
		mAdjectiveEvents.add("face-saving");
		mAdjectiveEvents.add("hostile");
		mAdjectiveEvents.add("lame");
		mAdjectiveEvents.add("limp");
		mAdjectiveEvents.add("maiden");
		mAdjectiveEvents.add("negative");
		mAdjectiveEvents.add("official");
		mAdjectiveEvents.add("on-the-job");
		mAdjectiveEvents.add("paid");
		mAdjectiveEvents.add("parting");
		mAdjectiveEvents.add("part-time");
		mAdjectiveEvents.add("personal");
		mAdjectiveEvents.add("physical");
		mAdjectiveEvents.add("positive");
		mAdjectiveEvents.add("preventative");
		mAdjectiveEvents.add("preventive");
		mAdjectiveEvents.add("procedural");
		mAdjectiveEvents.add("reciprocal");
		mAdjectiveEvents.add("recreational");
		mAdjectiveEvents.add("saltwater");
		mAdjectiveEvents.add("token");
		mAdjectiveEvents.add("two-handed");
		mAdjectiveEvents.add("two-step");
		mAdjectiveEvents.add("unprompted");
	}

	public Set<String> checkIfHasActionsAndGetEvents(String text) {
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
					if (!isWeakStativeVerb(token.get(LemmaAnnotation.class))) {
						hasEventCandidates = true;
					    verbalEventCandidates.add(token);
					}
				} else if (pos.startsWith("NN")) {
				    hasEventCandidates = true;
				    nounEventCandidates.add(token);
				} else if (pos.startsWith("JJ")) {
					if (isAdjectiveEventCandidate(token.get(TextAnnotation.class))) {
					    hasEventCandidates = true;
					    adjectiveEventCandidates.add(token);
					}
				}
			}

			if (hasEventCandidates) {
				eventsFound.addAll(handleEventCandidates(verbalEventCandidates, nounEventCandidates, adjectiveEventCandidates, parseTree));
			}
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
            		ret.add(formatEventText(Sentence.listToString(verbalPhraseTree.yield())));
            		
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
                    ret.add(formatEventText(Sentence.listToString(subTree.yield())));
            		
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
        if (sDictionary == null) {
           sDictionary = new Dictionary(Utils.getResourceAsURL("WordNet/dict/")); 
           sDictionary.open();
        }

        return sDictionary;
    }

	private Set<String> handleAdjectiveEventCandidates(List<CoreLabel> adjectiveEventCandidates, Tree parseTree) {
		Set<String> ret = new HashSet<String>();
		for (CoreLabel token : adjectiveEventCandidates) {
			for (Tree subTree : parseTree) {
				if (token.get(PartOfSpeechAnnotation.class).equals(subTree.label().value())
					&& (token.get(TextAnnotation.class).equals(subTree.firstChild().value()))) {
					ret.add(formatEventText(Sentence.listToString(subTree.parent().yield())));
				}
			}
		}

		return ret;
	}
	
	private boolean sentenceContainsActions(Tree parseTree) {
		TregexMatcher sentenceMatcher = TregexPattern.compile("ROOT << S=s").matcher(parseTree);

        while (sentenceMatcher.find()) {
        	Tree sentencePhraseTree = sentenceMatcher.getNode("s");

            TregexMatcher infinitiveVerbPhraseMatcher = TregexPattern.compile("S << VB=vb").matcher(sentencePhraseTree);

            while (infinitiveVerbPhraseMatcher.find()) {
            	List<Word> verbs = infinitiveVerbPhraseMatcher.getNode("vb").yieldWords();
            	if ((verbs.size() == 1) && !isWeakVerbForAction(verbs.get(0).toString())) {
            		return true;
            	}
            }

            TregexMatcher presentVerb3rdPersonPhraseMatcher = TregexPattern.compile("S << VBZ=vbz").matcher(sentencePhraseTree);
            
            while (presentVerb3rdPersonPhraseMatcher.find()) {
                List<Word> verbs = presentVerb3rdPersonPhraseMatcher.getNode("vbz").yieldWords();
                if ((verbs.size() == 1) && !isWeakVerbForAction(verbs.get(0).toString())) {
            		return true;
            	}
            }

            TregexMatcher presentVerbNon3rdPersonPhraseMatcher = TregexPattern.compile("S << VBP=vbp").matcher(sentencePhraseTree);
            
            while (presentVerbNon3rdPersonPhraseMatcher.find()) {
                List<Word> verbs = presentVerbNon3rdPersonPhraseMatcher.getNode("vbp").yieldWords();
                if ((verbs.size() == 1) && !isWeakVerbForAction(verbs.get(0).toString())) {
            		return true;
            	}
            }
        }

		return false;
	}

	private boolean isWeakVerbForAction(String word) {
		if (Utils.isTextEmpty(word)) return false;

		return mWeakActionVerbs.contains(word.toLowerCase());
	}

	private boolean isWeakStativeVerb(String verb) {
		if (Utils.isTextEmpty(verb)) return false;

		return mWeakStativeVerbs.contains(verb.toLowerCase());
	}

	private boolean isAdjectiveEventCandidate(String adjective) {
		if (Utils.isTextEmpty(adjective)) return false;
		
        return mAdjectiveEvents.contains(adjective.toLowerCase());
	}

	private String formatEventText(String eventText) {
		if (Utils.isTextEmpty(eventText)) {
			return eventText;
		}

		return (eventText.length() < EVENT_TEXT_MAX_SIZE) ?
				eventText : eventText.substring(0, EVENT_TEXT_MAX_SIZE) + "...";
	}
}
