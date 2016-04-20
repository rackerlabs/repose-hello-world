/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2016 Rackspace US, Inc.
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
package org.openrepose.filters.custom.helloworldkotlin

import org.openrepose.commons.config.manager.UpdateListener
import org.openrepose.commons.utils.servlet.http.HttpServletRequestWrapper
import org.openrepose.commons.utils.servlet.http.HttpServletResponseWrapper
import org.openrepose.commons.utils.servlet.http.ResponseMode
import org.openrepose.core.filter.FilterConfigHelper
import org.openrepose.core.services.config.ConfigurationService
import org.openrepose.filters.custom.helloworldkotlin.config.HelloWorldKotlinConfig
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Named
class HelloWorldKotlinFilter @Inject constructor(configurationService: ConfigurationService) : Filter, UpdateListener<HelloWorldKotlinConfig> {

    private final val LOG = LoggerFactory.getLogger(HelloWorldKotlinFilter::class.java)
    private final val DEFAULT_CONFIG = "hello-world-kotlin.cfg.xml"

    private val configurationService = configurationService
    private var configurationFile = DEFAULT_CONFIG
    private var messages = emptyList<String>()
    private var initialized = false

    override fun init(filterConfig: FilterConfig?) {
        configurationFile = FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG)
        LOG.info("Initializing filter using config $configurationFile")
        val xsdURL = javaClass.getResource("/META-INF/schema/config/hello-world-kotlin.xsd")
        configurationService.subscribeTo(
                filterConfig!!.filterName,
                configurationFile,
                xsdURL,
                this,
                HelloWorldKotlinConfig::class.java)
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        if (!isInitialized) {
            LOG.error("Hello World Kotlin filter has not yet initialized...")
            (response as HttpServletResponse).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        } else {
            val wrappedHttpRequest = HttpServletRequestWrapper(request as HttpServletRequest)
            val wrappedHttpResponse = HttpServletResponseWrapper(
                    response as HttpServletResponse, ResponseMode.PASSTHROUGH, ResponseMode.PASSTHROUGH)

            LOG.trace("Hello World Kotlin filter processing request...")
            messages.forEach { LOG.info("Request message: $it") }

            LOG.trace("Hello World Kotlin filter passing on down the Filter Chain...")
            filterChain.doFilter(wrappedHttpRequest, wrappedHttpResponse)

            LOG.trace("Hello World Kotlin filter processing response...")
            messages.forEach { LOG.info("Response message: $it") }
        }
    }

    override fun destroy() {
        configurationService.unsubscribeFrom(configurationFile, this)
    }

    override fun configurationUpdated(config: HelloWorldKotlinConfig) {
        messages = config.messages.message.map { it.value }
        initialized = true
    }

    override fun isInitialized(): Boolean = initialized
}