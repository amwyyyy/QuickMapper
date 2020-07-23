package com.wen.mapper.dataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.wen.mapper.utils.RandomUtil;

/**
 * 动态选择数据源
 * 
 * @author denis.huang
 * @since 2017年2月15日
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
	/** 主库数据源 */
	private DataSource masterDataSource;

	/** 从库数据源 */
	private List<DataSource> slaveDataSources;

	@Override
	protected Object determineCurrentLookupKey() {
		String dataSource = DynamicDataSourceHolder.getDataSource();

		// 默认情况取主库
		if (dataSource == null) {
			return "master";
		}

		// 随机选择从库
		if (dataSource.equals("slave") && slaveDataSources.size() > 1) {
			dataSource = "slave" + RandomUtil.next(0, slaveDataSources.size() - 1);
		}

		return dataSource;
	}

	private void setTargetDataSources() {
		if (masterDataSource != null && slaveDataSources != null) {
			Map<Object, Object> targetDataSources = new HashMap<>();

			targetDataSources.put("master", masterDataSource);

			// 给从库加上序号，方便选择
			for (int n = 0; n < slaveDataSources.size(); n++) {
				targetDataSources.put("slave" + n, slaveDataSources.get(n));
			}

			super.setTargetDataSources(targetDataSources);
		}
	}

	public void setMasterDataSource(DataSource masterDataSource) {
		this.masterDataSource = masterDataSource;
		setTargetDataSources();
	}

	public void setSlaveDataSources(List<DataSource> slaveDataSources) {
		this.slaveDataSources = slaveDataSources;
		setTargetDataSources();
	}
}