package org.evosuite.coverage.epa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.testcase.execution.EvosuiteError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Creates a new EPA from an XML document
 * 
 * @author galeotti
 *
 */
public abstract class EPAFactory {

	private static final String DESTINATION = "destination";
	private static final String LABEL = "label";
	private static final String TRANSITION = "transition";
	private static final String STATE = "state";
	private static final String NAME = "name";
	private static final String INITIAL_STATE = "initial_state";
	
	public static EPA buildEPAOrError(String xmlFilename) {
		try {
			return buildEPA(xmlFilename);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new EvosuiteError(e);
		}
	}
	public static EPA buildEPA(String xmlFilename) throws ParserConfigurationException, SAXException, IOException {
		return buildEPA(new FileInputStream(xmlFilename));
	}
	
	
	/**
	 * Builds a new EPA from an XML file
	 * 
	 * @param xml
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static EPA buildEPA(InputStream xml) throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document document = documentBuilder.parse(xml);
		final Element abstraction = document.getDocumentElement();
		final String initialStateName = abstraction.getAttribute(INITIAL_STATE);

		final String epaName = abstraction.getAttribute(NAME);
		final NodeList states = abstraction.getElementsByTagName(STATE);

		// Populate a map of names to states
		final Map<String, EPAState> epaStateMap = new HashMap<>();
		for (int i = 0; i < states.getLength(); i++) {
			final Element state = (Element) states.item(i);
			final String name = state.getAttribute(NAME);
			epaStateMap.put(name, new EPAState(name));
		}

		// Populate list of transitions
		final List<EPATransition> epaTransitions = new ArrayList<>();
		for (int i = 0; i < states.getLength(); i++) {
			final Element state = (Element) states.item(i);
			final String stateId = state.getAttribute(NAME);

			final NodeList stateChilds = states.item(i).getChildNodes();
			for (int j = 0; j < stateChilds.getLength(); j++) {
				final Node stateChild = stateChilds.item(j);
				if (Objects.equals(stateChild.getNodeName(), TRANSITION)) {
					final Element transition = (Element) stateChild;
					final String actionId = transition.getAttribute(LABEL);
					final String destinationStateId = transition.getAttribute(DESTINATION);
					EPAState originState = epaStateMap.get(stateId);
					EPAState destinationState = epaStateMap.get(destinationStateId);
					epaTransitions.add(new EPANormalTransition(originState, actionId,
							destinationState));
				}
			}
		}

		// Build map
		final Map<EPAState, Set<EPATransition>> map = new HashMap<>();
		epaStateMap.values().stream().forEach(epaState -> {
			final Set<EPATransition> epaStateTransitions = epaTransitions.stream()
					.filter(epaTransition -> epaTransition.getOriginState() == epaState).collect(Collectors.toSet());
			map.put(epaState, epaStateTransitions);
		});
		EPAState initialState = epaStateMap.get(initialStateName);

		EPA newEPA = new EPA(epaName, map, initialState);
		return newEPA;
	}

}
