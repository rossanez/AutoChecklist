package com.autochecklist.modules.preprocess;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.IRequirementsInfoOutBuildable;

public class XMLOutPreProcBuilder implements IRequirementsInfoOutBuildable {

	private DocumentBuilder mDocBuilder;
	private Document mDoc;
	private Element mRootElement;
	private Element mRequirementsElement;
	private Element mSectionsElement;
	private Element mRTMElement;
	private Element mCurrentRequirement;
	
	public XMLOutPreProcBuilder() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		try {
		    mDocBuilder = docFactory.newDocumentBuilder();
	    } catch (ParserConfigurationException e) {
	    	Utils.printError("Unable to create XML file! - " + e.getMessage());
		    throw new RuntimeException("Unable to create XML file! - " + e.getMessage());
	    }

		mDoc = mDocBuilder.newDocument();
		mRootElement = mDoc.createElement("SRS");
		mDoc.appendChild(mRootElement);

		mRequirementsElement = mDoc.createElement("requirements");
		mRootElement.appendChild(mRequirementsElement);

		mSectionsElement = mDoc.createElement("sections");
		mRootElement.appendChild(mSectionsElement);

		mRTMElement = mDoc.createElement("RTM");
		mRootElement.appendChild(mRTMElement);
	}

	public void createNewRequirement(String reqId) {
		Element requirement = mDoc.createElement("requirement");
		Element requirementId = mDoc.createElement("id");
		requirementId.appendChild(mDoc.createTextNode(reqId));
		requirement.appendChild(requirementId);
		mRequirementsElement.appendChild(requirement);
		mCurrentRequirement = requirement;
	}
	
	public void addRequirementText(String req) {
		if (mCurrentRequirement == null) {
			Utils.printError("(add) Wrong XML tag sequence!");
			throw new RuntimeException("(add) Wrong XML tag sequence!");
		}

		Element requirementText = mDoc.createElement("text");
		requirementText.appendChild(mDoc.createTextNode(req));
		mCurrentRequirement.appendChild(requirementText);
	}

	public void appendRequirementText(String req) {
		if (mCurrentRequirement == null) {
			Utils.printError("(append) Wrong XML tag sequence!");
			throw new RuntimeException("(append) Wrong XML tag sequence!");
		}

		Node oldChild = mCurrentRequirement.getLastChild();
		String currReq = oldChild.getFirstChild().getNodeValue();
		currReq = currReq + " " + req;
		mCurrentRequirement.removeChild(oldChild);
		Element requirementText = mDoc.createElement("text");
		requirementText.appendChild(mDoc.createTextNode(currReq));
		mCurrentRequirement.appendChild(requirementText);
	}

	public void addDocumentSection(String id, String name) {
		Element section = mDoc.createElement("section");
		
		Element sectionId = mDoc.createElement("id");
		sectionId.appendChild(mDoc.createTextNode(id));
		section.appendChild(sectionId);

		Element sectionName = mDoc.createElement("name");
		sectionName.appendChild(mDoc.createTextNode(name));
		section.appendChild(sectionName);

		mSectionsElement.appendChild(section);
	}

	public void addRTMContents(String contents) {
		if (Utils.isTextEmpty(contents)) return;

		mRTMElement.appendChild(mDoc.createTextNode(contents));
	}
	
	public File generateOutputFile(String fileName) {
		File resultFile = new File(fileName);
        if (resultFile.exists() & !resultFile.isDirectory()) {
        	resultFile.delete();
        }
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(mDoc);
			StreamResult result = new StreamResult(resultFile);

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		} catch (TransformerException e) {
			Utils.printError("Unable to generate XML file! - " + e.getMessage());
			throw new RuntimeException("Unable to generate XML file! - " + e.getMessage());
		}

		return resultFile;
	}
}
