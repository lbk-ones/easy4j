package io.github.lbkones.common;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * 网络工具类，提供IP地址处理、网络请求、DNS解析等常用功能
 * 仿照hutools设计，功能全面且API简洁易用
 */
public final class NetUtils {

    // IPv4地址正则表达式
    private static final String IPV4_PATTERN = 
        "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})(\\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})){3}$";
    
    // IPv6地址正则表达式（简化版）
    private static final String IPV6_PATTERN = 
        "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
    
    private static final Pattern IPV4_REGEX = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPV6_REGEX = Pattern.compile(IPV6_PATTERN);

    // 私有构造方法，防止实例化
    private NetUtils() {
        throw new AssertionError("工具类不能实例化");
    }

    /**
     * =====================
     * IP地址验证与解析
     * =====================
     */

    /**
     * 验证IP地址是否合法（支持IPv4和IPv6）
     * @param ip IP地址字符串
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return isValidIPv4(ip) || isValidIPv6(ip);
    }

    /**
     * 验证IPv4地址是否合法
     * @param ip IPv4地址字符串
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidIPv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV4_REGEX.matcher(ip).matches();
    }

    /**
     * 验证IPv6地址是否合法
     * @param ip IPv6地址字符串
     * @return 合法返回true，否则返回false
     */
    public static boolean isValidIPv6(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV6_REGEX.matcher(ip).matches();
    }

    /**
     * =====================
     * IP地址转换与计算
     * =====================
     */

    /**
     * 将IPv4地址转换为长整型数值
     * @param ip IPv4地址字符串
     * @return 长整型数值，非法IP返回-1
     */
    public static long ipv4ToLong(String ip) {
        if (!isValidIPv4(ip)) {
            return -1;
        }
        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = result * 256 + Integer.parseInt(octets[i]);
        }
        return result;
    }

    /**
     * 将长整型数值转换为IPv4地址
     * @param ipLong 长整型数值
     * @return IPv4地址字符串，非法数值返回null
     */
    public static String longToIPv4(long ipLong) {
        if (ipLong < 0 || ipLong > 0xFFFFFFFFL) {
            return null;
        }
        return (ipLong >>> 24) + "." +
               ((ipLong >>> 16) & 0xFF) + "." +
               ((ipLong >>> 8) & 0xFF) + "." +
               (ipLong & 0xFF);
    }

    /**
     * 获取子网掩码对应的网络地址
     * @param ip IPv4地址
     * @param subnetMask 子网掩码（如"255.255.255.0"）
     * @return 网络地址，非法输入返回null
     */
    public static String getNetworkAddress(String ip, String subnetMask) {
        if (!isValidIPv4(ip) || !isValidIPv4(subnetMask)) {
            return null;
        }
        long ipLong = ipv4ToLong(ip);
        long maskLong = ipv4ToLong(subnetMask);
        long networkLong = ipLong & maskLong;
        return longToIPv4(networkLong);
    }

    /**
     * 获取子网掩码对应的广播地址
     * @param ip IPv4地址
     * @param subnetMask 子网掩码（如"255.255.255.0"）
     * @return 广播地址，非法输入返回null
     */
    public static String getBroadcastAddress(String ip, String subnetMask) {
        if (!isValidIPv4(ip) || !isValidIPv4(subnetMask)) {
            return null;
        }
        long ipLong = ipv4ToLong(ip);
        long maskLong = ipv4ToLong(subnetMask);
        long broadcastLong = ipLong | (~maskLong);
        return longToIPv4(broadcastLong);
    }

    /**
     * =====================
     * 网络请求与连接
     * =====================
     */

    /**
     * 检查指定端口是否可用
     * @param port 端口号
     * @return 可用返回true，否则返回false
     */
    public static boolean isPortAvailable(int port) {
        if (port < 1 || port > 65535) {
            return false;
        }
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取本地主机名
     * @return 本地主机名，获取失败返回null
     */
    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 获取本地IP地址（IPv4）
     * @return 本地IP地址，获取失败返回null
     */
    public static String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }

    /**
     * =====================
     * DNS解析与域名处理
     * =====================
     */

    /**
     * 解析域名获取IP地址列表
     * @param domain 域名
     * @return IP地址列表，解析失败返回空列表
     */
    public static List<String> resolveDomain(String domain) {
        List<String> ips = new ArrayList<>();
        if (domain == null || domain.isEmpty()) {
            return ips;
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            for (InetAddress addr : addresses) {
                ips.add(addr.getHostAddress());
            }
        } catch (UnknownHostException e) {
            // 解析失败，返回空列表
        }
        return ips;
    }

    /**
     * 获取域名的主域名（如"www.example.com"返回"example.com"）
     * @param domain 完整域名
     * @return 主域名，非法输入返回null
     */
    public static String getMainDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        String[] parts = domain.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    /**
     * =====================
     * 其他实用方法
     * =====================
     */

    /**
     * 检查URL是否有效
     * @param url URL字符串
     * @return 有效返回true，否则返回false
     */
    public static boolean isValidURL(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * 从URL中提取域名
     * @param url URL字符串
     * @return 域名，提取失败返回null
     */
    public static String extractDomainFromURL(String url) {
        if (!isValidURL(url)) {
            return null;
        }
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 检查网络是否可达
     * @param host 主机名或IP
     * @param timeout 超时时间（毫秒）
     * @return 可达返回true，否则返回false
     */
    public static boolean isNetworkReachable(String host, int timeout) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        try {
            return InetAddress.getByName(host).isReachable(timeout);
        } catch (IOException e) {
            return false;
        }
    }
}