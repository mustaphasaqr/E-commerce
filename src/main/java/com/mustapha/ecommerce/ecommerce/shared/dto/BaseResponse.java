package com.mustapha.ecommerce.ecommerce.shared.dto;

/**
 * Base Response DTO
 * Responsibility: Common response structure
 */
public class BaseResponse {
    private boolean success;
    private String message;

    public BaseResponse() {
    }

    public BaseResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
