package net.ccbluex.liquidbounce.handler.network

import io.netty.bootstrap.ChannelFactory
import io.netty.channel.socket.oio.OioSocketChannel
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket

object ProxyManager {
    var isEnable = false
    var proxy = "127.0.0.1:10808"  // V2Ray VPN Default Port
    var proxyType = Proxy.Type.SOCKS

    val proxyInstance: Proxy
        get() = proxy.split(":").let { Proxy(proxyType, InetSocketAddress(it.first(), it.last().toInt())) }

    class ProxyOioChannelFactory(private val proxy: Proxy) : ChannelFactory<OioSocketChannel> {

        override fun newChannel(): OioSocketChannel {
            return OioSocketChannel(Socket(this.proxy))
        }
    }
}