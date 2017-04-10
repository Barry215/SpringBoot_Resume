package com.frank.dto;

import java.util.Date;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleForm {

    private String title;

    private String version;

    private Integer hasPublished;

    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getHasPublished() {
        return hasPublished;
    }

    public void setHasPublished(Integer hasPublished) {
        this.hasPublished = hasPublished;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ArticleForm{" +
                "title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", hasPublished=" + hasPublished +
                ", content='" + content + '\'' +
                '}';
    }
}
