package com.airey.domain.athlete;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Athlete {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("resource_state")
    @Expose
    private Integer resourceState;
    @SerializedName("firstname")
    @Expose
    private String firstname;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("sex")
    @Expose
    private String sex;
    @SerializedName("premium")
    @Expose
    private Boolean premium;
    @SerializedName("summit")
    @Expose
    private Boolean summit;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("badge_type_id")
    @Expose
    private Integer badgeTypeId;
    @SerializedName("profile_medium")
    @Expose
    private String profileMedium;
    @SerializedName("profile")
    @Expose
    private String profile;
    @SerializedName("friend")
    @Expose
    private Object friend;
    @SerializedName("follower")
    @Expose
    private Object follower;
    @SerializedName("email")
    @Expose
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getResourceState() {
        return resourceState;
    }

    public void setResourceState(Integer resourceState) {
        this.resourceState = resourceState;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public Boolean getSummit() {
        return summit;
    }

    public void setSummit(Boolean summit) {
        this.summit = summit;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getBadgeTypeId() {
        return badgeTypeId;
    }

    public void setBadgeTypeId(Integer badgeTypeId) {
        this.badgeTypeId = badgeTypeId;
    }

    public String getProfileMedium() {
        return profileMedium;
    }

    public void setProfileMedium(String profileMedium) {
        this.profileMedium = profileMedium;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Object getFriend() {
        return friend;
    }

    public void setFriend(Object friend) {
        this.friend = friend;
    }

    public Object getFollower() {
        return follower;
    }

    public void setFollower(Object follower) {
        this.follower = follower;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("username", username).append("resourceState", resourceState).append("firstname", firstname).append("lastname", lastname).append("city", city).append("state", state).append("country", country).append("sex", sex).append("premium", premium).append("summit", summit).append("createdAt", createdAt).append("updatedAt", updatedAt).append("badgeTypeId", badgeTypeId).append("profileMedium", profileMedium).append("profile", profile).append("friend", friend).append("follower", follower).append("email", email).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(sex).append(follower).append(premium).append(state).append(lastname).append(firstname).append(badgeTypeId).append(resourceState).append(country).append(city).append(updatedAt).append(id).append(username).append(email).append(createdAt).append(summit).append(friend).append(profileMedium).append(profile).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Athlete) == false) {
            return false;
        }
        Athlete rhs = ((Athlete) other);
        return new EqualsBuilder().append(sex, rhs.sex).append(follower, rhs.follower).append(premium, rhs.premium).append(state, rhs.state).append(lastname, rhs.lastname).append(firstname, rhs.firstname).append(badgeTypeId, rhs.badgeTypeId).append(resourceState, rhs.resourceState).append(country, rhs.country).append(city, rhs.city).append(updatedAt, rhs.updatedAt).append(id, rhs.id).append(username, rhs.username).append(email, rhs.email).append(createdAt, rhs.createdAt).append(summit, rhs.summit).append(friend, rhs.friend).append(profileMedium, rhs.profileMedium).append(profile, rhs.profile).isEquals();
    }

}
