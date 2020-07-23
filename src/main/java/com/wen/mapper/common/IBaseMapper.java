package com.wen.mapper.common;

import java.io.Serializable;
import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * Mapper基础接口，所有Mapper应该继承于此接口
 * 
 * @author denis.huang
 * @since 2017年2月15日
 * @param <E>
 * @param <Id>
 */
public interface IBaseMapper<E, Id extends Serializable> {
	/**
	 * 插入数据
	 * 
	 * @param po
	 * @return
	 */
	int insert(E po);

	/**
	 * 根据主键更新数据
	 * 
	 * @param po
	 * @return
	 */
	int updateByPrimaryKey(E po);

	/**
	 * 根据主键删除数据
	 * 
	 * @param id
	 * @return
	 */
	int deleteByPrimaryKey(Id id);

	/**
	 * 根据主键查找数据
	 * 
	 * @param id
	 * @return
	 */
	E selectByPrimaryKey(Id id);

	/**
	 * 根据主键数组查找数据
	 * 
	 * @param ids
	 * @return
	 */
	List<E> selectByPrimaryKeys(Id[] ids);

	/**
	 * 根据Example查询数据
	 * 
	 * @param example
	 * @return
	 */
	List<E> selectByExample(Object example);

	/**
	 * 根据Example更新数据
	 * 
	 * @param po
	 * @param example
	 * @return
	 */
	int updateByExample(@Param("record") E po, @Param("example") Object example);

	/**
	 * 根据Example统计数据数量
	 * 
	 * @param example
	 * @return
	 */
	int countByExample(Object example);

	/**
	 * 根据Example删除数据
	 * 
	 * @param example
	 * @return
	 */
	int deleteByExample(Object example);

	/**
	 * 根据Example更新数据，只更新非空的字段
	 * 
	 * @param po
	 * @param example
	 * @return
	 */
	int updateByExampleSelective(@Param("record") E po, @Param("example") Object example);

	/**
	 * 根据主键更新数据，只更新非空字段
	 * 
	 * @param po
	 * @return
	 */
	int updateByPrimaryKeySelective(E po);
}
