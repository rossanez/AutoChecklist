package com.autochecklist.model.requirements;

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

public class RequirementList {

	private List<Requirement> mRequirements;

	public RequirementList() {
		mRequirements = new ArrayList<Requirement>();
	}
	
	public RequirementList(String fileName) {
		mRequirements = new ArrayList<Requirement>();
		obtainRequirements(fileName);
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
			throw new RuntimeException("Unable to parse pre-processed file! - " + e.getMessage());
		}
	}
	
	private void obtainRequirements_internal(String fileName) throws SAXException, IOException, ParserConfigurationException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("SRS")) {
			throw new RuntimeException("Inconsistent pre-processed file!");
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
