/**
 * 
 */
package com.hp.core.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author huangping
 * 2016年7月24日 下午1:51:49
 */
public class NettyServerChannelInitialier extends ChannelInitializer<SocketChannel> {

	static Logger log = LoggerFactory.getLogger(NettyServerChannelInitialier.class);

	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		log.info("已经启动监听");
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
		pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
		pipeline.addLast(this.new NettyServerDispatchHandler());
	}
	
	public class NettyServerDispatchHandler extends SimpleChannelInboundHandler<String> {


		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
			log.info("服务端收到消息。 msg={}", msg);
			//Response
			ctx.channel().writeAndFlush("yes, server is accepted you ,nice !"+msg);
		}
		

	}

}
