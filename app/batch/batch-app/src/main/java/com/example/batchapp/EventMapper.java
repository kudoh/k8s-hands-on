package com.example.batchapp;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EventMapper {

    @Insert("insert into github_events " +
            "values(#{id},#{type},#{createdAt},#{repoName},#{repoUrl},#{author},#{organization})")
    int insert(EventRecord eventRecord);

    @Select("select count(*) from github_events where id = #{id}")
    int countById(long id);
}
