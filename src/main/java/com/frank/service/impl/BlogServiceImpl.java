package com.frank.service.impl;

import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.dao.TagMapper;
import com.frank.dto.ArticleInfo;
import com.frank.dto.JsonResult;
import com.frank.model.Article;
import com.frank.model.Document;
import com.frank.service.BlogService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by frank on 17/4/17.
 */
@Service
public class BlogServiceImpl implements BlogService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private TagMapper tagMapper;

    @Override
    @Cacheable(cacheNames = "show_articleInfo",key = "'id:'+#document_id",unless = "#result == null")
    public ArticleInfo getArticleInfo(int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        if (document.getShowId() != null){
            Article article = articleMapper.selectByPrimaryKey(document.getShowId());
            List<String> tags = tagMapper.selectTagsByArticle(document.getShowId());
            return new ArticleInfo(article,tags,true);
        }else {
            return null;
        }
    }
}
