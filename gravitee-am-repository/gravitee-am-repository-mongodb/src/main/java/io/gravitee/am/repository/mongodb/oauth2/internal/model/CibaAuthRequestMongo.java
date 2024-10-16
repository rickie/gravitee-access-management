/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.repository.mongodb.oauth2.internal.model;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;
import java.util.Set;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CibaAuthRequestMongo {

    @BsonId private String id;

    private String status;

    private String subject;

    private Set<String> scopes;

    private String userCode;

    @BsonProperty("client")
    private String client;

    @BsonProperty("created_at")
    private Date createdAt;

    @BsonProperty("last_acess_at")
    private Date lastAccessAt;

    @BsonProperty("expire_at")
    private Date expireAt;

    @BsonProperty("ext_transaction_id")
    private String externalTrxId;

    @BsonProperty("external_information")
    private Document externalInformation;

    @BsonProperty("device_notifier_id")
    private String deviceNotifierId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public Date getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(Date lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }

    public String getExternalTrxId() {
        return externalTrxId;
    }

    public void setExternalTrxId(String externalTrxId) {
        this.externalTrxId = externalTrxId;
    }

    public Document getExternalInformation() {
        return externalInformation;
    }

    public void setExternalInformation(Document externalInformation) {
        this.externalInformation = externalInformation;
    }

    public String getDeviceNotifierId() {
        return deviceNotifierId;
    }

    public void setDeviceNotifierId(String deviceNotifierId) {
        this.deviceNotifierId = deviceNotifierId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CibaAuthRequestMongo that = (CibaAuthRequestMongo) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
