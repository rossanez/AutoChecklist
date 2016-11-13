package com.autochecklist.base.requirements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.autochecklist.utils.Utils;

public class RequirementList {

	private List<Requirement> mRequirements;

	public RequirementList() {
		mRequirements = new ArrayList<Requirement>();
	}
	
	public RequirementList(String preprocFileName) {
		mRequirements = new ArrayList<Requirement>();
		obtainRequirements(preprocFileName);
	}

	public List<Requirement> getRequirements() {
		return mRequirements;
	}

	public void addRequirements(List<Requirement> reqs) {
		mRequirements.addAll(reqs);
	}

	private void obtainRequirements(String fileName) {
		try {
			obtainRequirements_internal(fileName);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			Utils.printError("Unable to parse the preprocessed file for obtaining requirements! - " + e.getMessage());
			throw new RuntimeException("Unable to parse preprocessed file for obtaining requirements! - " + e.getMessage());
		}
	}
	
	private void obtainRequirements_internal(String fileName) throws SAXException, IOException, ParserConfigurationException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("SRS")) {
			Utils.printError("Inconsistence on preprocessed file!");
			throw new RuntimeException("Inconsistence on preprocessed file!");
		}

		NodeList nRequirements = doc.getElementsByTagName("requirement");
		for (int i = 0; i < nRequirements.getLength(); i++) {
			Node nRequirement = nRequirements.item(i);
			if (nRequirement.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nRequirement;
				String id = eElement.getElementsByTagName("id")
						.item(0).getTextContent();
				String text = eElement.getElementsByTagName("text")
						.item(0).getTextContent();
				mRequirements.add(new Requirement(id, text));
			}
		}
	}
}
