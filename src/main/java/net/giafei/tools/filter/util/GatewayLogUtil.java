package net.giafei.tools.filter.util;

import net.giafei.tools.filter.RecorderServerHttpResponseDecorator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * ////////////////////////////////////////////////////////////////////
 * //                          _ooOoo_
 * //                         o8888888o
 * //                         88" . "88
 * //                         (| ^_^ |)
 * //                         O\  =  /O
 * //                      ____/`---'\____
 * //                    .'  \\|     |//  `.
 * //                   /  \\|||  :  |||//  \
 * //                  /  _||||| -:- |||||-  \
 * //                  |   | \\\  -  /// |   |
 * //                  | \_|  ''\---/''  |   |
 * //                  \  .-\__  `-`  ___/-. /
 * //                ___`. .'  /--.--\  `. . ___
 * //              ."" '<  `.___\_<|>_/___.'  >'"".
 * //            | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 * //            \  \ `-.   \_ __\ /__ _/   .-` /  /
 * //      ========`-.____`-.___\_____/___.-`____.-'========
 * //                           `=---='
 * //      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //         佛祖保佑       永无BUG     永不修改
 * ////////////////////////////////////////////////////////////////////
 *
 * @author xjf
 * @version 1.0
 * Date 2018/11/6 15:16
 */

public class GatewayLogUtil {
    private final static String REQUEST_RECORDER_LOG_BUFFER = "RequestRecorderGlobalFilter.request_recorder_log_buffer";

    private static boolean hasBody(HttpMethod method) {
        //只记录这3种谓词的body
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)
            return true;

        return false;
    }

    private static boolean shouldRecordBody(MediaType contentType) {
        String type = contentType.getType();
        String subType = contentType.getSubtype();

        if ("application".equals(type)) {
            return "json".equals(subType) || "x-www-form-urlencoded".equals(subType) || "xml".equals(subType) || "atom+xml".equals(subType) || "rss+xml".equals(subType);
        } else if ("text".equals(type)) {
            return true;
        }

        //form没有记录
        return false;
    }

    private static Mono<Void> doRecordBody(StringBuffer logBuffer, Flux<DataBuffer> body, Charset charset) {
        return DataBufferUtilFix.join(body)
                .doOnNext(wrapper -> {
                    logBuffer.append(new String(wrapper.getData(), charset));
                    logBuffer.append("\n------------ end ------------\n\n");
                    wrapper.clear();
                }).then();
    }

    private static Charset getMediaTypeCharset(@Nullable MediaType mediaType) {
        if (mediaType != null && mediaType.getCharset() != null) {
            return mediaType.getCharset();
        }
        else {
            return StandardCharsets.UTF_8;
        }
    }

    public static Mono<Void> recorderOriginalRequest(ServerWebExchange exchange) {
        StringBuffer logBuffer = new StringBuffer("\n---------------------------");
        exchange.getAttributes().put(REQUEST_RECORDER_LOG_BUFFER, logBuffer);

        ServerHttpRequest request = exchange.getRequest();
        return recorderRequest(request, request.getURI(), logBuffer.append("\n原始请求：\n"));
    }

    public static Mono<Void> recorderRouteRequest(ServerWebExchange exchange) {
        URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
        StringBuffer logBuffer = exchange.getAttribute(REQUEST_RECORDER_LOG_BUFFER);

        return recorderRequest(exchange.getRequest(), requestUrl, logBuffer.append("代理请求：\n"));
    }

    private static Mono<Void> recorderRequest(ServerHttpRequest request, URI uri, StringBuffer logBuffer) {
        if (uri == null) {
            uri = request.getURI();
        }

        HttpMethod method = request.getMethod();
        HttpHeaders headers = request.getHeaders();

        logBuffer
                .append(method.toString()).append(' ')
                .append(uri.toString()).append('\n');

        logBuffer.append("------------请求头------------\n");
        headers.forEach((name, values) -> {
            values.forEach(value -> {
                logBuffer.append(name).append(":").append(value).append('\n');
            });
        });

        Charset bodyCharset = null;
        if (hasBody(method)) {
            long length = headers.getContentLength();
            if (length <= 0) {
                logBuffer.append("------------无body------------\n");
            } else {
                logBuffer.append("------------body 长度:").append(length).append(" contentType:");
                MediaType contentType = headers.getContentType();
                if (contentType == null) {
                    logBuffer.append("null，不记录body------------\n");
                } else if (!shouldRecordBody(contentType)) {
                    logBuffer.append(contentType.toString()).append("，不记录body------------\n");
                } else {
                    bodyCharset = getMediaTypeCharset(contentType);
                    logBuffer.append(contentType.toString()).append("------------\n");
                }
            }
        }


        if (bodyCharset != null) {
            return doRecordBody(logBuffer, request.getBody(), bodyCharset);
        } else {
            logBuffer.append("------------ end ------------\n\n");
            return Mono.empty();
        }
    }

    public static Mono<Void> recorderResponse(ServerWebExchange exchange) {
        RecorderServerHttpResponseDecorator response = (RecorderServerHttpResponseDecorator)exchange.getResponse();
        StringBuffer logBuffer = exchange.getAttribute(REQUEST_RECORDER_LOG_BUFFER);

        HttpStatus code = response.getStatusCode();
        if (code == null) {
            logBuffer.append("返回异常").append("\n------------ end ------------\n\n");
            return Mono.empty();
        }

        logBuffer.append("响应：").append(code.value()).append(" ").append(code.getReasonPhrase()).append('\n');

        HttpHeaders headers = response.getHeaders();
        logBuffer.append("------------响应头------------\n");
        headers.forEach((name, values) -> {
            values.forEach(value -> {
                logBuffer.append(name).append(":").append(value).append('\n');
            });
        });

        Charset bodyCharset = null;
        MediaType contentType = headers.getContentType();
        if (contentType == null) {
            logBuffer.append("------------ contentType = null，不记录body------------\n");
        } else if (!shouldRecordBody(contentType)) {
            logBuffer.append("------------不记录body------------\n");
        } else {
            bodyCharset = getMediaTypeCharset(contentType);
            logBuffer.append("------------body------------\n");
        }

        if (bodyCharset != null) {
            return doRecordBody(logBuffer, response.copy(), bodyCharset);
        } else {
            logBuffer.append("\n------------ end ------------\n\n");
            return Mono.empty();
        }
    }

    public static String getLogData(ServerWebExchange exchange) {
        StringBuffer logBuffer = exchange.getAttribute(REQUEST_RECORDER_LOG_BUFFER);
        return logBuffer.toString();
    }
}
