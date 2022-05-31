package org.ants.parser.utils;

public class IpHelper {

    public static String bitsToIP(int bitsNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitsNum; i++) {
            sb.append('1');
        }
        for (int i = 0; i < 32 - bitsNum; i++) {
            sb.append('0');
        }
        String[] ip = new String[4];
        for (int i = 0; i < 32; i += 8) {
            String curNum = sb.substring(i, i + 8);
            ip[i / 8] = Integer.valueOf(curNum, 2).toString();
        }
        return ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
    }

    public static String numberToIp(Long number) {
        StringBuilder ip = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            ip.append(number & 0xff);
            if (i != 0) {
                ip.append(".");
            }
            number = number >> 8;
        }

        return ip.toString();
    }

    public static long ipToNumber(String addr) {
        String[] addrArray = addr.split("\\.");
        if (addrArray.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + addr);
        }

        long num = 0;
        try {
            for (int i = 0; i < 4; i++) {
                long segment = Long.parseLong(addrArray[i]);
                num = (num << 8) + segment;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + addr, e);
        }
        return num;
    }
}
