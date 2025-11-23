package com.lpa.demon_slayer_api_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DemonSlayerApiException extends Exception{
    private final HttpStatus status;

    public DemonSlayerApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


    public DemonSlayerApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
   }

   public int getStatusCode() {
        return status.value();
   }

}
