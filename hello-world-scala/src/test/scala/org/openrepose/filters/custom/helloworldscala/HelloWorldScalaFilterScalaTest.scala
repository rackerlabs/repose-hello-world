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
package org.openrepose.filters.custom.helloworldscala

import java.net.URL

import org.openrepose.filters.custom.helloworldscala.config.{HelloWorldScalaConfig, Message, MessageList}
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.test.appender.ListAppender
import org.junit.runner.RunWith
import org.mockito.{ArgumentCaptor, Matchers, Mockito}
import org.openrepose.core.services.config.ConfigurationService
import org.scalatest._
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.mock.web.{MockFilterChain, MockFilterConfig, MockHttpServletRequest, MockHttpServletResponse}

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class HelloWorldScalaFilterScalaTest extends FunSpec with BeforeAndAfterAll with BeforeAndAfter with GivenWhenThen with org.scalatest.Matchers with MockitoSugar {
  var filter: HelloWorldScalaFilter = _
  var config: HelloWorldScalaConfig = _
  var servletRequest: MockHttpServletRequest = _
  var servletResponse: MockHttpServletResponse = _
  var filterChain: MockFilterChain = _
  var mockConfigService: ConfigurationService = _
  var mockFilterConfig: MockFilterConfig = _
  var totalMessages: Int = _
  var listAppender: ListAppender = _

  override def beforeAll() {
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
      "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")
  }

  before {
    servletRequest = new MockHttpServletRequest
    servletResponse = new MockHttpServletResponse
    filterChain = new MockFilterChain
    mockConfigService = mock[ConfigurationService]
    mockFilterConfig = new MockFilterConfig("HelloWorldScalaFilter")
    filter = new HelloWorldScalaFilter(mockConfigService)
    config = new HelloWorldScalaConfig
    val messageList = new MessageList
    val messages = messageList.getMessage
    val message1 = new Message
    val message2 = new Message
    val message3 = new Message
    message1.setValue("Message 1")
    message2.setValue("Message 2")
    message3.setValue("Message 3")
    messages.add(message1)
    messages.add(message2)
    messages.add(message3)
    config.setMessages(messageList)
    totalMessages = messages.size()
    val ctx = LogManager.getContext(false).asInstanceOf[LoggerContext]
    listAppender = ctx.getConfiguration.getAppender("List0").asInstanceOf[ListAppender].clear
  }

  after {
    if (filter.isInitialized) filter.destroy()
  }

  describe("when the configuration is updated") {
    it("should become initialized") {
      Given("an un-initialized filter and the default configuration")
      !filter.isInitialized

      When("the configuration is updated")
      filter.configurationUpdated(config)

      Then("the filter should be initialized")
      filter.isInitialized
      val events = listAppender.getEvents.asScala.toList.map(_.getMessage.getFormattedMessage)
      events.count(_.contains("Update   message: ")) shouldBe totalMessages
    }
  }

  describe("when initializing the filter") {
    it("should initialize the configuration to the default configuration") {
      Given("an un-initialized filter and a mock'd Filter Config")
      !filter.isInitialized
      val argumentCaptor = ArgumentCaptor.forClass(classOf[URL])

      When("the filter is initialized")
      filter.init(mockFilterConfig)

      Then("the filter should register with the ConfigurationService")
      Mockito.verify(mockConfigService).subscribeTo(
        Matchers.eq("HelloWorldScalaFilter"),
        Matchers.eq("hello-world-scala.cfg.xml"),
        argumentCaptor.capture,
        Matchers.eq(filter),
        Matchers.eq(classOf[HelloWorldScalaConfig]))

      argumentCaptor.getValue.toString.endsWith("/META-INF/schema/config/hello-world-scala.xsd")
    }
    it("should initialize the configuration to the given configuration") {
      Given("an un-initialized filter and a mock'd Filter Config")
      !filter.isInitialized
      mockFilterConfig.addInitParameter("filter-config", "another-name.cfg.xml")

      When("the filter is initialized")
      filter.init(mockFilterConfig)

      Then("the filter should register with the ConfigurationService")
      Mockito.verify(mockConfigService).subscribeTo(
        Matchers.anyString,
        Matchers.eq("another-name.cfg.xml"),
        Matchers.any(classOf[URL]),
        Matchers.any(classOf[HelloWorldScalaFilter]),
        Matchers.eq(classOf[HelloWorldScalaConfig]))
    }
  }

  describe("when destroying the filter") {
    it("should unregister the configuration from the configuration service") {
      Given("an un-initialized filter and a mock'd Filter Config")
      !filter.isInitialized
      mockFilterConfig.addInitParameter("filter-config", "another-name.cfg.xml")

      When("the filter is initialized and destroyed")
      filter.init(mockFilterConfig)
      filter.destroy()

      Then("the filter should unregister with the ConfigurationService")
      Mockito.verify(mockConfigService).unsubscribeFrom("another-name.cfg.xml", filter)
    }
  }

  describe("when the filter is accessed") {
    it("should log the messages") {
      Given("a request")
      servletRequest.setRequestURI("/path/to/bad")
      servletRequest.setMethod("GET")
      filter.configurationUpdated(config)

      When("the resource is requested")
      filter.doFilter(servletRequest, servletResponse, filterChain)

      Then("the configured messages should be logged.")
      val events = listAppender.getEvents.asScala.toList.map(_.getMessage.getFormattedMessage)
      events.count(_.contains("Update   message: ")) shouldBe totalMessages
      events.count(_.contains("Request  message: ")) shouldBe totalMessages
      events.count(_.contains("Response message: ")) shouldBe totalMessages
    }
  }
}
