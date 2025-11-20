package com.byul.wheresmymoney.backend.API.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ApiDTO {
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KiwoomCredentials {
        private String appkey;
        private String secretkey;
    }
}
