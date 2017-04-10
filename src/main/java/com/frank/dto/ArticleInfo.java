package com.frank.dto;

import com.frank.model.Article;

import java.util.List;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleInfo extends ArticleForm {
    private List<String> tagList;

    private int browseCount;

    private int document_id;

    public ArticleInfo(Article article, List<String> tagList) {
        super.setTitle(article.getTitle());
        super.setContent(article.getContent());
        super.setVersion(article.getVersion());
        super.setHasPublished(article.getHasPublished());
        this.browseCount = article.getBrowseCount();
        this.document_id = article.getDocumentId();
        this.tagList = tagList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public int getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(int browseCount) {
        this.browseCount = browseCount;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    @Override
    public String toString() {
        return "ArticleInfo{" +
                "tagList=" + tagList +
                ", browseCount=" + browseCount +
                ", document_id=" + document_id +
                '}';
    }
}
