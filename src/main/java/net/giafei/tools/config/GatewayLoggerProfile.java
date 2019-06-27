package net.giafei.tools.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
 * Date 2018/8/13 17:20
 */

@Component
@ConditionalOnProperty(value = "gateway.log.enable", matchIfMissing = true)
public class GatewayLoggerProfile {
    public static final String LOGGER_NAME = "requestRecorder";

    @Value("${logging.path}")
    private String logPath;

    @Value("${logging.pattern.file}")
    private String logPattern;

    @PostConstruct
    public void onReady() {
        LoggerContext context = (LoggerContext)StaticLoggerBinder.getSingleton().getLoggerFactory();

        Logger logger = context.getLogger(LOGGER_NAME);
        logger.detachAndStopAllAppenders();

        String logFile = null;
        if (logPath.endsWith("/"))
            logFile = logPath + "pv/pv.log";
        else
            logFile = logPath + "/pv/pv.log";

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setFile(logFile);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(logPattern);
        encoder.setContext(context);
        encoder.setParent(appender);
        appender.setEncoder(encoder);
        encoder.start();

        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy();
        policy.setCleanHistoryOnStart(false);
        policy.setMaxHistory(30);
        policy.setContext(context);
        policy.setFileNamePattern(logFile + ".%d{yyyy-MM-dd}.gz");
        policy.setParent(appender);
        appender.setRollingPolicy(policy);
        policy.start();

        appender.setName(LOGGER_NAME + "-appender");
        appender.setContext(context);
        appender.start();

        logger.addAppender(appender);

        logger.setAdditive(false);
    }
}
