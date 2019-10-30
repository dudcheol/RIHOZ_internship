package com.example.guideline_on_camera.VO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetResponse {
    @SerializedName("response")
    @Expose
    private boolean response;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("registrationNumFront")
    @Expose
    private String registrationNumFront;
    @SerializedName("registrationNumBack")
    @Expose
    private String registrationNumBack;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("visa")
    @Expose
    private String visa;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("contentFirst")
    @Expose
    private String contentFirst;
    @SerializedName("contentSecond")
    @Expose
    private String contentSecond;
    @SerializedName("buttonText")
    @Expose
    private String buttonText;

    /* getter */
    public boolean isResponse() {
        return response;
    }

    public String getType() {
        return type;
    }

    public String getRegistrationNumFront() {
        return registrationNumFront;
    }

    public String getRegistrationNumBack() {
        return registrationNumBack;
    }

    public String getName() {
        return name;
    }

    public String getVisa() {
        return visa;
    }

    public String getTitle() {
        return title;
    }

    public String getContentFirst() {
        return contentFirst;
    }

    public String getContentSecond() {
        return contentSecond;
    }

    public String getButtonText() {
        return buttonText;
    }

    /* setter */
    public void setResponse(boolean response) {
        this.response = response;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRegistrationNumFront(String registrationNumFront) {
        this.registrationNumFront = registrationNumFront;
    }

    public void setRegistrationNumBack(String registrationNumBack) {
        this.registrationNumBack = registrationNumBack;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVisa(String visa) {
        this.visa = visa;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContentFirst(String contentFirst) {
        this.contentFirst = contentFirst;
    }

    public void setContentSecond(String contentSecond) {
        this.contentSecond = contentSecond;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
