package com.airey.domain.authorization;

import com.airey.domain.athlete.Athlete;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Authorization {
    @SerializedName("token_type")
    @Expose
    private String tokenType;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("athlete")
    @Expose
    private Athlete athlete;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tokenType", tokenType).append("accessToken", accessToken).append("athlete", athlete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tokenType).append(accessToken).append(athlete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Authorization) == false) {
            return false;
        }
        Authorization rhs = ((Authorization) other);
        return new EqualsBuilder().append(tokenType, rhs.tokenType).append(accessToken, rhs.accessToken).append(athlete, rhs.athlete).isEquals();
    }

}
