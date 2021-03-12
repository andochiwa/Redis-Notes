# Redis Study Notes

|        1        |              2               |           3            |         4          |
| :-------------: | :--------------------------: | :--------------------: | :----------------: |
| [入门简介](#1)  | [五大数据类型与基础语法](#2) | [持久化(RDB, AOF)](#3) | [配置文件拆解](#4) |
|        5        |              6               |           7            |         8          |
| [redis事务](#5) |        [发布订阅](#6)        |     [主从复制](#7)     |   [连接java](#8)   |



## <span id="1">1.入门简介</span>

1. redis为单进程，I/O多路复用
2. 默认16个数据库，从0开始，默认使用0号库, select x 命令切换数据库
3. 一些常用命令
   * DBSIZE 查看当前数据库的key的数量
   * FLUSHDB 清空当前库
   * FLUSHALL 删库跑路
   * KEYS xx 查看当前库的keys，在后面加上匹配内容



## <span id="2">2.Redis数据类型</span>

[redis数据类型操作命令大全](http://redisdoc.com/)

|       1       |      2      |     3      |      4      |      5      |
| :-----------: | :---------: | :--------: | :---------: | :---------: |
| [String](#21) | [List](#22) | [Set](#23) | [Hash](#24) | [Zset](#25) |



### key(键)

* keys *：查找key， *表示怎么匹配
* exists key： 判断某个key是否存在
* move key db：移动key到db库
* expire key time：为key设置过期时间time(秒)
* ttl key：查看key的过期时间，-1永不过期，-2已过期
* type key：查看key类型

### <span id="21">1.String(字符串)</span>

最基本的数据类型，redis中字符串的value最多可以是512M

#### set；get；del；append；strlen

* 设置值
* 获取值
* 删除key
* 给key的value后面添加字符串
* 获取元素个数

#### incr；decr；incrby；decrby(必须是数字)

* 加1
* 减1
* 加n
* 减n

#### getrange；setrange

* 获取指定区间范围内的值**类似于between...and**；
* 设置指定区间范围内的值——**setrange key range n**(range表示从何开始)

#### setex；setnx

* 创建key时设置key的存活时间——**setex key time value**；
* 如果key不存在才设置(set if not exist)

#### mset；mget；msetnx(more)

​	设置多个，**特别注意msetnx必须得全部都不存在时才能设置**

### <span id="22">2.List(列表)</span>

双向链表

#### lpush；rpush；lrange

* 往list左边插入值；
* 右边插入值；
* 从左边遍历

#### lpop；rpop

* 左边的pop；
* 右边的pop

#### lindex；llen；lrem；ltrim

* 从左往右索引位置取值(lindex key n)；
* 获取长度；
* 删n个value——**lrem key n value**；
* 取n到m范围的value赋给key——**ltrim key n m**

#### rpoplpush

* key1从右边pop一个元素，push进k2的左边——**rpoplpush key1 key2**

#### lset；linsert

* 设置key的第n个位置的值为value——**lset key n value**；
* 往key的value1的 左边/右边 插入value2——**linsert key before/after value1 value2**

### <span id="23">3.Set(无序集合)</span>

String类型的无序集合，**不允许重复**

#### sadd；smembers；sismember

* 添加；
* 遍历；
* 查看是否有元素value——**sismember set value**

#### scard；srem；srandmember

* 获取集合元素个数；
* 删除集合中的元素

#### srandmember；spop；smove

* 从已有元素中随机抽n个元素——**srandmember set n**；
* 随机出栈；
* 把key1里值value1赋给key2——**smove key1 key2 value1**

#### 数学集合类：sdiff；sinter；sunion

* 差集key1 - key2；
* 交集；
* 并集

### <span id="24">4.Hash(哈希表)</span>

kv模式不变，但是v是个键值对

String类型的Field和Value的映射表，类似Map<String, Object>

#### hset；hget；hmset；hmget；hgetall；hdel（重要）

* 设置hash k的值为键值对v c——**hset k v c**
* 获取k的值的键v的值c——**hget k v**
* 设置多个键值对——**hmset k v1 c1 v2 c2 ...**
* 获取多个k的值的键的值
* 获取所有vc
* 通过v删除掉k的一组键值对——**hdel k v**

#### hkeys；hvals（重要）

* 获取所有v；
* 获取所有c

#### hlen；hexists

* 获取长度；
* 查看k里面的v是否存在

#### hincrby；hincrbyfloat；hsetnx

* 给c的值v加n——**hincrby k c n**；
* n为小数；
* 给已存在的c的值赋值为v

### <span id="25">5.Zset(sorted set: 有序集合)</span>

String类型的有序集合，不允许重复

**会关联一个double类型的分数(score，相当于优先级)，按照分数排序**

#### zadd；zrange；zrangebyscore

* 创建，添加值——**zadd zset value1 score1 value2 score2 ...**；
* 遍历，加上withscores可以把score也遍历出来；
* 找出闭区间n到m的score的值——**zrangebyscore zset n m**(加上limit和mysql一个意思)

#### zrem；zcard；zcount；  zrank；zscore；zrevrank；  zrevrange

* 删掉value；
* 统计个数（不包含score）；
* 统计score闭区间内个数；
* 找出索引位置；
* 找出score对应的值；
* 逆序找出索引位置；
* 逆序遍历；
* 逆序根据区间遍历

## <span id="3">3.持久化</span>

### 1.RDB(Redis Database)

在指定的时间间隔内，将内存中的数据集快照写入磁盘，**即Snapshot快照**，它恢复时是将快照文件直接读取到内存中

Redis会单独创建(fork)一个子进程来进行持久化，会先将数据写入到一个临时文件中，待持久化过程都结束了，再用这个临时文件替换上次持久化好的文件。**整个过程中，主进程不进行任何的IO操作，这就确保了极高的性能**。

如果需要进行大规模数据的恢复，并且对数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加高效。RDB的缺点是最后一次持久化后数据可能丢失

* **Fork** 作用是复制一个与当前进程一样的进程。新进程的所有数据（变量，程序计数器等）数值都和原进程一致，但是是一个全新的进程，并作为原进程的子进程
* **触发方法** 
  * 配置文件中配置触发方法
  * 使用命令save（同步）或者bgsave（异步）
  * 执行flushall命令（删库跑路）
* 恢复方法 将备份文件移动到redis安装目录并启动服务即可config get dir获取目录
* **优势**
  * 适合大规模的数据恢复
  * 对数据完整性和一致性要求不高
* **劣势**
  * 因为在一定间隔时间做备份，所以如果redis意外down了的话，就会丢失最后一次快照的所有修改
  * fork的时候，内存中的数据被克隆了一份，膨胀性需要考虑
* **停止方法** redis-cli config set save "" 或者配置文件配置



### 2.AOF(Append Only File)

**以日志的形式来记录每个写操作**，将Redis执行过的所有写指令记录下来（读操作不记录），只许追加文件但不可以改写文件，redis启动之初会读取该文件重新构建数据。

换言之，redis重启的话就根据日志文件的内容将写指令从前到后执行一次以完成数据的恢复工作。

**可以和RDB文件并存，优先执行AOF**

* **触发方法** 配置文件配置
* **恢复方法**
  * **正常恢复** 重启redis后重新加载aof文件
  * **异常恢复** 备份被写坏的aof，用Redis-check-aof --fix进行修复，然后重新加载
* **Rewrite**
  * **简介** Aof采用文件追加方式，文件会越来越大，为避免出现这种情况新增的重写机制。当AOF文件的大小超过设置的阈值时，Redis就会启动AOF文件的内容压缩，只保留可以恢复数据的最小指令集，可以使用命令bgrewriteaof
  * **原理** AOF文件增长过大时，会fock出一条新进程来将文件重写（先写临时文件再rename），遍历新进程的内存中数据，每条记录有一条set语句。**重写aof文件的操作，并没有读取旧的aof文件，而是将整个内存中的数据库内容的命令的方式重写了一个新的aof文件。**
  * **触发机制** Redis会记录上次重写时的AOF文件大小，默认配置是当AOF文件大小是上次重写后大小的一倍且文件大于64M时触发

* **优势**
  * **每秒同步** appendfsync always 同步持久化，每次发生数据变更会立即记录到磁盘，性能较差但数据完整性比较好
  * **每修改同步** everysec 异步操作，每秒记录，如果一秒内宕机，可能有数据丢失
  * **不同步** no
* **劣势**
  * 相同的数据集的数据而言，AOF文件要远大于RDB文件，恢复速度也比RDB慢
  * 运行效率慢于rdb，每秒同步策略效率较好，不同步效率和rdb相同



## <span id="4">4.配置文件拆解</span>

#### redis.conf

* **includes** 可以包含其他配置文件，redis.conf相当于一个总闸

* **GENERAL** 通用

  * **daemonize** 是否设置为守护进程，在后台运行，默认为false
  * **pidfile** 当Redis以守护进程方式运行时，即使该项没有配置，Redis也会默认把pid写入/var/run/redis.pid文件；而当Redis不是以守护进程凡是运行时，若该项没有配置，则redis不会创建pid文件。创建pid文件是尝试性的动作，即使创建写入失败，redis依然可以启动运行
  * **port** 指定Redis的监听窗口，默认为6379
  * **tcp-backlog** 连接队列，队列总和为 = 未完成三次握手队列 + 已完成三次握手队列（network）
  * **bind** 绑定主机地址，不做限制的话就为0.0.0.0
  * **timeout** 空闲多少秒后关闭连接，0为不关闭
  * **Tcp-keepalive** 每n秒一次进行Keepalive检测，发送一个ping查看网络状态，0为不检测（建议60？）
  * **loglevel** 日志级别
    * **debug** 开发测试时使用
    * **verbose** 测试阶段
    * **notice** 默认
    * **warning** 最高级别
  * **logfile** 日志路径
  * **databases** redis库的数量，默认为0

* **snapshotting** RDB相关快照配置

  * **save <seconds> <changes>** 指定在多长时间内，有多少次更新操作，就将数据同步到数据文件，**可以多个条件配合**，Redis默认配置文件中提供了三个条件：

    * save 900 1
    * save 300 10
    * save 60 10000

     分别表示900秒内有1个更改，300秒内有10个更改，60秒内有10000个更改。

  * **stop-writes-on-bgsave-error** 如果以RDB的方式持久化数据时出错了，redis默认将不接收写入操作，以便让应用层感知出现问题了，配置为no表示你不在乎数据不一致或者有其他手段控制

  * **rdbcompression** 指定存储至本地数据库时是否压缩数据，默认为yes，Redis采用LZF压缩，如果为了节省CPU时间，可以关闭该选项，但会导致数据库文件变的巨大

  * **dir** 指定快照的存放目录

* append only mode AOF相关配置

  * **appendonly** 开启AOF，默认为no
  * **appendfsync** 将数据写入磁盘的方式
    * **no** 不及时同步，由操作系统控制何时写入，速度最快
    * **always** 每次只写日志，性能较差，但最安全
    * **everysec** 默认，每秒钟同步一次，折中的方法，异步操作
  * **no-appendfsync-on-rewrite** appendfsync为always或everysec的时候，因为要在后台执行大量的IO操作，可能会导致阻塞很久，为了缓解这个问题，可以将这项变为true，它将会在有一个BGSAVE或BGREWRITEAOF正在运行时，阻止主进程调用fsync()，但可能不安全，一般为no即可。
  * **auto-aof-rewrite-percentage** 设置重写的**百分比**基准值，当AOF日志的大小根据指定的百分比增长时，Redis会暗中调用BGREWRITEAOF去自动重写日志文件，设置百分比为0则禁用
  * **auto-aof-rewrite-min-size** 设置重写的**大小**基准值，默认为64mb
  * **aof-load-truncated** 当发生AOF文件在redis启动中被截断，如果设置为yes，则被截断的AOF文件将加载，并且redis发送一个日志告知用户。如果为no则发送一个错误并且中断启动

* **security** 安全（密码相关）
  * **requirepass** 设置Redis连接密码
  * **rename-command CONFIG** 更改config命令的名字
* **clients** 客户端
  
  * **maxclients** 最大客户端数量 默认10000
* **memory management** 内存管理
  * **maxmemory <bytes>** 最大内存 
  * **maxmemory-policy** 缓存淘汰策略
    * **volatile-lru** 对设置了过期时间的key使用`LRU算法`移除
    * **allkeys-lru** 对所有键都采取`LRU算法`移除
    * **volatile-lfu** 从设置了过期时间的key中移除使用频率最少的key
    * **allkeys-lfu** 对所有key中移除使用频率最少的key
    * **volatile-random** 在过期集合中移除设置了过期时间中的随机的key
    * **allkeys-random** 移除随机的key
    * **volatile-ttl** 移除TTL值最小的key（即将淘汰的key）
    * **noeviction** 默认 永不过期，如果超过maxmemory则返回错误响应
  * **maxmemory-samples** 设置淘汰策略的样本信息数量
* **lazy freeing** 延迟释放
  * **lazyfree-lazy-eviction** 针对redis内存使用达到maxmemory且设置了淘汰策略时，在被动淘汰key时是否采用lazy free机制，**可能导致内存超用**， 默认为no
  * **lazyfree-lazy-expire** 针对设置有TTL的key，达到过期时间后，被redis清理删除时是否采用lazy free机制，**建议开启**， 默认关闭
  * **lazyfree-lazy-server-del** 针对有些指令在处理已存在的key时，会带有隐式的del key操作，如果这些key是个big key，就可能导致阻塞删除的性能问题，开启此参数可以解决
  * **slave-lazy-flush** 针对slave进行全量数据同步，slave在加载master的RDB文件前，会运行flushall来清理自己的数据场景，参数设置决定是否采用异常flush机制，如果内存变动不大可开启



## <span id="5">5.事务</span>

* **简介** Redis的事务是可以一次执行多个命令，本质为一组命令的几何。一个事务中的所有命令都会被序列化，按顺序的串行化执行而不会被其他命令插入（一个队列中，一次性、顺序性、排他性的执行一系列命令）
* **常用命令**
  * **discard** 取消事务，放弃执行事务块内的所有命令
  * **exec** 执行所有事务块内的命令
  * **multi** 标记一个事务的开始
  * **unwatch** 取消watch命令对所有key的监视
  * **watch key[key ...]** 监视一个或多个key，如果在事务执行前这些key被其他命令所改动，那么事务将被中断
* **watch监控**
  * **悲观锁、乐观锁、CAS** 
    * **悲观锁(Pessimistic lock)** 很悲观，每次拿数据都认为别人会修改，所以在拿数据时都会上锁，这样别人想拿这个数据就会阻塞知道它拿到数据
    * **乐观锁(Optimistic lock)** 很乐观，每次拿数据都认为别人不会修改，**所以不会上锁**，但是在更新时会判断在此期间有没有其他人去更新这个数据，可以用版本号等机制。比较适用于读操作多的应用类型，可以提高吞吐量
  * watch即类似于乐观锁，当watch过后，在数据更新期间被别人修改过数据，则提交事务将会中止，整个事务队列都不会被执行
* **特性**
  * **单独的隔离操作** 事务中所有命令都会被序列化，按顺序执行，执行过程中不会被其他命令打断
  * **没有隔离级别的概念** 队列中的命令都没有被实际提交，因为事务提交前任何指令都不会被实际执行，所以也不存在隔离级别
  * **不保证原子性** redis同一个事务中如果有一条命令执行失败，其他命令仍然会被执行，没有回滚



## <span id="6">6.发布订阅(一般都交给专业的消息中间件去做)</span>

* **简介** redis的发布订阅是进程间的一种消息通信模式，发布者(pub)发送消息，订阅者(sub)接收消息
* **命令**
  * **psubscribe pattern[pattern ...]** 订阅一个或多个符合给定模式的频道
  * **pubsub subcommand[argument [argument ...]]** 查看订阅与发布系统状态
  * **publish channel message** 将信息发送到指定频道
  * **punsubscribe [pattern [pattenr ...]]** 退订所有给定模式的频道
  * **subscribe channel [channel ...]** 订阅给定的一个或多个频道信息
  * **unsubscribe [channel [channel ...]]** 退订给定的频道

## <span id="7">7.复制(主从复制)</span>

* **简介** 主从复制，主机数据更新后根据配置和策略，自动同步到备机的master/slaver机制，master以写为主，slaver以读为主

* **作用**

  * **读写分离**
  * **容灾恢复**

* **用法**

  * **配从不配主** 
  * **从库配置：slaveof主库ip主库端口**
    * 每次与master断开之后，都需要重新配置，除非配置进配置文件中
    * info replication
  * **修改配置文件的细节操作**
    * 拷贝多个redis.conf配置文件
    * daemonize yes
    * pid文件名字
    * 指定端口号
    * log文件名字
    * Dump.rdb名字
  * **常用使用方法**
    * **一主二仆** 一个master两个slave
    * **薪火相传**
      * 上一个slave可以是下一个slave的master，Slave同样可以接受其他slaves的连接和同步请求，那么该slave作为了链条中下一个的master，可以有效减轻master的写压力
      * 中途变更转向：会清除之前的数据，重新建立拷贝新的
      * Slaveof 新主库ip 新主库端口
    * **反客为主**
      * **SLAVEOF no one** 使当前数据库停止与其他数据库的同步，转成主数据库

* **复制原理**

  * slave启动成功连接到master后会发送一个sync命令
    master接到命令启动后台的存盘进程，同时收集所有接收到的用于修改数据集的命令，在后台进程执行完毕之后，master将传送整个数据文件到slave，以完成一次同步

  * **全量复制** slave服务在接收到数据库文件数据后，将其存盘并加载到内存中
  * **增量复制** master继续将新的所有收集到的修改命令依次传给slave，完成同步
  * 只要是重新连接master，全量复制将被自动执行

* **哨兵模式**

  * **简介** **反客为主**的自动版，可以后台监控主机是否故障，如果故障了则根据投票数自动将从库变成主库
  * 使用步骤
    * 配置目录下新建sentinel.conf文件，名字必须一致
    * **配置哨兵**
      * **sentinel monitor**  <master-name> <ip> <redis-port> <quorum>
      * 最后一位数字，表示主机挂掉后slave投票看让谁接替主机，得票数多少后成为主机
    * 启动哨兵
      * **Redis-sentinul /xxx/sentinel.conf** 配置文件目录
  * 如果原有的master挂了，则投票从slave里新选一个master
    如果之前的master回来了，则会变为新master的slave



## <span id="8">8.Java连接</span>

用到Jedis jar包

```java
public class RedisConnectionTest {
    public static void main(String[] args){
        Jedis jedis = new Jedis("localhost", 6379);
        System.out.println(jedis.ping());
    }
}
```