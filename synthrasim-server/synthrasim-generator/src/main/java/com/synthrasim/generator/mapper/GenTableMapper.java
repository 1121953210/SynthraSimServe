package com.synthrasim.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.generator.domain.GenTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 代码生成-表信息数据访问层
 */
@Mapper
public interface GenTableMapper extends BaseMapper<GenTable> {

    /**
     * 查询当前数据库中的所有表
     * 从MySQL的information_schema中读取表信息
     */
    @Select("SELECT table_name AS tableName, table_comment AS tableComment, create_time AS createTime, update_time AS updateTime " +
            "FROM information_schema.tables " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_type = 'BASE TABLE' " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectDbTableList();

    /**
     * 根据表名查询表信息
     */
    @Select("SELECT table_name AS tableName, table_comment AS tableComment " +
            "FROM information_schema.tables " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName}")
    Map<String, Object> selectDbTableByName(String tableName);
}
