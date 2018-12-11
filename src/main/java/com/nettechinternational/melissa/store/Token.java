package com.nettechinternational.melissa.store;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class Token {

    private String id;
    private String username;
    private String code;
    private long createdAt;
    private Long endlife;
    private boolean connected;

    private Application application;

    public Token() {
    }

    public Token(JsonObject object) {
        TokenConverter.fromJson(object, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        TokenConverter.toJson(this, json);
        return json;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getEndlife() {
        return endlife;
    }

    public void setEndlife(Long endlife) {
        this.endlife = endlife;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public String toString() {
        return "Token{" + "id=" + id + ", username=" + username + ", code=" + code + ", createdAt=" + createdAt + ", endlife=" + endlife + ", connected=" + connected + ", application=" + application + '}';
    }

}
