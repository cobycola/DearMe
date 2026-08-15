package com.zionysus.dearme.north.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答题/首题请求的统一响应载体。
 * 区分 isFirst：True 表示这是首题响应（POST /first-question），False 表示答题后响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultView {

    private NextQuestionResponse response;
    private boolean isFirst;
}