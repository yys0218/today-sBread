package com.ex.common;

import org.springframework.stereotype.Component;

// 주소 마스킹을 위한 유틸 클래스
@Component("stringUtil")
public class StringUtil {

    // 주소 마스킹
    // 필요한 매개변수 : String address
    public String maskAddress(String address) {
        if (address == null)
            return "";
        // 숫자 뒤에 오는 문자열을 마스킹처리
        return address.replaceAll("(\\d+\\s*)(.*)", "$1 ******");
    }

    // 전화번호 마스킹
    // 필요한 매개변수 : String phone
    // 리턴되는 형식 : 010-00**-****
    public String maskPhone(String phone) {
        if (phone == null) {
            return "";
        }
        // 010 5669 8920
        // 012 3456 78910
        String firstSub = phone.substring(0, 3);
        String secondSub = phone.substring(3, 5);
        String secondMasked = secondSub + "**-";
        String lastMasked = secondMasked + "****";
        return firstSub + "-" + lastMasked;
    }

    public String formatPhone(String phone) {
        if (phone == null) {
            return "";
        }
        String firstSub = phone.substring(0, 3);
        String secondSub = phone.substring(3, 7);
        String thirdSub = phone.substring(7, 11);
        return firstSub + "-" + secondSub + "-" + thirdSub;
    }

}
