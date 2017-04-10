package com.frank.dao;

import com.frank.model.TagRela;

public interface TagRelaMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TagRela record);

    int insertSelective(TagRela record);

    TagRela selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TagRela record);

    int updateByPrimaryKey(TagRela record);
}