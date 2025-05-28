package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Main类的JUnit测试类
 * 包含calcShortestPath方法和其他功能的测试用例
 */
class MainTest {

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

    // ========== calcShortestPath方法测试用例 ==========
    
    @Test
    @DisplayName("TC01: word1=\"the\", word2=\"scientist\" - 覆盖等价类(1),(2),(3),(4)")
    void test1() {
        // 测试用例1: 两个存在且不同的节点，存在直接路径
        String result = Main.calcShortestPath("the", "scientist");
        
        // 验证结果格式正确
        assertTrue(result.contains("The shortest path from \"the\" to \"scientist\" is:"));
        assertTrue(result.contains("the → scientist"));
        assertTrue(result.contains("Path length: 1"));
        
        System.out.println("TC01 实际输出: " + result);
    }

    @Test
    @DisplayName("TC02: word1=\"hello\", word2=\"scientist\" - 覆盖等价类(2),(5)")
    void test2() {
        // 测试用例2: word1不存在于图中
        String result = Main.calcShortestPath("hello", "scientist");
        
        // 验证返回正确的错误信息
        assertEquals("No \"hello\" in the graph!", result);
        
        System.out.println("TC02 实际输出: " + result);
    }

    @Test
    @DisplayName("TC03: word1=\"the\", word2=\"the\" - 覆盖等价类(1),(2),(7)")
    void test3() {
        // 测试用例3: 相同的起始和目标节点
        String result = Main.calcShortestPath("the", "the");
        
        // 验证返回正确的自环路径
        assertEquals("The path from \"the\" to \"the\" is: the", result);
        
        System.out.println("TC03 实际输出: " + result);
    }

    @Test
    @DisplayName("TC04: word1=\"again\", word2=\"scientist\" - 覆盖等价类(1),(2),(3),(8)")
    void test4() {
        // 测试用例4: 两个存在但不连通的节点
        String result = Main.calcShortestPath("again", "scientist");
        
        // 验证返回正确的无路径信息
        assertEquals("No path from \"again\" to \"scientist\"!", result);
        
        System.out.println("TC04 实际输出: " + result);
    }
}