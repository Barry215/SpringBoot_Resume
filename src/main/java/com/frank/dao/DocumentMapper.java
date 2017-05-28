package com.frank.dao;

import com.frank.model.Document;
import org.apache.ibatis.annotations.Param;

public interface DocumentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Document record);

    int insertSelective(Document record);

    Document selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Document record);

    int updateByPrimaryKey(Document record);

    int countDocument();

    int checkVersion(@Param("document_id") int document_id,@Param("article_id") int article_id);

    int deleteDocument(Integer id);

    int recoverDocument(Integer id);
}