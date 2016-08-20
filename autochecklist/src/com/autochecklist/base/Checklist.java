package com.autochecklist.base;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.utils.Utils;

public class Checklist {

	public void parseChecklistResource(String resourceName, QuestionCategory...categories) {
		try {
			parseChecklistResource_internal(resourceName, categories);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Utils.printError("Unable to parse checklist file! - " + e.getMessage());
			throw new RuntimeException("Unable to parse checklist file! - " + e.getMessage());
		}
	}
	
	private void parseChecklistResource_internal(String resourceName, QuestionCategory... categories) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(Utils.getResourceAsInputStream(resourceName));

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("checklist")) {
			Utils.printError("Inconsistence on checklist file!");
			throw new RuntimeException("Inconsistence on checklist file!");
		}

		NodeList nCategories = doc.getElementsByTagName("category");
		for (QuestionCategory cat : categories) {
			for (int i = 0; i < nCategories.getLength(); i++) {
				Element nCategory = (Element) nCategories.item(i);
				if (cat.getCategoryName().equals(nCategory.getAttribute("type"))) {
					NodeList nQuestions = (NodeList) nCategory.getChildNodes();
					for (int j = 0; j < nQuestions.getLength(); j++) {
						Node nQuestion = nQuestions.item(j);
						if (nQuestion.getNodeType() == Node.ELEMENT_NODE) {
						    Element eElement = (Element) nQuestion;
						    int id = Integer.parseInt(eElement.getAttribute("id"));
						    QuestionAction action = extractAction(eElement.getAttribute("action"));
						    Question question = new Question(id, eElement.getTextContent());
						    if (action != null) {
						    	question.setAction(action);
						    }
						    cat.addQuestion(question);
						}
					}
				}
			}
		}
	}

	private QuestionAction extractAction(String action) {
        if (Utils.isTextEmpty(action)) {
        	return null;
        }

        String[] params = action.split(",");
        int act = resolveAction(params[0]);
        if (act < 0) return null;
        
        String value = null;
        if (!Utils.isTextEmpty(params[1])) {
        	value = params[1].trim();
        	value = value.replace(' ', '_');
        }

        if (act == QuestionAction.CHECK_NUMBER_AND_UNIT) {
        	return new QuestionAction(act, "NUMBER_AND_UNIT", value);
        }
        return new QuestionAction(act, value);
	}

	private int resolveAction(String action) {
		if ("extract".equals(action)) return QuestionAction.EXTRACT_TERM_OR_EXPRESSION;
		else if ("numeric".equals(action)) return QuestionAction.CHECK_NUMBER_AND_UNIT;

		return -1;
	}
}