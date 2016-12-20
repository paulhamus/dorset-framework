/*
 * Copyright 2016 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.jhuapl.dorset.agents;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.jhuapl.dorset.ResponseStatus;
import edu.jhuapl.dorset.agents.AbstractAgent;
import edu.jhuapl.dorset.agents.AgentRequest;
import edu.jhuapl.dorset.agents.AgentResponse;
import edu.jhuapl.dorset.agents.Description;
import edu.jhuapl.dorset.http.HttpClient;
import edu.jhuapl.dorset.http.HttpRequest;
import edu.jhuapl.dorset.http.HttpResponse;
import edu.jhuapl.dorset.nlp.RuleBasedTokenizer;
import edu.jhuapl.dorset.nlp.Tokenizer;

/**
 * Weather agent
 *
 * Weather Underground has an API that provides weather information.
 * Documentation on the api here:
 * https://www.wunderground.com/weather/api/d/docs?d=index
 */
public class WeatherAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(WeatherAgent.class);

    private static final String SUMMARY =
                    "Get information about the weather (currently hardcoded to current time, Laurel, MD and returns temp(f))";
    private static final String EXAMPLE = "Tell me the current weather in Laurel Maryland?";

    private String apikey;
    private HttpClient client;

    /**
     * Create a weather agent
     *
     * @param client  http client
     * @param apikey  A Weather Underground API key 
     */
    public WeatherAgent(HttpClient client, String apikey) {
        this.apikey = apikey;
        this.client = client;
        this.setDescription(new Description("weather", SUMMARY, EXAMPLE));
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        logger.debug("Handling the request: " + request.getText());
        
        String requestText = request.getText();
        
        AgentResponse response = null;
        
        String data = requestData(requestText);        
        
        if (data == null) {
            response = new AgentResponse(new ResponseStatus(
                            ResponseStatus.Code.AGENT_INTERNAL_ERROR,
                            "Something went wrong with connection"));
            
            return response;    
        }
        
        if (validateData(data))
        {
            response = createResponse(data);
            
            return response;
        }
        else{
            response = new AgentResponse(new ResponseStatus(
                ResponseStatus.Code.AGENT_INTERNAL_ERROR,
                "Something went wrong with the Weather Underground API request. Please check your API key."));
        
            return response;
      
        }
        
    }
    
    protected Boolean validateData(String json) {
        
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
        
        JsonObject currentObs = jsonObj.getAsJsonObject("current_observation");
        
        if(currentObs == null){
            
            return false;
        }
        
        return true;
 
    }

    protected String requestData(String requestText) {
        logger.debug("Creating URL string");
        
        //String urlString = "http://api.wunderground.com/api/8eaf1eec835a1033/conditions/q/MD/Laurel.json";
        
        // apikey 8eaf1eec835a1033
        String urlString = "http://api.wunderground.com/api/" + this.apikey + "/conditions/q/MD/Laurel.json";
        HttpResponse response = client.execute(HttpRequest.get(urlString));
        if (response == null || response.isError()) {
            return null;
        }
        return response.asString();
    }

    protected AgentResponse createResponse(String json) {
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(json, JsonObject.class);
        JsonObject currentObs = jsonObj.getAsJsonObject("current_observation");
        String temp_f = currentObs.get("temp_f").getAsString();
        
        String responseText = "The current temperature in Laurel is "+temp_f;
 
        return new AgentResponse(responseText);
    }

}
