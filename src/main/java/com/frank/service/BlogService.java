package com.frank.service;

import com.frank.dto.ArticleInfo;

/**
 * Created by frank on 17/4/17.
 */
public interface BlogService {
    ArticleInfo getArticleInfo(int document_id);
}
