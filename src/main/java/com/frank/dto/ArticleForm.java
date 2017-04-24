package com.frank.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleForm {

    @NotEmpty(message = "标题不能为空")
    @Length(min = 1,max = 30,message = "标题长度不符合要求")
    private String title;

    @NotEmpty(message = "版本不能为空")
    @Length(min = 1,max = 7,message = "版本长度不符合要求")
    private String version;

    @Range(min = 0, max = 1, message = "是否发布只能是0或1")
    private Integer hasPublished;

    @NotEmpty(message = "内容不能为空")
    @Length(min = 1, message = "内容长度不符合要求")
    private String content;

    @NotEmpty(message = "存档不能为空")
    @Length(min = 1,max = 10,message = "存档长度不符合要求")
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
