package com.frank.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank on 17/5/28.
 */
public class ArticleWithTime {
    private String time;
    private List<ArticleInfo> articleInfoList;

    public ArticleWithTime() {
        time = "";
        articleInfoList = new ArrayList<>();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<ArticleInfo> getArticleInfoList() {
        return articleInfoList;
    }

    public void setArticleInfoList(List<ArticleInfo> articleInfoList) {
        this.articleInfoList = articleInfoList;
    }

    @Override
    public String toString() {
        return "ArticleWithTime{" +
                "time='" + time + '\'' +
                ", articleInfoList=" + articleInfoList +
                '}';
    }
}
