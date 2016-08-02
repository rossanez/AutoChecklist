package com.autochecklist.base;

import java.io.File;
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

	public void parseChecklistFile(String fileName, QuestionCategory...categories) {
		try {
			parseChecklistFile_internal(fileName, categories);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException("Unable to parse checklist file! - " + e.getMessage());
		}
	}
	
	private void parseChecklistFile_internal(String fileName, QuestionCategory... categories) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("checklist")) {
			throw new RuntimeException("Inconsistent checklist file!");
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
        	if (act == QuestionAction.CHECK_NUMBER_AND_UNIT) {
        		value = "NUMBER_AND_UNIT";
        	} else {
        	    value = params[1].trim().toUpperCase();
        	}
        	value = value.replace(' ', '_');
        }

        return new QuestionAction(act, value);
	}

	private int resolveAction(String action) {
		if ("extract".equals(action)) return QuestionAction.EXTRACT_TERM_OR_EXPRESSION;
		else if ("numeric".equals(action)) return QuestionAction.CHECK_NUMBER_AND_UNIT;

		return -1;
	}
}