package com.frank.dao;

import com.frank.dto.ArticleWithTag;
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

    List<Article> selectSavedArticle();

    List<Article> selectDocuments();

    List<Article> selectHistoryArticles(Integer document_id);

    List<Article> selectArchiveDocuments();

    List<ArticleWithTag> selectTagDocuments();

    List<Article> selectArticlesInArchive(String archive);

    List<Article> selectArticlesInTag(String tag);

    List<String> selectArchiveList();

    int selectArchiveCount();

    Integer selectArticleByVersion(@Param("document_id") int document_id, @Param("version") String version);

    List<Article> selectArticleLikeTitle(String title);

    List<Article> selectArticleLikeContent(String content);
}