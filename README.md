# simple-rpc
简易版RPC实现，未来计划魔改。

思路和代码模版来源于汤姆还在写代码
目前v0版本就是代码模版

`TODO LIST`
 - [ ] 编解码：比如上面body中我们是直接用的Java序列化，那要是跨平台怎么办，计划引入protobuf 
 - [ ] RPC协议：协议的字段有很多的，协议版本、传输方式、序列化方式、连接个数等等
 - [ ] 版本兼容：RPC协议的版本兼容问题
 - [ ] 服务治理：整个rpc的运行怎么可靠，比如说客户端请求太多，一台服务器不顶用，IO打满了，再加一台服务器？
 - [ ] 服务注册：服务器怎么才能把自己能够handle的接口告诉客户端，不然自己都不能处理，客户端调用接口，调了也是失败
 - [ ] 服务发现：客户端怎么找到能够调用rpc的服务器的ip和端口？

