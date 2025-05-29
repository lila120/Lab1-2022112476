| 约束条件说明 | 有效等价类及其编号 | 无效等价类及其编号 |
|-------------|------------------|------------------|
| word1在图中的存在性 | (1) word1存在于图中 | (4) word1不存在于图中 |
| word2在图中的存在性 | (2) word2存在于图中 | (5) word2不存在于图中 |
| word1与word2的关系 | (3) word1与word2不相同且存在路径 | (6) word1与word2相同 |
| 路径连通性 | (7) word1到word2存在路径 | (8) word1到word2不存在路径 |

## 基于实际图结构的黑盒测试用例设计

根据提供的有向图（基于文本"The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again."），重新设计测试用例：

| 测试用例编号 | 输入 | 期望输出 | 所覆盖的等价类编号 |
|-------------|------|----------|------------------|
| TC01 | word1="the", word2="scientist" | "The shortest path from \"the\" to \"scientist\" is: the → scientist\nPath length: 1" | (1), (2), (3), (7) |
| TC02 | word1="hello", word2="scientist" | "No \"hello\" in the graph!" | (4), (2) |
| TC03 | word1="the", word2="the" | "The path from \"the\" to \"the\" is: the" | (1), (2), (6) |
| TC04 | word1="scientist", word2="team" | "The shortest path from \"scientist\" to \"team\" is: scientist → carefully → analyzed → the → data → wrote → a → detailed → report → and → shared → the → report → with → the → team\nPath length: 15" | (1), (2), (3), (7) |
| TC05 | word1="again", word2="scientist" | "No path from \"again\" to \"scientist\"!" | (1), (2), (3), (8) |

## 测试用例说明

**TC01**: 测试直接相邻节点的最短路径
- 从图中可以看到"the"直接连接到"scientist"，权重为1

**TC02**: 测试不存在的起始节点
- "hello"不在图中，应该返回相应的错误信息

**TC03**: 测试相同节点的路径查询
- 当起始节点和目标节点相同时的边界情况

**TC04**: 测试较长路径的最短路径计算
- 从"scientist"到"team"需要经过多个中间节点，测试Dijkstra算法的正确性

**TC05**: 测试两个节点都存在于图中但彼此不连通的情况
- "again"和"scientist"都存在于图中，但从图的结构可以看出，"again"是句子的最后一个词，它没有出边指向其他节点，因此无法到达"scientist"节点
- 这个测试用例验证了程序在处理图中存在孤立节点或单向连通情况时的正确性

注：实际路径长度可能因图的具体构建方式而有所不同，以程序实际运行结果为准。
流程图
```mermaid
flowchart TD
    A[开始] --> B["273-278: word1/word2转小写<br/>获取节点集合"]
    B --> C{"280: word1和word2<br/>都不在图中?"}
    C -->|是| D["281: 返回<br/>都不在图中"]
    C -->|否| E{"283: word1<br/>不在图中?"}
    E -->|是| F["284: 返回<br/>word1不在图中"]
    E -->|否| G{"286: word2<br/>不在图中?"}
    G -->|是| H["287: 返回<br/>word2不在图中"]
    G -->|否| I["291-293: 初始化<br/>edges, bridgeWords, word1Edges"]
    I --> J{"294: word1Edges<br/>!= null?"}
    J -->|否| P{"303: bridgeWords<br/>为空?"}
    J -->|是| K["295: 开始遍历<br/>word1的邻接节点"]
    K --> L{"297: bridgeEdges != null<br/>且<br/>containsKey word2?"}
    L -->|是| M["298: 添加桥接词<br/>到bridgeWords"]
    L -->|否| N{"还有更多<br/>邻接节点?"}
    M --> N
    N -->|是| K
    N -->|否| P
    P -->|是| Q["304: 返回<br/>无桥接词"]
    P -->|否| R{"305: bridgeWords<br/>只有1个?"}
    R -->|是| S["306-307: 返回<br/>单个桥接词"]
    R -->|否| T["309-312: 返回<br/>多个桥接词"]
    D --> U[结束]
    F --> U
    H --> U
    Q --> U
    S --> U
    T --> U
```

控制流图
```mermaid
flowchart TD
    N1["N1: 273-278<br/>入口节点"] --> N2{"N2: 280<br/>判定节点"}
    N2 -->|是| N3["N3: 281<br/>返回节点"]
    N2 -->|否| N4{"N4: 283<br/>判定节点"}
    N4 -->|是| N5["N5: 284<br/>返回节点"]
    N4 -->|否| N6{"N6: 286<br/>判定节点"}
    N6 -->|是| N7["N7: 287<br/>返回节点"]
    N6 -->|否| N8["N8: 291-293<br/>处理节点"]
    N8 --> N9{"N9: 294<br/>判定节点"}
    N9 -->|是| N10["N10: 295<br/>循环头节点"]
    N9 -->|否| N13{"N13: 303<br/>判定节点"}
    N10 --> N11{"N11: 297<br/>判定节点"}
    N11 -->|是| N12["N12: 298<br/>处理节点"]
    N11 -->|否| N10
    N12 --> N10
    N10 --> N13
    N13 -->|是| N14["N14: 304<br/>返回节点"]
    N13 -->|否| N15{"N15: 305<br/>判定节点"}
    N15 -->|是| N16["N16: 306-307<br/>返回节点"]
    N15 -->|否| N17["N17: 309-312<br/>返回节点"]
    
    style N2 fill:#ffeb3b
    style N4 fill:#ffeb3b
    style N6 fill:#ffeb3b
    style N9 fill:#ffeb3b
    style N11 fill:#ffeb3b
    style N13 fill:#ffeb3b
    style N15 fill:#ffeb3b
    style N3 fill:#f44336,color:#fff
    style N5 fill:#f44336,color:#fff
    style N7 fill:#f44336,color:#fff
    style N14 fill:#f44336,color:#fff
    style N16 fill:#f44336,color:#fff
    style N17 fill:#f44336,color:#fff
```

## 6.5 基本路径法白盒测试用例设计

| 测试用例编号 | 输入数据 | 期望的输出 | 所覆盖的基本路径编号 |
|-------------|----------|------------|---------------------|
| 1. | word1="hello", word2="scientist" | "No \"hello\" in the graph!" | 基本路径2: 273→280→283→284 |
| 2. | word1="again", word2="scientist" | "No bridge words from \"again\" to \"scientist\"!" | 基本路径4: 273→280→283→286→291→294→303→304 |
| 3. | word1="the", word2="data" | "The bridge words from \"the\" to \"data\" is: \"scientist\"" 或 "No bridge words from \"the\" to \"data\"!" | 基本路径5: 273→280→283→286→291→294→295→303→304 |
| 4. | word1="scientist", word2="analyzed" | "The bridge words from \"scientist\" to \"analyzed\" is: \"carefully\"" | 基本路径6: 273→280→283→286→291→294→295→297→298→295→303→305→306 |

### 测试用例说明：

**测试用例1**: 测试第一个单词不在图中的情况  
- word1不存在，word2存在，验证程序优先检测word1

**测试用例2**: 测试两个单词都存在但word1没有出边的情况
- "again"是句末单词，没有出边，验证程序返回无桥接词

**测试用例3**: 测试有出边但没有桥接词的情况
- word1有出边，但不能通过一个中间节点到达word2

**测试用例4**: 测试找到唯一桥接词的情况
- 验证程序能正确找到并返回单个桥接词

注：由于测试基于具体的图结构，某些期望输出可能需要根据实际图的构建结果进行调整。
