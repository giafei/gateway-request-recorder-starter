## Spring Cloud Gateway组件
可以记录通过gateway转发的请求到日志文件，辅助调试和分析。

配置项 | 含义
------------ | -------------
gateway.log.enable | true或false，是否启用组件.默认true
logging.path | 日志文件夹路径
logging.pattern.file | 日志格式

我当初设计这个功能纯粹是前后端调试时方便撇清后端责任，生产环境慎用。
 
 ### 效果
 
 ```
 ------------开始时间 1533963520775------------
 原始请求：
 GET http://localhost:8080/filter/echo?a=1&b=2
 ------------请求头------------
 cache-control:no-cache
 Postman-Token:3ceae0d1-9f3f-42bc-85c1-ebea10950c46
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 Connection:keep-alive
 ------------ end ------------
 
 代理请求：
 GET http://localhost:4101/echo?a=1&b=2&throwFilter=true
 ------------请求头------------
 cache-control:no-cache
 Postman-Token:3ceae0d1-9f3f-42bc-85c1-ebea10950c46
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 Connection:keep-alive
 ------------ end ------------
 
 响应：200 OK
 ------------响应头------------
 Content-Type:application/json;charset=UTF-8
 Date:Sat, 11 Aug 2018 04:58:40 GMT
 ------------body------------
 {"a":["1"],"b":["2"],"throwFilter":["true"]}
 ------------ end at 1533963520873------------
 ```
 
 ```
 ------------开始时间 1533963577778------------
 原始请求：
 POST http://localhost:8080/filter/echo?a=1&b=2
 ------------请求头------------
 Content-Type:application/json
 cache-control:no-cache
 Postman-Token:69498eea-4270-4ed7-b374-5e15e760cd10
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 content-length:14
 Connection:keep-alive
 ------------body 长度:14 contentType:application/json------------
 {"a":1, "b":2}
 ------------ end ------------
 
 代理请求：
 POST http://localhost:4101/echo?a=1&b=2&throwFilter=true
 ------------请求头------------
 Content-Type:application/json
 cache-control:no-cache
 Postman-Token:69498eea-4270-4ed7-b374-5e15e760cd10
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 content-length:14
 Connection:keep-alive
 ------------body 长度:14 contentType:application/json------------
 {"a":1, "b":2}
 ------------ end ------------
 
 响应：200 OK
 ------------响应头------------
 Content-Type:text/plain;charset=UTF-8
 Content-Length:14
 Date:Sat, 11 Aug 2018 04:59:37 GMT
 ------------body------------
 }2:"b" ,1:"a"{
 ------------ end at 1533963577796------------
 ```
 ```
 ------------开始时间 1533963706176------------
 原始请求：
 GET http://localhost:8080/image/webp
 ------------请求头------------
 cache-control:no-cache
 Postman-Token:01562f0b-9f28-4eda-8095-398991f7d537
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 Connection:keep-alive
 ------------ end ------------
 
 代理请求：
 GET http://httpbin.org:80/image/webp
 ------------请求头------------
 cache-control:no-cache
 Postman-Token:01562f0b-9f28-4eda-8095-398991f7d537
 User-Agent:PostmanRuntime/7.2.0
 Accept:*/*
 Host:localhost:8080
 accept-encoding:gzip, deflate
 Connection:keep-alive
 ------------ end ------------
 
 响应：200 OK
 ------------响应头------------
 Server:gunicorn/19.9.0
 Date:Sat, 11 Aug 2018 05:01:44 GMT
 Content-Type:image/webp
 Content-Length:10568
 Access-Control-Allow-Origin:*
 Access-Control-Allow-Credentials:true
 Via:1.1 vegur
 ------------不记录body------------
 
 ------------ end at 1533963706808------------
 ```