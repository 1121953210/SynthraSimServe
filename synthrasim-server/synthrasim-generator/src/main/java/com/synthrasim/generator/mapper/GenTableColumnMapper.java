package com.synthrasim.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.synthrasim.generator.domain.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 代码生成-表字段数据访问层
 */
@Mapper
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {

    /**
     * 根据表名查询所有字段信息
     * 从MySQL的information_schema中读取列信息
     */
    @Select("SELECT column_name AS columnName, " +
            "       column_comment AS columnComment, " +
            "       column_type AS columnType, " +
            "       column_key AS columnKey, " +
            "       extra, " +
            "       is_nullable AS isNullable, " +
            "       ordinal_position AS sort " +
            "FROM information_schema.columns " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName} " +
            "ORDER BY ordinal_position")
    List<Map<String, Object>> selectDbColumnsByTableName(String tableName);
}
