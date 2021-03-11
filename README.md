# Redis Study Notes

## 1.入门

1. redis为单进程，I/O多路复用
2. 默认16个数据库，从0开始，默认使用0号库, select x 命令切换数据库
3. 一些常用命令
   * DBSIZE 查看当前数据库的key的数量
   * FLUSHDB 清空当前库
   * FLUSHALL 删库跑路
   * KEYS xx 查看当前库的keys，在后面加上匹配内容



## 2.redis数据类型

[redis数据类型操作命令大全](http://redisdoc.com/)

### key(键)

* keys *：查找key， *表示怎么匹配
* exists key： 判断某个key是否存在
* move key db：移动key到db库
* expire key time：为key设置过期时间time(秒)
* ttl key：查看key的过期时间，-1永不过期，-2已过期
* type key：查看key类型

### String(字符串)

最基本的数据类型，redis中字符串的value最多可以是512M

#### set；get；del；append；strlen

​	设置值；获取值；删除key；给key的value后面添加字符串；获取值

#### incr；decr；incrby；decrby(必须是数字)

​	加1；减1；加n；减n

#### getrange；setrange

​	获取指定区间范围内的值**(类似于between...and)**；设置指定区间范围内的值**(setrange key range n)**(range表示从何开始)

#### setex；setnx

​	创建key时设置key的存活时间**(setex key time value)**；如果key不存在才设置(set if not exist)

#### mset；mget；msetnx(more)

​	设置多个，**特别注意msetnx必须得全部都不存在时才能设置**

### List(列表)

双向链表

#### lpush；rpush；lrange

​	往list左边插入值；右边插入值；从左边遍历

#### lpop；rpop

​	左边的pop；右边的pop

#### lindex；llen；lrem；ltrim

​	从左往右索引位置取值(lindex key n)；获取长度；

​	删n个value**(lrem key n value)**；取n到m范围的value赋给key**(ltrim key n m)**

#### rpoplpush

​	key1从右边pop一个元素，push进k2的左边**(rpoplpush key1 key2)**

#### lset；linsert

​	设置key的第n个位置的值为value**(lset key n value)**；

​	往key的value1的 左边/右边 插入value2**(linsert key before/after value1 value2)**

### set(无序集合)

String类型的无序集合，**不允许重复**

#### sadd；smembers；sismember

​	添加；遍历；查看是否有元素value**(sismember set value)**

#### scard；srem；srandmember

​	获取集合元素个数；删除集合中的元素

#### srandmember；spop；smove

​	从已有元素中随机抽n个元素**(srandmember set n)**；随机出栈；

​	把key1里值value1赋给key2**(smove key1 key2 value1)**

#### 数学集合类：sdiff；sinter；sunion

​	差集key1 - key2；交集；并集

### Hash(哈希表)

kv模式不变，但是v是个键值对

String类型的Field和Value的映射表，类似Map<String, Object>

#### hset；hget；hmset；hmget；hgetall；hdel（重要）

​	hset：设置hash k的值为键值对v c**(hset k v c)**

​	hget：获取k的值的键v的值c**(hget k v)**

​	hmset：设置多个键值对**(hmset k v1 c1 v2 c2 ...)**

​	hmget：获取多个k的值的键的值

​	hgetall：获取所有vc

​	hdel：通过v删除掉k的一组键值对**(hdel k v)**

#### hkeys；hvals（重要）

​	获取所有v；获取所有c

#### hlen；hexists

​	获取长度；查看k里面的v是否存在

#### hincrby；hincrbyfloat；hsetnx

​	给c的值v加n**(hincrby k c n)**；n为小数；给已存在的c的值赋值为v

### Zset(sorted set: 有序集合)

String类型的有序集合，不允许重复

**会关联一个double类型的分数(score，相当于优先级)，按照分数排序**

#### zadd；zrange；zrangebyscore

​	创建，添加值**(zadd zset value1 score1 value2 score2 ...)**；遍历，加上withscores可以把score也遍历出来；

​	找出闭区间n到m的score的值**(zrangebyscore zset n m)**(加上limit和mysql一个意思)

#### zrem；zcard；zcount；  zrank；zscore；zrevrank；  zrevrange

​	删掉value；统计个数（不包含score）；统计score闭区间内个数；

​	找出索引位置；找出score对应的值；逆序找出索引位置；

​	逆序遍历；逆序根据区间遍历



## 

