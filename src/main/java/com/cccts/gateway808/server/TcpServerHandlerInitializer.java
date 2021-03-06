package com.cccts.gateway808.server;

import com.cccts.gateway808.Config;
import com.cccts.gateway808.server.inboundhandlers.*;
import com.cccts.gateway808.server.outboundhandlers.Message808Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;


public class TcpServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    final ByteBuf delimiter1 = Unpooled.copiedBuffer(new byte[]{0x7E});
    final ByteBuf delimiter2 = Unpooled.copiedBuffer(new byte[]{0x7E, 0x7E});

//    final LogInboundHandler logInboundHandler = new LogInboundHandler();

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {



        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast(new LoggingHandler(LogLevel.ERROR));
        /**
         * 编码808消息并记录回复日志
         */
        pipeline.addLast("message808Encoder", new Message808Encoder());

        /**
         * 超时检测(in1)
         */
        pipeline.addLast("idleStateHandler", new IdleStateHandler(Config.TERMINAL_OVERTIME, 0, 0));

        /**
         * 粘包，拆包处理(in2)
         */
        pipeline.addLast("delimiterBasedFrameDecoder", new DelimiterBasedFrameDecoder(1024,true,delimiter1, delimiter2));

        /**
         * 记录收到消息(in3)
         */
        pipeline.addLast("logInboundHandler", new LogInboundHandler());

        /**
         * 解析808消息(in5)
         */
        pipeline.addLast("message808Decoder", new Message808Decoder());


        /**
         *  终端状态处理(in6)
         */
        pipeline.addLast("SessionHandler", new SessionHandler());

        /**
         * 回复消息(in7)
         */
        pipeline.addLast("responseHandler", new ResponseHandler());
    }
}
