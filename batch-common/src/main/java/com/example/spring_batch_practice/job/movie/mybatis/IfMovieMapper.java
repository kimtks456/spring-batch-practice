package com.example.spring_batch_practice.job.movie.mybatis;

import com.example.spring_batch_practice.job.movie.domain.IfMovie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IfMovieMapper {

    void upsert(IfMovie movie);

    void deleteAll();

    int countAll();

    IfMovie findByExternalId(@Param("externalId") String externalId);
}
