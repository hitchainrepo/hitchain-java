DevP2P

以太坊定义了自己的DevP2P协议（https://github.com/ethereum/devp2p）实现以太坊网络内各节点之间的block同步。

 

以太坊的网络协议大致可以分为三个层次：

l  网络层（https://github.com/ethereum/devp2p/blob/master/rlpx.md），定义了如何在网络里发现相邻的node，如何进行node之间的安全握手，如何把上层协议消息放入传输Frame里去，以及如何进行消息的流控。

l  DEVP2P协议层（https://github.com/ethereum/wiki/wiki/%C3%90%CE%9EVp2p-Wire-Protocol），定义了建立以太坊node之间的P2P链接所需要的消息和消息交互的流程。

l  Ethereum协议层（https://github.com/ethereum/wiki/wiki/Ethereum-Wire-Protocol），定义了如何在以太坊node之间获得blockHeaders，Blocks，Transactions等信息的消息和消息交互的流程。

 

EthereumJ Net

EthereumJ使用UDP协议来进行以太坊网络node的发现，使用TCP协议来进行以太坊网络node之间的block消息交互。

 

EthereumJ使用netty的NIO类库来处理底层的TCP协议和UDP协议，自己实现了以太坊DEVP2P定义的协议。

 

EthereumJ进行以太坊node discovery的模块主要包括以下Class。

l  UDPListener，启动netty的UDPChannel，并在Channel的pipeline上加载PacketDecoder和MessageHandler来处理DEVP2P的消息。

 

l  NodeManager，是处理以太坊node discovery的主要class。它使用NodeHandler来处理以太坊node的状态转换。

 

在发现了网络上的其它nodeshou，EthereumJ主要使用以下Class来处理以太坊DEVP2P的消息。

l  EthereumChannelInitializer，扩展了netty的ChannelInitializer<NioSocketChannel>。为EthereumJ建立的client connection或者server connection创建EthereumJ自己的Channel，并对其进行初始化。

 

l  PeerClient，实现了以太坊node的client端的connection的管理。其它模块通过connect()，可以向已经被发现的以太坊node主动建立连接，然后发送消息。

 

l  PeerServer，实现了以太坊node的server端的connection的管理。ChannelManager通过start()启动配置的peep.listener端口，可以响应其它以太坊node建立连接的请求。

 

l  ChannelManager，管理和其它已经被发现的以太坊node建立的Channels。并且提供了sendNewBlock(Block)和sendTransaction(List<Transaction>, Channel)的函数，其它的模块可以调用向以太坊网络里发送blocks和transactions。

 

l  Channel，管理在以太坊node之间所建立的DEVP2P connection上，进行消息的传送，通过下面介绍的各种handler，来处理不同level的消息。

 

l  HandshakeHandler，处理DEVP2P的握手消息。HandshakeHandler扩展了netty的ByteToMessageDecoder，通过netty的回掉函数来处理各种消息。如，通过channelActive()来进行初始化，通过decode()来处理握手的消息。当DEV2P的握手交互结束后，通过调用Channel的publicRLPxHandshakeFinished（）来加载P2pHandler。

 

l  P2pHandler，处理DEVP2P的协议消息。P2pHandler扩展了netty的SimpleChannelInboundHandler，netty通过回掉函数channelRead0()来触发P2pHandler处理各种消息。

 

EthHandler，用来处理DEVP2P的Etheureum协议。当收到对方的DEVP2P的HELLO消息后，Channel的activateEth()被用来启动Etheureum的消息处理。EthereumJ可以处理Eth62和Eth63这两个版本的Ethereum消息协议。

————————————————
DevP2P

以太坊定义了自己的DevP2P协议（https://github.com/ethereum/devp2p）实现以太坊网络内各节点之间的block同步。


以太坊的网络协议大致可以分为三个层次：

l 网络层（https://github.com/ethereum/devp2p/blob/master/rlpx.md），定义了如何在网络里发现相邻的node，如何进行node之间的安全握手，如何把上层协议消息放入传输Frame里去，以及如何进行消息的流控。

l DEVP2P协议层（https://github.com/ethereum/wiki/wiki/%C3%90%CE%9EVp2p-Wire-Protocol），定义了建立以太坊node之间的P2P链接所需要的消息和消息交互的流程。

l Ethereum协议层（https://github.com/ethereum/wiki/wiki/Ethereum-Wire-Protocol），定义了如何在以太坊node之间获得blockHeaders，Blocks，Transactions等信息的消息和消息交互的流程。


EthereumJ Net

EthereumJ使用UDP协议来进行以太坊网络node的发现，使用TCP协议来进行以太坊网络node之间的block消息交互。


EthereumJ使用netty的NIO类库来处理底层的TCP协议和UDP协议，自己实现了以太坊DEVP2P定义的协议。


EthereumJ进行以太坊node discovery的模块主要包括以下Class。

l UDPListener，启动netty的UDP
Channel，并在Channel的pipeline上加载PacketDecoder和MessageHandler来处理DEVP2P的消息。


l NodeManager，是处理以太坊node discovery的主要class。它使用NodeHandler来处理以太坊node的状态转换。


在发现了网络上的其它nodeshou，EthereumJ主要使用以下Class来处理以太坊DEVP2P的消息。

l EthereumChannelInitializer，扩展了netty的ChannelInitializer<NioSocketChannel>。为EthereumJ建立的client connection或者server connection创建EthereumJ自己的Channel，并对其进行初始化。


l PeerClient，实现了以太坊node的client端的connection的管理。其它模块通过connect()，可以向已经被发现的以太坊node主动建立连接，然后发送消息。


l PeerServer，实现了以太坊node的server端的connection的管理。ChannelManager通过start()启动配置的peep.listener端口，可以响应其它以太坊node建立连接的请求。


l ChannelManager，管理和其它已经被发现的以太坊node建立的Channels。并且提供了sendNewBlock(Block)和sendTransaction(List<Transaction>, Channel)的函数，其它的模块可以调用向以太坊网络里发送blocks和transactions。


l Channel，管理在以太坊node之间所建立的DEVP2P connection上，进行消息的传送，通过下面介绍的各种handler，来处理不同level的消息。


l HandshakeHandler，处理DEVP2P的握手消息。HandshakeHandler扩展了netty的ByteToMessageDecoder，通过netty的回掉函数来处理各种消息。如，通过channelActive()来进行初始化，通过decode()来处理握手的消息。当DEV2P的握手交互结束后，通过调用Channel的publicRLPxHandshakeFinished（）来加载P2pHandler。


l P2pHandler，处理DEVP2P的协议消息。P2pHandler扩展了netty的SimpleChannelInboundHandler，netty通过回掉函数channelRead0()来触发P2pHandler处理各种消息。


EthHandler，用来处理DEVP2P的Etheureum协议。当收到对方的DEVP2P的HELLO消息后，Channel的activateEth()被用来启动Etheureum的消息处理。EthereumJ可以处理Eth62和Eth63这两个版本的Ethereum消息协议。