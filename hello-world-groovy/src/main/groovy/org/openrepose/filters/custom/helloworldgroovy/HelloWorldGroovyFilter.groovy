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
package org.openrepose.filters.custom.helloworldgroovy

import org.openrepose.commons.config.manager.UpdateListener
import org.openrepose.commons.utils.servlet.http.HttpServletRequestWrapper
import org.openrepose.commons.utils.servlet.http.HttpServletResponseWrapper
import org.openrepose.commons.utils.servlet.http.ResponseMode
import org.openrepose.core.filter.FilterConfigHelper
import org.openrepose.core.services.config.ConfigurationService
import org.openrepose.filters.custom.helloworldgroovy.config.HelloWorldGroovyConfig
import org.openrepose.filters.custom.helloworldgroovy.config.Message
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.inject.Named
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// @Named can be omitted if class doesn't have a constructor with arguments
@Named
public class HelloWorldGroovyFilter implements Filter, UpdateListener<HelloWorldGroovyConfig> {
    private static final LOG = LoggerFactory.getLogger(HelloWorldGroovyFilter.class)
    private static final DEFAULT_CONFIG = "hello-world-groovy.cfg.xml"
    private configurationService
    private configurationFile = DEFAULT_CONFIG
    private configuration = null
    private initialized = false

    @Inject
    public HelloWorldGroovyFilter(ConfigurationService configurationService) {
        this.configurationService = configurationService
    }

    @Override
    def void init(FilterConfig filterConfig) throws ServletException {
        configurationFile = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG)
        LOG.info("Initializing filter using config " + configurationFile)
        // Must match the .xsd file created in step 18.
        def xsdURL = getClass().getResource("/META-INF/schema/config/hello-world-groovy.xsd")
        configurationService.subscribeTo(
                filterConfig.getFilterName(),
                configurationFile,
                xsdURL,
                this,
                HelloWorldGroovyConfig.class
        )
    }

    @Override
    def void destroy() {
        configurationService.unsubscribeFrom(configurationFile, this)
    }

    @Override
    def void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!initialized) {
            LOG.error("Hello World Groovy filter has not yet initialized...")
            ((HttpServletResponse) servletResponse).sendError(500)
        } else {
            def wrappedHttpRequest = new HttpServletRequestWrapper(servletRequest as HttpServletRequest)
            def wrappedHttpResponse = new HttpServletResponseWrapper(
                    servletResponse as HttpServletResponse, ResponseMode.PASSTHROUGH, ResponseMode.PASSTHROUGH)

            // This is where this filter's custom logic is invoked.
            // For the purposes of this example, the configured messages are logged
            // before and after the Filter Chain is processed.
            LOG.trace("Hello World Groovy filter processing request...")
            def messageList = configuration.getMessages()
            for (Message message : messageList.getMessage()) {
                LOG.info("Request  message: " + message.getValue())
            }

            LOG.trace("Hello World Groovy filter passing on down the Filter Chain...")
            filterChain.doFilter(wrappedHttpRequest, wrappedHttpResponse)

            LOG.trace("Hello World Groovy filter processing response...")
            for (Message message : messageList.getMessage()) {
                LOG.info("Response message: " + message.getValue())
            }
        }
        LOG.trace("Hello World filter returning response...")
    }

    // This class is generated from.xsd file.
    @Override
    def void configurationUpdated(HelloWorldGroovyConfig configurationObject) {
        configuration = configurationObject
        def messageList = configuration.getMessages()
        for (Message message : messageList.getMessage()) {
            LOG.info("Update   message: " + message.getValue())
        }
        initialized = true
    }

    @Override
    def boolean isInitialized() {
        return initialized
    }
}
