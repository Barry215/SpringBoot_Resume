package com.frank.dto;

import com.frank.model.Article;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleInfo extends ArticleForm implements Serializable {

    private static final long serialVersionUID = -1L;

    private int article_id;

    private int browseCount;

    private int document_id;

    private Date create_time;

    public ArticleInfo() {
    }

    public ArticleInfo(Article article, List<String> tagList, boolean full_content) {
        super.setTitle(article.getTitle());
        if (article.getContent().length() >= 50 && !full_content){
            super.setContent(article.getContent().substring(0,50));
        }else {
            super.setContent(article.getContent());
        }
        super.setVersion(article.getVersion());
        super.setHasPublished(article.getHasPublished());
        super.setTags(tagList);
        super.setArchive(article.getArchive());
        this.article_id = article.getId();
        this.browseCount = article.getBrowseCount();
        this.document_id = article.getDocumentId();
        this.create_time = article.getCreateTime();
    }

    public int getArticle_id() {
        return article_id;
    }

    public void setArticle_id(int article_id) {
        this.article_id = article_id;
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

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "ArticleInfo{" +
                "article_id=" + article_id +
                ", browseCount=" + browseCount +
                ", document_id=" + document_id +
                ", create_time=" + create_time +
                '}';
    }
}
