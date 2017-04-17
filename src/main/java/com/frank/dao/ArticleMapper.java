package com.frank.dao;

import com.frank.model.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKeyWithBLOBs(Article record);

    int updateByPrimaryKey(Article record);

    List<Article> selectEditArticle();

    List<Article> selectDocuments(Integer offset);

    List<Article> selectHistoryArticles(Integer document_id);

    List<Article> selectArchiveDocuments(Integer offset);

    int selectArchiveCount();

    Integer selectArticleByVersion(@Param("document_id") int document_id, @Param("version") String version);
}