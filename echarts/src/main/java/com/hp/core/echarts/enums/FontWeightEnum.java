/**
 * 
 */
package com.hp.core.echarts.enums;

import java.util.ArrayList;
import java.util.List;

import com.hp.core.common.beans.ValueTextBean;

/**
 * @author huangping
 * 2018年10月9日
 */
public enum FontWeightEnum {

	/**
	 * 正常
	 */
	normal(1),
	
	/**
	 * 粗体
	 */
	bold(2),
	
	/**
	 * 黑体
	 */
	bolder(3),
	
	/**
	 * 淡色的
	 */
	lighter(4);
	
	private FontWeightEnum(Integer value2) {
		this.value2 = value2;
	}
	private Integer value2;
	
	/**
	 * 返回json格式的数据
	 * @return
	 */
	public static List<ValueTextBean> toJSON() {
		List<ValueTextBean> list = new ArrayList<>();
		for (FontWeightEnum e : values()) {
			list.add(new ValueTextBean(e.getValue2(), e.name()));
		}
		return list;
	}

	public Integer getValue2() {
		return value2;
	}
	
}
