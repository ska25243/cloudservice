package com.wildmeet.gatewayservice.response;

import lombok.Data;

@Data
public class ResponseResult {
    String contentType;
    int status;
    String message;
}
