/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *//*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.river.wikipedia.support;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For internal use only -- Used by the {@link WikiPage} class.
 * Can also be used as a stand alone class to parse wiki formatted text.
 *
 * @author Delip Rao
 */
public class WikiTextParser {

    private String wikiText = null;
    private ArrayList<String> pageCats = null;
    private ArrayList<String> pageLinks = null;
    private float [] pageCoords = null;
    private boolean redirect = false;
    private String redirectString = null;
    private static Pattern redirectPattern =
            Pattern.compile("#REDIRECT\\s+\\[\\[(.*?)\\]\\]", Pattern.CASE_INSENSITIVE);
    private boolean stub = false;
    private boolean disambiguation = false;
    private static Pattern stubPattern = Pattern.compile("\\-stub\\}\\}");
    // the first letter of pages is case-insensitive
    private static Pattern disambCatPattern =
            Pattern.compile("\\{\\{[Dd]isambig(uation)?\\}\\}");
    
    private InfoBox infoBox = null;

    public WikiTextParser(String wtext) {
        wikiText = wtext;
        Matcher matcher = redirectPattern.matcher(wikiText);
        if (matcher.find()) {
            redirect = true;
            if (matcher.groupCount() == 1)
                redirectString = matcher.group(1);
        }
        matcher = stubPattern.matcher(wikiText);
        stub = matcher.find();
        matcher = disambCatPattern.matcher(wikiText);
        disambiguation = matcher.find();
    }

    public boolean isRedirect() {
        return redirect;
    }

    public boolean isStub() {
        return stub;
    }

    public String getRedirectText() {
        return redirectString;
    }

    public String getText() {
        return wikiText;
    }

    public ArrayList<String> getCategories() {
        if (pageCats == null) parseCategories();
        return pageCats;
    }
    
    public float [] getCoords(){
    	if(pageCoords == null) parseCoords();
    	return pageCoords;
    }

    public ArrayList<String> getLinks() {
        if (pageLinks == null) parseLinks();
        return pageLinks;
    }

    private void parseCategories() {
        pageCats = new ArrayList<String>();
        Pattern catPattern = Pattern.compile("\\[\\[[Cc]ategory:(.*?)\\]\\]", Pattern.MULTILINE);
        Matcher matcher = catPattern.matcher(wikiText);
        while (matcher.find()) {
            String[] temp = matcher.group(1).split("\\|");
            pageCats.add(temp[0]);
        }
    }
    
    private void parseCoords() {
        pageCoords = new float[2];
    	Pattern coordPattern = Pattern.compile("\\{\\{Coord.*display=.*title.*\\}\\}", Pattern.CASE_INSENSITIVE);
    	Pattern coordPattern2 = Pattern.compile("");
        Matcher matcher = coordPattern.matcher(wikiText);
        if (matcher.find()) {
        	String[] temp = matcher.group(0).split("\\|");
        	
        	ArrayList<String> lat = new ArrayList<String>();
        	ArrayList<String> lon = new ArrayList<String>();
        	lat.add(temp[1]);
        	int i = 2;
        	//Starts at position 2 since the first 2 index positions are always numbers, reads until it encounters N or S
        	while(!temp[i].equalsIgnoreCase("N") && !temp[i].equalsIgnoreCase("S") && i < temp.length-2){
        		lat.add(temp[i]);
        		i++;
        	}
        	//On N, positive number
        	if(temp[i].equalsIgnoreCase("N")){
        		lat.add("1");
        		i++;
        	}
        	//On S, negative number
        	else if(temp[i].equalsIgnoreCase("S")){
        		lat.add("-1");
        		i++;
        	}
        	//read second array until E or W
        	while(!temp[i].equalsIgnoreCase("E") && !temp[i].equalsIgnoreCase("W") && i < temp.length-2){
        		lon.add(temp[i]);
        		i++;
        	}
        	//positive for E
        	if(temp[i].equalsIgnoreCase("E")){
        		lon.add("1");
        		i++;
             }
        	//negative for W
        	else if (temp[i].equalsIgnoreCase("W")){
        		lon.add("-1");
        		i++;
        	}
        	//if it's just two numbers in the array, lat/lon
        	if(lat.size() == 2 && lon.isEmpty()) {
                pageCoords[0]= Float.parseFloat(lat.get(0));
                pageCoords[1]= Float.parseFloat(lat.get(1));
        	}
        	//conversion for decimal degrees
        	else {
        		float lat_coord = Float.parseFloat(lat.get(0));
        		float lon_coord = Float.parseFloat(lon.get(0));
        	
        			for(int j = 1; j < lat.size()-1; j++){
        				lat_coord = (float) (lat_coord + Float.parseFloat((lat.get(j)))/(Math.pow(60.0,j)));
        			}
        			
        			for(int k = 1; k < lon.size()-1; k++) {
        				lon_coord = (float) (lon_coord + Float.parseFloat((lon.get(k)))/(Math.pow(60.0,k)));
        			}
        			
        	
        	pageCoords[0] = (lat_coord * Float.parseFloat(lat.get(lat.size()-1)));
        	pageCoords[1] = (lon_coord * Float.parseFloat(lon.get(lon.size()-1)));
        	}
        }
        
        //current placeholder if there are no geolocated coordinates, make them equal to these, they are ignored on the builder
        
        
        
        
        else{
        	pageCoords[0] = 8675309;
        	pageCoords[1] = 911;
        }
    }
    
   
    
   
    

    private void parseLinks() {
        pageLinks = new ArrayList<String>();

        Pattern catPattern = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE);
        Matcher matcher = catPattern.matcher(wikiText);
        while (matcher.find()) {
            String[] temp = matcher.group(1).split("\\|");
            if (temp == null || temp.length == 0) continue;
            String link = temp[0];
            if (link.contains(":") == false) {
                pageLinks.add(link);
            }
        }
    }

    public String getPlainText() {
        String text = wikiText.replaceAll("&gt;", ">");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("<ref>.*?</ref>", " ");
        text = text.replaceAll("</?.*?>", " ");
        text = text.replaceAll("\\{\\{.*?\\}\\}", " ");
        text = text.replaceAll("\\[\\[.*?:.*?\\]\\]", " ");
        text = text.replaceAll("\\[\\[(.*?)\\]\\]", "$1");
        text = text.replaceAll("\\s(.*?)\\|(\\w+\\s)", " $2");
        text = text.replaceAll("\\[.*?\\]", " ");
        text = text.replaceAll("\\'+", "");
        return text;
    }

    public InfoBox getInfoBox() {
        //parseInfoBox is expensive. Doing it only once like other parse* methods
        if (infoBox == null)
            infoBox = parseInfoBox();
        return infoBox;
    }

    private InfoBox parseInfoBox() {
        String INFOBOX_CONST_STR = "{{Infobox";
        int startPos = wikiText.indexOf(INFOBOX_CONST_STR);
        if (startPos < 0) return null;
        int bracketCount = 2;
        int endPos = startPos + INFOBOX_CONST_STR.length();
        for (; endPos < wikiText.length(); endPos++) {
            switch (wikiText.charAt(endPos)) {
                case '}':
                    bracketCount--;
                    break;
                case '{':
                    bracketCount++;
                    break;
                default:
            }
            if (bracketCount == 0) break;
        }
        String infoBoxText = wikiText.substring(startPos, endPos + 1);
        infoBoxText = stripCite(infoBoxText); // strip clumsy {{cite}} tags
        // strip any html formatting
        infoBoxText = infoBoxText.replaceAll("&gt;", ">");
        infoBoxText = infoBoxText.replaceAll("&lt;", "<");
        infoBoxText = infoBoxText.replaceAll("<ref.*?>.*?</ref>", " ");
        infoBoxText = infoBoxText.replaceAll("</?.*?>", " ");
        return new InfoBox(infoBoxText);
    }

    private String stripCite(String text) {
        String CITE_CONST_STR = "{{cite";
        int startPos = text.indexOf(CITE_CONST_STR);
        if (startPos < 0) return text;
        int bracketCount = 2;
        int endPos = startPos + CITE_CONST_STR.length();
        for (; endPos < text.length(); endPos++) {
            switch (text.charAt(endPos)) {
                case '}':
                    bracketCount--;
                    break;
                case '{':
                    bracketCount++;
                    break;
                default:
            }
            if (bracketCount == 0) break;
        }
        text = text.substring(0, startPos - 1) + text.substring(endPos);
        return stripCite(text);
    }

    public boolean isDisambiguationPage() {
        return disambiguation;
    }

    public String getTranslatedTitle(String languageCode) {
        Pattern pattern = Pattern.compile("^\\[\\[" + languageCode + ":(.*?)\\]\\]$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(wikiText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
