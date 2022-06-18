package com.muyuntage.chatroom;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.UUID;

public class HttpVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new HttpVerticle());
    }

    @Override
    public void start() throws Exception {

        int port = config().getInteger("port", 8888);

        System.out.println("Hello Vert.x");

        EventBus eb = vertx.eventBus();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route("/hello").handler(ctx -> {

            // This handler will be called for every request
            HttpServerResponse response = ctx.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello World from Vert.x-Web!");
        });

        PermittedOptions inboundPermitted1 = new PermittedOptions()
                .setAddress("chatroom");

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(inboundPermitted1)
                .addOutboundPermitted(inboundPermitted1);

        router
                .route("/eventbus/*")
                .subRouter(sockJSHandler.bridge(options));

        router.route().handler(StaticHandler.create());

        server.requestHandler(router).listen(port, serverAsyncResult -> {
            if (serverAsyncResult.succeeded()) {
                System.out.println("启动在" + serverAsyncResult.result().actualPort() + "端口");
            }
        });

//        vertx.setPeriodic(1000, now -> {
//            eb.publish("chatroom",
//                    new JsonObject()
//                            .put("name", "vertx")
//                            .put("content", UUID.randomUUID().toString())
//            );
//        });
    }
}
