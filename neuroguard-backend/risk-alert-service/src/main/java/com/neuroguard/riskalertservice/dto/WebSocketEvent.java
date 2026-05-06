package com.neuroguard.riskalertservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent<T> {
    private String action;     // CREATE, UPDATE, DELETE, RESOLVE
    private String entityType; // ALERT
    private T data;
}
