package com.hu.util;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * @Author: hu.chen
 * @DateTime: 2022/7/26 12:08 PM
 */
@Slf4j
public class LocalIpUtils {

    private static final Logger logger = LoggerFactory.getLogger(LocalIpUtils.class);

    private static Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");

    private static String chinaz = "https://ip.chinaz.com";

    public static void main(String[] args) throws Exception {
        System.out.println(getIntranetIP());

    }


    /**
     * 获取内网ip
     *
     * @return
     * @throws Exception
     */
    public static String getIntranetIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "127.0.0.1";
    }



    // ---------------------- valid ----------------------

    /**
     * 判断系统是不是windows
     **/
    public static boolean isWindowsOs() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") > -1) {
            return true;
        }
        return false;
    }

    /**
     * 判断系统是不是mac
     **/
    public static boolean isMacOsx() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("mac") > -1) {
            return true;
        }
        return false;
    }

    /**
     * 判断系统是不是linux
     **/
    public static boolean isLinux() {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("linux") > -1) {
            return true;
        }
        return false;
    }

    // ---------------------- valid ----------------------
}
