项目启动步骤

1、Initializer

管理版本
加载配置文件
加载所有创世块
加载nodeId
2、WorldManager

从磁盘加载区块链信息
客户端连接别人（同时启动客户端同步）
3、SyncManager

快速同步
标准同步
在父类中，header同步，block同步。
从同步队列中获取区块，添加到主链中
同步完成后，通知所有监控该动作的模块（包括挖矿程序）
4、BlockMiner

监听同步完成事件，启动挖矿程序
创建新区块
开始挖矿
尝试添加新区块
广播区块
监听程序

EthereumListener 同步，追踪，交易等监听
MinerListener 挖矿动作监听

CommonConfig

公共配置类，用来指定服务的具体实现类。
SystemProperties
Initializer
RepositoryWrapper
Repository
trieNodeSource
StateSource
cachedDbSource （根据名称生成原子db，如rockDB,levelDB）
blockchainSource （做了层格式转换）
blockchainDbCache （又做了一层封装,写缓存封装）
keyValueDataSource (rockDB,levelDB)
levelDbDataSource
rocksDbDataSource
fastSyncCleanUp
resetDataSource
headerSource (?)
precompileSource (?)
blockchainDB (blockchain区块链存储文件名)
dbFlushManager （刷新仓库）
BlockHeaderValidator （区块header验证）
ParentBlockHeaderValidator
peerSource （对等数据源）

DefaultConfig

BlockStore
TransactionStore
PruneManager

SystemProperties

管理所有配置文件读取及参数验证。

BlockchainNetConfig