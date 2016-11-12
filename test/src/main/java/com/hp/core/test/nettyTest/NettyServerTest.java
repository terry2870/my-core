/**
 * 
 */
package com.hp.core.test.nettyTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.core.netty.bean.NettyRequest;
import com.hp.core.netty.server2.NettyServer;
import com.hp.core.netty.server2.NettyServerChannelInboundHandler.NettyProcess;

/**
 * @author huangping
 * 2016年7月24日 下午3:11:25
 */
public class NettyServerTest {

	static Logger log = LoggerFactory.getLogger(NettyServerTest.class);
	

	public static void main(String[] args) {
		log.info("server");
		log.info("===== " + System.getProperty("line.separator"));
		try {
			new NettyServer(9999, new NettyProcess() {
				@Override
				public String process(NettyRequest request) throws Exception {
					log.info("服务端收到请求：{}", request);
					Thread.sleep(10000);
					return "收到了【"+ request.getData() +"】";
				}
			}).init();
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
