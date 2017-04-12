package com.frank.aspect;

import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.model.Article;
import com.frank.model.Document;
import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by frank on 17/4/12.
 */
@Aspect
@Order(5)
@Component
public class BrowseCountAspect {
    private Logger logger = Logger.getLogger(getClass());

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    ThreadLocal<Long> time = new ThreadLocal<>();

    @Pointcut("execution(public * com.frank.controller.BlogController.getArticle(int)) && args(document_id)")
    public void browseCount(int document_id){}

    @Before("browseCount(document_id)")
    public void doBefore(int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        if (document.getShowId() != null){
            Article article = articleMapper.selectByPrimaryKey(document.getShowId());
            article.setBrowseCount(article.getBrowseCount()+1);
            articleMapper.updateByPrimaryKeySelective(article);
        }
    }

}
