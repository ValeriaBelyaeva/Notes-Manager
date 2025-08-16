package com.example.notes_manager.core.network

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException

/** Разрешаем только IPv4. Если IPv4 записей нет — кидаем UnknownHostException. */
object Ipv4OnlyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val all = Dns.SYSTEM.lookup(hostname)           // пусть системный DNS резолвит
        val v4  = all.filterIsInstance<Inet4Address>()  // оставляем только IPv4
        if (v4.isEmpty()) throw UnknownHostException("No IPv4 for $hostname (had ${all.size} records)")
        return v4
    }
}
