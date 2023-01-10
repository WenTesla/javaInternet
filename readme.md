# 《网络编程技术》大作业

## Java网络编程

src下每个包代表一个实验

大作业在End包下

目前：
大作业内容已经实现

相关的代码和实验要求已经放到对应的包内

### 大作业内容

完成航班显示系统的客户端和航班数据的服务器

客户端能够选择不同的协议和地址和端口进行相关配置。**同时航班数据会动态刷新**。

服务器能够同时配置UDP和TCP的地址和端口，同时也能看到当前航班显示系统的运行状态和连接状态。

其中**默认配置为address=localHost和port=9999**。

#### 客户端：

![image-20230110205359476](https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/12640/image-20230110205359476.png)

输入本地的IP地址和端口号即可开启，也可以选择协议（二选一）

#### 服务器：

![image-20230110205528546](https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/12640/image-20230110205528546.png)

输入本地的IP地址和端口号即可开启，也同时开启UDP和TCP服务。

**此服务器支持并发操作，同时可以显示服务器的连接日志和状态**

如下：

![image-20230110205854719](https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/12640/image-20230110205854719.png)

同时可以在控制面板显示相关服务

并发操作显示如下：

![image-20230110210021095](https://xingqiu-tuchuang-1256524210.cos.ap-shanghai.myqcloud.com/12640/image-20230110210021095.png)







Author: BoWen

Teacher: XiongYuTing

Github: WenTesla

🏫School:CAUC

📮Email:WenTesla@163.com

⏱️Data: 2022/11/21

⏱️UpdatedData:2023/1/10

