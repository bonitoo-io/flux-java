package io.bonitoo.platform.dto;

/**
 * The health of Platform.
 *
 * @author Jakub Bednar (bednar@github) (17/09/2018 08:25)
 */
public final class Health {

    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public boolean isHealthy() {
        return "healthy".equals(status);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}