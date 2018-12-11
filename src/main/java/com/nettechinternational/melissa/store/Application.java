package com.nettechinternational.melissa.store;


import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
@DataObject(generateConverter = true)
public class Application {

    private String id;
    private String name;
    private String description = "";
    private String apiToken = "";
    private boolean enabled = true;
    private long createdAt;

    public Application() {
    }

    public Application(JsonObject json) {
        ApplicationConverter.fromJson(json, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        ApplicationConverter.toJson(this, json);
        return json;
    }

    public String getId() {
        return id;
    }

    public Application setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Application setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Application setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Application setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getApiToken() {
        return apiToken;
    }

    public Application setApiToken(String apiToken) {
        this.apiToken = apiToken;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Application other = (Application) obj;
        return Objects.equals(this.id, other.id);
    }

}
