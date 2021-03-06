/**
 * 
 */
package com.hp.core.plugins.intercept;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.core.common.beans.BaseBean;
import com.hp.core.common.utils.MD5Util;
import com.hp.core.redis.HPHashOperations;
import com.hp.core.redis.HPValueOperations;

/**
 * redis缓存插件
 * @author ping.huang
 * 2017年5月17日
 */
public class RedisInterceptPlugin implements AroundInterceptHandle {

	static Logger log = LoggerFactory.getLogger(RedisInterceptPlugin.class);
	
	ThreadLocal<RedisKey> redisKey = new ThreadLocal<>();
	
	@Autowired
	HPValueOperations hpValueOperations;
	@Autowired
	HPHashOperations hpHashOperations;
	
	//key的前缀
	private String keyPrefix = "";
	//键是否md5
	private boolean keyMd5 = true;
	//超时时间（秒）
	private long timeout = 10 * 60;
	
	//是否使用hashRedis
	private boolean useHash = true;
	
	
	@Override
	public Object getReturnValue(Object target, String methodName, Object[] args, Class<?> returnType) {
		if (useHash) {
			String key = keyPrefix + target.getClass().getName() + "." + methodName;
			String field = buildKey(target, methodName, args);
			redisKey.set(new RedisKey(key, field));
			Object obj = hpHashOperations.get(key, field, returnType);
			return obj;
		} else {
			String key = buildKey(target, methodName, args);
			redisKey.set(new RedisKey(key));
			Object obj = hpValueOperations.get(key, returnType);
			return obj;
		}
		
	}
	
	@Override
	public void onAfter(Object target, String methodName, Object[] args, Object result) {
		//设置redis
		RedisKey key = redisKey.get();
		if (useHash) {
			hpHashOperations.put(key.getKey(), key.getField(), result, timeout, TimeUnit.SECONDS);
		} else {
			hpValueOperations.set(key.getKey(), result, timeout, TimeUnit.SECONDS);
		}
		redisKey.remove();
	}

	/**
	 * build key
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 */
	private String buildKey(Object target, String methodName, Object[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append(target.getClass().getName());
		if (StringUtils.isNotEmpty(methodName)) {
			sb.append("_").append(methodName);
		}
		if (ArrayUtils.isNotEmpty(args)) {
			for (Object o : args) {
				if (o == null) {
					continue;
				}
				sb.append("_").append(o.toString());
			}
		}
		log.debug("buildKey with target={}, methodName={}, args={}. with key={}", target, methodName, args, sb);
		if (keyMd5) {
			return keyPrefix + MD5Util.getMD5(sb.toString());
		} else {
			return keyPrefix + sb.toString();
		}
	}
	
	private class RedisKey extends BaseBean {
		
		public RedisKey(String key) {
			super();
			this.key = key;
		}
		
		public RedisKey(String key, String field) {
			super();
			this.key = key;
			this.field = field;
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = -3884841808339557447L;
		private String key;
		private String field;
		public String getKey() {
			return key;
		}
		public String getField() {
			return field;
		}
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public void setKeyMd5(boolean keyMd5) {
		this.keyMd5 = keyMd5;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setUseHash(boolean useHash) {
		this.useHash = useHash;
	}
}
