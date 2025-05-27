package xyz.yoinky3000.server_info_api.api;

import java.util.HashMap;
import java.util.Map;

public class APIResponse {
    private final HttpStatus statusCode;
    private final MimeType mimeType;
    private final String body;

    public APIResponse(HttpStatus statusCode, MimeType mimeType, String body) {
        this.statusCode = statusCode;
        this.mimeType = mimeType;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode.getCode();
    }

    public String getMimeType() {
        return mimeType.getType();
    }

    public String getBody() {
        return body;
    }
    public enum HttpStatus {
        // 2xx Success
        OK(200),
        CREATED(201),
        NO_CONTENT(204),

        // 4xx Client Errors
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),

        // 5xx Server Errors
        INTERNAL_SERVER_ERROR(500),
        NOT_IMPLEMENTED(501),
        SERVICE_UNAVAILABLE(503);

        private final int code;
        private static final Map<Integer, HttpStatus> BY_CODE = new HashMap<>();

        static {
            for (HttpStatus status : values()) {
                BY_CODE.put(status.code, status);
            }
        }

        HttpStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static HttpStatus fromCode(int code) {
            HttpStatus status = BY_CODE.get(code);
            if (status == null) {
                throw new IllegalArgumentException("No HttpStatus constant for code: " + code);
            }
            return status;
        }
    }

    public enum MimeType {
        TEXT_PLAIN("text/plain"),
        APPLICATION_JSON("application/json"),
        TEXT_HTML("text/html"),
        APPLICATION_XML("application/xml"),
        IMAGE_JPEG("image/jpeg");

        private final String type;

        MimeType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
