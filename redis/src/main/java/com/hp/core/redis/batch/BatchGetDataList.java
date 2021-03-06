/**
 * 
 */
package com.hp.core.redis.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.hp.core.common.utils.MapUtil;
import com.hp.core.common.utils.MapUtil.FunctionExPlus;
import com.hp.core.redis.HPValueOperations;
import com.hp.core.common.utils.SpringContextUtil;
import com.hp.core.common.utils.StringUtil;

/**
 * 批量从缓存或数据库中查询数据
 * @author huangping
 * 2018年12月29日
 */
public class BatchGetDataList {
	
	/**
	 * 批量查询
	 * @param <T>			缓存不命中时，从数据库批量获取时返回的对象
	 * @param <K>			最终返回的map的key
	 * @param <V>			最终返回的map的value
	 * @param idList
	 * @param function
	 * @param params
	 * @return
	 */
	public static final <T, K, V> Map<K, List<V>> batchGetForMapList(List<K> idList, BatchGetListOperate<T, K, V> function, Object... params) {
		if (CollectionUtils.isEmpty(idList)) {
			return null;
		}
		
		//返回对象
		Map<K, List<V>> map = new HashMap<>();
		
		//组装redis key
		List<String> keys = new ArrayList<>();
		for (K id : idList) {
			keys.add(function.buildKey(id, params));
		}
		//批量查询缓存
		HPValueOperations redisValueHelper = SpringContextUtil.getBean(HPValueOperations.class);
		List<K> missUidList = new ArrayList<>();//未命中缓存的id
		List<List<V>> valueList = redisValueHelper.multiGetList(keys, function.getValueClass());
		if (CollectionUtils.isNotEmpty(valueList)) {
			for (int i = 0; i < valueList.size(); i++) {
				if (valueList.get(i) == null) {
					//未命中的，合在一起
					missUidList.add(idList.get(i));
				} else {
					//命中的，放入返回对象
					map.put(idList.get(i), valueList.get(i));
				}
			}
		} else {
			//全都未命中
			missUidList.addAll(idList);
		}
		
		if (CollectionUtils.isEmpty(missUidList)) {
			//没有未命中的，则直接返回
			return map;
		}
		
		//未命中的，批量查询数据库
		List<T> list = function.batchDataFromDatabase(missUidList, params);
		if (CollectionUtils.isEmpty(list)) {
			return map;
		}
		
		Map<K, List<V>> newMap = MapUtil.transformListMap(list, new FunctionExPlus<T, K, V>() {
			@Override
			public K applyKey(T input) {
				return function.getKey(input);
			}

			@Override
			public V applyValue(T input) {
				return function.getValue(input);
			}
		});
		
		if (MapUtils.isEmpty(newMap)) {
			return map;
		}
		
		//保存到redis
		for (Entry<K, List<V>> entry : newMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
			redisValueHelper.set(function.buildKey(entry.getKey(), params), entry.getValue(), function.getCacheTimeMinutes(), function.getCacheTimeUnit());
		}
		
		return map;
	}
	
	/**
	 * 批量获取数据的操作方法
	 * @author huangping
	 * 2019年1月24日
	 * @param <T>	缓存不命中时，从数据库批量获取时返回的对象
	 * @param <K>	最终返回的map的key
	 * @param <V>	最终返回的map的value
	 */
	public static interface BatchGetListOperate<T, K, V> {
		
		/**
		 * 获取key的前缀
		 * @param params
		 * @return
		 */
		public String getKeyPrefix(Object... params);
		
		/**
		 * 构造key
		 * @param id
		 * @param params
		 * @return
		 */
		public default String buildKey(K id, Object... params) {
			return StringUtil.getKey(getKeyPrefix(params), id);
		}
		
		/**
		 * 批量从数据库查询
		 * @param idList
		 * @param params
		 * @return
		 */
		public List<T> batchDataFromDatabase(List<K> idList, Object... params);
		
		/**
		 * 从数据库返回对象中获取key
		 * @param t
		 * @return
		 */
		public K getKey(T t);
		
		/**
		 * 从数据库返回对象中获取value
		 * @param t
		 * @return
		 */
		public V getValue(T t);
		
		/**
		 * 返回list里面的对象
		 * @return
		 */
		public Class<V> getValueClass();
		
		/**
		 * 缓存时间
		 * @return
		 */
		public default long getCacheTimeMinutes() {
			return 10;
		}
		
		/**
		 * 获取缓存的单位
		 * @return
		 */
		public default TimeUnit getCacheTimeUnit() {
			return TimeUnit.MINUTES;
		}
	}
}
