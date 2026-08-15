package com.zionysus.dearme.north.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponse {

    private String sessionId;
    private String status;
    private String topicId;
}