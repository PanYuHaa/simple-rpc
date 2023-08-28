# simple-rpc
简易版RPC实现，未来计划魔改。

思路和代码模版来源于b站up主汤姆还在写代码和xhyOvO
目前v0版本就是代码模版

`TODO LIST`
 - [x] 编解码：比如上面body中我们是直接用的Java序列化，那要是跨平台怎么办，计划引入protobuf 
 - [x] RPC协议：丰富协议字段，自定义线程池拒绝策略触发容错策略，优化IO流关闭释放资源
 - [x] 拦截器：提供更丰富的扩展
 - [x] SPI机制：组件化
 - [ ] 版本兼容：RPC协议的版本兼容问题
 - [x] 服务治理：故障转移、超时控制
 - [x] 服务注册：服务器怎么才能把自己能够handle的接口告诉客户端，不然自己都不能处理，客户端调用接口，调了也是失败
 - [x] 服务发现：客户端怎么找到能够调用rpc的服务器的ip和端口？

## 项目架构
![架构](https://github.com/PanYuHaa/simple-rpc/blob/master/assets/RPC%E6%A1%86%E6%9E%B6.png)
