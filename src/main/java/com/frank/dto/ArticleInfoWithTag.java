package com.frank.dto;

import java.util.List;

/**
 * Created by frank on 17/5/28.
 */
public class ArticleInfoWithTag extends ArticleInfo {
    private String tagName;

    public ArticleInfoWithTag(ArticleWithTag articleWithTag, List<String> tagList, boolean full_content){
        super(articleWithTag,tagList,full_content);
        this.tagName = articleWithTag.getTagName();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return "ArticleInfoWithTag{" +
                "tagName='" + tagName + '\'' +
                '}';
    }
}
