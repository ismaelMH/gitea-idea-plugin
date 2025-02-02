/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.authentication.accounts

import com.github.leondevlifelog.gitea.exception.GiteaParseException
import com.intellij.collaboration.api.ServerPath
import com.intellij.util.io.URLUtil
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import org.apache.http.client.utils.URIBuilder
import org.jetbrains.annotations.NotNull
import java.net.MalformedURLException
import java.net.URI
import java.net.URL


/**
 * Gitea server reference allowing to specify custom port and path to instance
 */
@Tag("server")
class GiteaServerPath(usHttp: Boolean, host: String, port: Int) : ServerPath {
    constructor() : this(DEFAULT_SERVER.myUseHttp, DEFAULT_SERVER.myHost, DEFAULT_SERVER.myPort)

    companion object {
        @JvmStatic
        val DEFAULT_SERVER = GiteaServerPath(false, "localhost", 443)

        @Throws(GiteaParseException::class)
        fun from(url: String): GiteaServerPath {
            try {
                val instanceUrl = URL(url)
                return GiteaServerPath(
                    instanceUrl.protocol == URLUtil.HTTP_PROTOCOL, instanceUrl.host, instanceUrl.port
                )
            } catch (e: MalformedURLException) {
                throw GiteaParseException()
            }
        }
    }

    @Attribute("useHttp")
    private var myUseHttp: Boolean = usHttp

    @Attribute("host")
    private var myHost: String = host

    @Attribute("port")
    private var myPort: Int = port

    override fun toString(): String {
        return "${getSchema()}${URLUtil.SCHEME_SEPARATOR}$myHost${if (myPort != -1) ":$myPort" else ""}"
    }

    override fun toURI(): URI {
        return URIBuilder().apply {
            scheme = getSchema()
            host = myHost
            port = myPort
        }.build()
    }

    @NotNull
    fun getSchema(): String {
        return if (!myUseHttp) URLUtil.HTTPS_PROTOCOL else URLUtil.HTTP_PROTOCOL
    }

    @NotNull
    fun getHost(): String {
        return myHost
    }

    @NotNull
    fun getPort(): Int {
        return myPort
    }

    fun toAccessTokenUrl(): String {
        return "${getSchema()}${URLUtil.SCHEME_SEPARATOR}$myHost${if (myPort != -1) ":$myPort" else ""}/user/settings/applications"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiteaServerPath

        if (myUseHttp != other.myUseHttp) return false
        if (myHost != other.myHost) return false
        if (myPort != other.myPort) return false

        return true
    }

    fun equals(other: Any?, ignoreProtocol: Boolean): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiteaServerPath

        if (!ignoreProtocol) {
            if (myUseHttp != other.myUseHttp) return false
        }
        if (myHost != other.myHost) return false
        if (myPort != other.myPort) return false

        return true
    }

    override fun hashCode(): Int {
        var result = myUseHttp.hashCode()
        result = 31 * result + myHost.hashCode()
        result = 31 * result + myPort
        return result
    }

}