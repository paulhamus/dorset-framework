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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import edu.jhuapl.dorset.ResponseStatus;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.AgentRequest;
import edu.jhuapl.dorset.agents.AgentResponse;
import edu.jhuapl.dorset.http.HttpClient;
import edu.jhuapl.dorset.http.HttpRequest;
import edu.jhuapl.dorset.http.HttpResponse;

public class WeatherAgentTest {

    private String apikey = "default_apikey";
    
    @Test
    public void testGetGoodResponse() {
        String query = "Tell me the weather";
        String jsonData = FileReader.getFileAsString("weather/Laurel.json");
        HttpClient client = new FakeHttpClient(new FakeHttpResponse(jsonData));

        Agent agent = new WeatherAgent(client, apikey);
        AgentResponse response = agent.process(new AgentRequest(query));

        assertTrue(response.isSuccess());
        assertTrue(response.getText().startsWith("The current temperature in Laurel is 61.0"));
    }

    @Test
    public void testBadApiKey() {
        String query = "Tell me the weather";
        String jsonData = FileReader.getFileAsString("weather/bad_key.json");
        
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.isSuccess()).thenReturn(false);
        when(httpResponse.asString()).thenReturn(jsonData);
        HttpClient client = mock(HttpClient.class);
        when(client.execute(any(HttpRequest.class))).thenReturn(httpResponse);

        Agent agent = new WeatherAgent(client, apikey);
        AgentResponse response = agent.process(new AgentRequest(query));

        assertFalse(response.isSuccess());        
        assertEquals(ResponseStatus.Code.AGENT_INTERNAL_ERROR, response.getStatus().getCode());
    }
    
}
