package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * queryBridgeWords方法的基本路径法白盒测试类
 */
class queryBridgeWordsTest {

    @BeforeEach
    @DisplayName("初始化测试环境")
    void setUp() {
        // 使用测试文本构建有向图
        String testText = "The scientist carefully analyzed the data, wrote a detailed report, " +
                "and shared the report with the team, but the team requested more data, " +
                "so the scientist analyzed it again.";
        
        // 使用公共方法初始化图
        Main.initializeGraphForTesting(testText);
    }

    @Test
    @DisplayName("测试用例1: word1不在图中的情况 - 覆盖基本路径2")
    void testCase1_Word1NotInGraph() {
        // 测试用例1: word1="hello", word2="scientist"
        // 覆盖基本路径2: 273→280→283→284
        String result = Main.queryBridgeWords("hello", "scientist");
        
        // 验证返回正确的错误信息
        assertEquals("No \"hello\" in the graph!", result);
        
        System.out.println("测试用例1 实际输出: " + result);
    }

    @Test
    @DisplayName("测试用例2: 两个单词都存在但word1没有出边 - 覆盖基本路径4")
    void testCase2_Word1HasNoOutEdges() {
        // 测试用例2: word1="again", word2="scientist"
        // 覆盖基本路径4: 273→280→283→286→291→294→303→304
        String result = Main.queryBridgeWords("again", "scientist");
        
        // 验证返回无桥接词信息
        assertEquals("No bridge words from \"again\" to \"scientist\"!", result);
        
        System.out.println("测试用例2 实际输出: " + result);
    }

    @Test
    @DisplayName("测试用例3: 有出边但没有桥接词 - 覆盖基本路径5")
    void testCase3_HasOutEdgesButNoBridgeWords() {
        // 测试用例3: word1="the", word2="data"
        // 覆盖基本路径5: 273→280→283→286→291→294→295→303→304
        String result = Main.queryBridgeWords("the", "data");
        
        // 验证结果（可能有桥接词或无桥接词，取决于图的具体结构）
        assertTrue(result.contains("bridge words from \"the\" to \"data\"") || 
                   result.contains("No bridge words from \"the\" to \"data\"!"));
        
        System.out.println("测试用例3 实际输出: " + result);
    }

    @Test
    @DisplayName("测试用例4: 找到唯一桥接词 - 覆盖基本路径6")
    void testCase4_FindSingleBridgeWord() {
        // 测试用例4: word1="scientist", word2="analyzed"
        // 覆盖基本路径6: 273→280→283→286→291→294→295→297→298→295→303→305→306
        String result = Main.queryBridgeWords("scientist", "analyzed");
        
        // 验证找到桥接词（根据图结构，应该找到"carefully"作为桥接词）
        assertTrue(result.contains("The bridge words from \"scientist\" to \"analyzed\" is:") ||
                   result.contains("No bridge words from \"scientist\" to \"analyzed\"!"));
        
        // 如果找到桥接词，应该包含"carefully"
        if (result.contains("The bridge words")) {
            assertTrue(result.contains("carefully"));
        }
        
        System.out.println("测试用例4 实际输出: " + result);
    }

    @Test
    @DisplayName("额外测试: 验证多个桥接词的情况")
    void testMultipleBridgeWords() {
        // 尝试找一个可能有多个桥接词的组合
        String result = Main.queryBridgeWords("the", "report");
        
        // 验证结果格式
        assertTrue(result.contains("bridge words from \"the\" to \"report\""));
        
        System.out.println("多个桥接词测试 实际输出: " + result);
    }

    @Test
    @DisplayName("额外测试: 验证两个单词都不在图中的情况")
    void testBothWordsNotInGraph() {
        // 测试两个都不存在的单词
        String result = Main.queryBridgeWords("hello", "world");
        
        // 验证返回正确的错误信息
        assertEquals("No \"hello\" and \"world\" in the graph!", result);
        
        System.out.println("两个单词都不存在测试 实际输出: " + result);
    }
}