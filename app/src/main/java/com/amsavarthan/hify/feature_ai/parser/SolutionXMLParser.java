package com.amsavarthan.hify.feature_ai.parser;


import com.amsavarthan.hify.feature_ai.models.Solution;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SolutionXMLParser {

    public static ArrayList<Solution> parseResultXml(String resultXml, String query) {

        ArrayList<Solution> solutions = new ArrayList<>();

        try {
            Document document = loadXMLFromString(resultXml);
            document.getDocumentElement().normalize();

            Element rootElement = (Element) (document.getElementsByTagName("queryresult")).item(0);

            NodeList pods = rootElement.getElementsByTagName("pod");

            for (int count = 0; count < pods.getLength(); count++) {

                Solution solution = new Solution();

                Node pod = pods.item(count);
                int numsubpods=Integer.parseInt(((Element)pod).getAttribute("numsubpods"));

                // for(int i=0;i<numsubpods;i++){

                    //Element subPodElement = (Element) ((Element) pod).getElementsByTagName("subpod").item(i);
                    Element subPodElement = (Element) ((Element) pod).getElementsByTagName("subpod").item(0);

                    Element imgElement = (Element) (subPodElement.getElementsByTagName("img").item(0));

                    try {
                        Element statesElement = (Element) ((Element) pod).getElementsByTagName("states").item(0);

                        int state_count=Integer.parseInt(statesElement.getAttribute("count"));
                        solution.setState_count(state_count);
                        solution.state_name=new ArrayList<>();
                        solution.state_input=new ArrayList<>();

                        for (int j = 0; j < state_count; j++) {
                            Element stateElement = (Element) (statesElement.getElementsByTagName("state").item(j));
                            solution.state_name.add(stateElement.getAttribute("name"));
                            solution.state_input.add(stateElement.getAttribute("input"));
                        }

                    }catch (Exception e){e.printStackTrace();}

                    solution.setDefaultCard(false);
                    solution.setStateResult(false);
                    solution.setInput(query);
                    solution.setTitle(((Element) pod).getAttribute("title"));
                    solution.setSrc(imgElement.getAttribute("src"));
                    solution.setDescription(imgElement.getAttribute("title"));
                    solution.setHeight(Integer.parseInt(imgElement.getAttribute("height")));
                    solution.setWidth(Integer.parseInt(imgElement.getAttribute("width")));

                    if (StringUtils.isNotEmpty(solution.getSrc())){
                        solutions.add(solution);
                    }

                //}

            }

            if (solutions.size() > 0)
                return solutions;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource source = new InputSource(new StringReader(xml));
        return builder.parse(source);
    }

}
