package com.example.api;

/**
 * DTO representing a logged-in user session.
 *
 * @author georgegaspar
 */
public class Sessions {
    private String id;
    private String session;
    private String deviceId;

    public Sessions() {
    }

    public Sessions(String id, String sessionId, String deviceId) {
        this.id = id;
        this.session = sessionId;
        this.deviceId = deviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessions() {
        return session;
    }

    public void setSessions(String session) {
        this.session = session;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
