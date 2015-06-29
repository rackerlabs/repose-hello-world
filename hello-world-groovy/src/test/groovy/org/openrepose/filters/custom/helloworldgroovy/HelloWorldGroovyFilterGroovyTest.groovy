/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.filters.custom.helloworldgroovy;

import org.openrepose.filters.custom.helloworldgroovy.config.HelloWorldGroovyConfig;
import org.openrepose.filters.custom.helloworldgroovy.config.Message;
import org.openrepose.filters.custom.helloworldgroovy.config.MessageList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openrepose.core.services.config.ConfigurationService;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class HelloWorldGroovyFilterGroovyTest {
    private HelloWorldGroovyFilter filter;
    private HelloWorldGroovyConfig config;
    private MockHttpServletRequest servletRequest;
    private MockHttpServletResponse servletResponse;
    private MockFilterChain filterChain;
    private ConfigurationService mockConfigService;
    private MockFilterConfig mockFilterConfig;
    private int totalMessages;
    private ListAppender listAppender;

    @BeforeClass
    public static void setupSpec() {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }

    @Before
    public void setup() throws Exception {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        mockConfigService = mock(ConfigurationService.class);
        mockFilterConfig = new MockFilterConfig("HelloWorldGroovyFilter");
        filter = new HelloWorldGroovyFilter(mockConfigService);
        config = new HelloWorldGroovyConfig();
        MessageList messageList = new MessageList();
        List<Message> messages = messageList.getMessage();
        Message message1 = new Message();
        Message message2 = new Message();
        Message message3 = new Message();
        message1.setValue("Message 1");
        message2.setValue("Message 2");
        message3.setValue("Message 3");
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);
        config.setMessages(messageList);
        totalMessages = messages.size();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        listAppender = ((ListAppender) (ctx.getConfiguration().getAppender("List0"))).clear();
    }

    @After
    public void cleanup() throws Exception {
        if (filter.isInitialized()) filter.destroy();
    }

    @Test
    public void testConfigurationInitialized() throws Exception {
        //Given("an un-initialized filter and the default configuration")
        assertFalse(filter.isInitialized());

        //When("the configuration is updated")
        filter.configurationUpdated(config);

        //Then("the filter should be initialized")
        assertTrue(filter.isInitialized());

        List<LogEvent> events = listAppender.getEvents();
        assertEquals(totalMessages, count(events, "Update   message: "));
    }

    @Test
    public void testFilterInitializedDefault() throws Exception {
        //Given("an un-initialized filter and a mock'd Filter Config")
        assertFalse(filter.isInitialized());
        ArgumentCaptor<URL> argumentCaptor = ArgumentCaptor.forClass(URL.class);

        //When("the filter is initialized")
        filter.init(mockFilterConfig);

        //Then("the filter should register with the ConfigurationService")
        Mockito.verify(mockConfigService).subscribeTo(
                Matchers.eq("HelloWorldGroovyFilter"),
                Matchers.eq("hello-world-groovy.cfg.xml"),
                argumentCaptor.capture(),
                Matchers.eq(filter),
                Matchers.eq(HelloWorldGroovyConfig.class));

        assertTrue(argumentCaptor.getAllValues().toString().contains("/META-INF/schema/config/hello-world-groovy.xsd"));
    }

    @Test
    public void testFilterInitializedGiven() throws Exception {
        //Given("an un-initialized filter and a mock'd Filter Config")
        assertFalse(filter.isInitialized());
        mockFilterConfig.addInitParameter("filter-config", "another-name.cfg.xml");

        //When("the filter is initialized")
        filter.init(mockFilterConfig);

        //Then("the filter should register with the ConfigurationService")
        Mockito.verify(mockConfigService).subscribeTo(
                Matchers.anyString(),
                Matchers.eq("another-name.cfg.xml"),
                Matchers.any(URL.class),
                Matchers.any(HelloWorldGroovyFilter.class),
                Matchers.eq(HelloWorldGroovyConfig.class));
    }

    @Test
    public void testDestroyingUnregister() throws Exception {
        //Given("an un-initialized filter and a mock'd Filter Config")
        assertFalse(filter.isInitialized());
        mockFilterConfig.addInitParameter("filter-config", "another-name.cfg.xml");

        //When("the filter is initialized and destroyed")
        filter.init(mockFilterConfig);
        filter.destroy();

        //Then("the filter should unregister with the ConfigurationService")
        Mockito.verify(mockConfigService).unsubscribeFrom("another-name.cfg.xml", filter);
    }

    @Test
    public void testDoFilterLog() throws Exception {
        //Given("a request")
        servletRequest.setRequestURI("/path/to/bad");
        servletRequest.setMethod("GET");
        filter.configurationUpdated(config);

        //When("the resource is requested")
        filter.doFilter(servletRequest, servletResponse, filterChain);

        //Then("the configured messages should be logged.")
        List<LogEvent> events = listAppender.getEvents();
        assertEquals(totalMessages, count(events, "Update   message: "));
        assertEquals(totalMessages, count(events, "Request  message: "));
        assertEquals(totalMessages, count(events, "Response message: "));
    }

    private int count(final List<LogEvent> events, final String msg) {
        int rtn = 0;
        for (LogEvent event : events) {
            if (event.getMessage().getFormattedMessage().contains(msg)) rtn++;
        }
        return rtn;
    }
}
