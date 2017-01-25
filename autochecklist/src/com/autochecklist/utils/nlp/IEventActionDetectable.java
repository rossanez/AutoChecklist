package com.autochecklist.utils.nlp;

import java.util.Set;

public interface IEventActionDetectable {

	Set<String> getActions(String text);

	Set<String> checkIfHasActionsAndGetEvents(String text);
}
