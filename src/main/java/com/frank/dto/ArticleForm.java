package com.frank.dto;

import java.util.List;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleForm {

    private String title;

    private String version;

    private Integer hasPublished;

    private String content;

    private String archive;

    private List<String> tags;

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

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ArticleForm{" +
                "title='" + title + '\'' +
                ", version='" + version + '\'' +
                ", hasPublished=" + hasPublished +
                ", content='" + content + '\'' +
                ", archive='" + archive + '\'' +
                ", tags=" + tags +
                '}';
    }
}
