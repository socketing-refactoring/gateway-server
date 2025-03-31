package com.jeein.gateway.dto;

import lombok.Getter;

@Getter
public class JwtMemberDTO {
    private String memberId;
    private String memberEmail;
    private String memberName;
    private String memberNickname;
}
