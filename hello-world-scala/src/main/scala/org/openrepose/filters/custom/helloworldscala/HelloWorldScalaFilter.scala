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
import javax.inject.{Inject, Named}
import javax.servlet._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.openrepose.filters.custom.helloworldscala.config.HelloWorldScalaConfig
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.openrepose.commons.config.manager.UpdateListener
import org.openrepose.commons.utils.servlet.http.{MutableHttpServletRequest, MutableHttpServletResponse}
import org.openrepose.core.filter.FilterConfigHelper
import org.openrepose.core.services.config.ConfigurationService

import scala.collection.JavaConversions._

// @Named can be omitted if class doesn't have a constructor with arguments
@Named
class HelloWorldScalaFilter @Inject()(configurationService: ConfigurationService)

  extends Filter
  with UpdateListener[HelloWorldScalaConfig]
  with LazyLogging {

  private final val DEFAULT_CONFIG = "hello-world-scala.cfg.xml"

  private var configurationFile: String = DEFAULT_CONFIG
  private var configuration: HelloWorldScalaConfig = _
  private var initialized = false

  override def init(filterConfig: FilterConfig): Unit = {
    configurationFile = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG)
    logger.info("Initializing filter using config " + configurationFile)
    // Must match the .xsd file created in step 18.
    val xsdURL: URL = getClass.getResource("/META-INF/schema/config/hello-world-scala.xsd")
    configurationService.subscribeTo(
      filterConfig.getFilterName,
      configurationFile,
      xsdURL,
      this,
      classOf[HelloWorldScalaConfig]
    )
  }

  override def destroy(): Unit = {
    configurationService.unsubscribeFrom(configurationFile, this)
  }

  override def doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain): Unit = {
    if (!initialized) {
      logger.error("Hello World Scala filter has not yet initialized...")
      servletResponse.asInstanceOf[HttpServletResponse].sendError(500)
    } else {
      val mutableHttpRequest = MutableHttpServletRequest.wrap(servletRequest.asInstanceOf[HttpServletRequest])
      val mutableHttpResponse = MutableHttpServletResponse.wrap(mutableHttpRequest, servletResponse.asInstanceOf[HttpServletResponse])

      // This is where this filter's custom logic is invoked.
      // For the purposes of this example, the configured messages are logged
      // before and after the Filter Chain is processed.
      logger.trace("Hello World Scala filter processing request...")
      Option(configuration.getMessages).flatMap {
        messages => Option(messages.getMessage)
      }.toList.flatten.foreach {
        message => logger.info("Request  message: " + message.getValue)
      }

      logger.trace("Hello World Scala filter passing on down the Filter Chain...")
      filterChain.doFilter(mutableHttpRequest, mutableHttpResponse)

      logger.trace("Hello World Scala filter processing response...")
      Option(configuration.getMessages).flatMap {
        messages => Option(messages.getMessage)
      }.toList.flatten.foreach {
        message => logger.info("Response message: " + message.getValue)
      }
    }
    logger.trace("Hello World filter returning response...")
  }

  // This class is generated from.xsd file.
  override def configurationUpdated(configurationObject: HelloWorldScalaConfig): Unit = {
    configuration = configurationObject
    Option(configuration.getMessages).flatMap {
      messages => Option(messages.getMessage)
    }.toList.flatten.foreach {
      message => logger.info("Update   message: " + message.getValue)
    }
    initialized = true
  }

  override def isInitialized: Boolean = initialized
}
