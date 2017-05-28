package com.frank.dto;

import com.frank.model.Article;

/**
 * Created by frank on 17/5/28.
 */
public class ArticleWithTag extends Article {
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return "ArticleWithTag{" +
                "tagName='" + tagName + '\'' +
                '}';
    }
}
