package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * 软件工程实验1 - 有向图文本分析系统.
 */
public class Main {

  // 有向图数据结构
  private static class DirectedGraph {
    private Set<String> nodes;
    private Map<String, Map<String, Integer>> edges; // 源节点 -> {目标节点 -> 权重}
    private Map<String, List<String>> inEdges; // 目标节点 -> [源节点列表]
    
    // 使用静态SecureRandom实例，避免重复创建，提供加密安全的随机数
    private static final Random SHARED_RANDOM = new SecureRandom();

    public DirectedGraph() {
      this.nodes = new HashSet<>();
      this.edges = new HashMap<>();
      this.inEdges = new HashMap<>();
    }

    public void addEdge(String source, String target, int weight) {
      nodes.add(source);
      nodes.add(target);
      edges.computeIfAbsent(source, k -> new HashMap<>()).merge(target, weight, Integer::sum);
      inEdges.computeIfAbsent(target, k -> new ArrayList<>()).add(source);
    }

    public void buildFromText(String text) {
      text = text.replaceAll("[^a-zA-Z\\s]", " ")
              .replaceAll("\\s+", " ")
              .trim()
              .toLowerCase();

      String[] words = text.split("\\s+");
      if (words.length < 2) {
        return;
      }

      for (int i = 0; i < words.length - 1; i++) {
        if (!words[i].isEmpty() && !words[i + 1].isEmpty()) {
          addEdge(words[i], words[i + 1], 1);
        }
      }
    }

    public Set<String> getNodes() {
      return new HashSet<>(nodes);
    }

    public Map<String, Map<String, Integer>> getEdges() {
      return new HashMap<>(edges);
    }

    public Map<String, List<String>> getInEdges() {
      return new HashMap<>(inEdges);
    }

    public Random getRandom() {
      return SHARED_RANDOM;
    }
  }

  // 全局图对象
  private static DirectedGraph graph = new DirectedGraph();

  /**
   * 用于测试的公共方法：初始化图
   * @param text 用于构建图的文本
   */
  public static void initializeGraphForTesting(String text) {
    graph = new DirectedGraph();
    graph.buildFromText(text);
  }

  /**.
   * 主程序入口
   */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    System.out.println("=== 有向图文本分析系统 ===");

    // 读取输入文件
    String text = loadTextFile(args, scanner);
    if (text == null) {
      System.err.println("无法读取文本，程序退出");
      return;
    }

    // 构建图
    System.out.println("正在构建有向图...");
    graph.buildFromText(text);
    System.out.println("有向图构建完成！");
    System.out.println("节点数量: " + graph.getNodes().size());
    System.out.println("边数量: " + graph.getEdges().values().stream().mapToInt(Map::size).sum());

    // 主菜单循环
    while (true) {
      showMenu();
      String choice = scanner.nextLine().trim();

      try {
        switch (choice) {
          case "1":
            System.out.println("\n=== 有向图结构 ===");
            showDirectedGraph(graph);
            break;
          case "2":
            handleVisualizeGraph();
            break;
          case "3":
            handleQueryBridgeWords(scanner);
            break;
          case "4":
            handleGenerateNewText(scanner);
            break;
          case "5":
            handleCalcShortestPath(scanner);
            break;
          case "6":
            handleCalcPageRank(scanner);
            break;
          case "7":
            handleRandomWalk();
            break;
          case "0":
            System.out.println("程序退出，感谢使用！");
            scanner.close();
            return;
          default:
            System.out.println("无效选择，请重试");
        }
      } catch (Exception e) {
        System.err.println("操作出错: " + e.getMessage());
      }
    }
  }

  /**.
   * 展示有向图
   *
   * @param directedGraph 有向图对象
   */
  public static void showDirectedGraph(DirectedGraph directedGraph) {
    System.out.println("节点总数: " + directedGraph.getNodes().size());

    int totalEdges = directedGraph.getEdges().values().stream().mapToInt(Map::size).sum();
    System.out.println("边总数: " + totalEdges);

    System.out.println("\n节点连接关系:");
    List<String> sortedNodes = new ArrayList<>(directedGraph.getNodes());
    Collections.sort(sortedNodes);

    for (String node : sortedNodes) {
      Map<String, Integer> neighbors = directedGraph.getEdges().get(node);
      if (neighbors != null && !neighbors.isEmpty()) {
        System.out.print("节点 '" + node + "' 连接到: ");
        List<String> edgesInfo = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
          edgesInfo.add("'" + entry.getKey() + "'(权重:" + entry.getValue() + ")");
        }
        System.out.println(String.join(", ", edgesInfo));
      }
    }
  }

  /**
   * 使用Graphviz可视化有向图.
   *
   * @param highlightPath 需要高亮显示的路径
   * @return 操作结果信息
   */
  public static String visualizeGraph(List<String> highlightPath) {
    try {
      StringBuilder dot = new StringBuilder();
      dot.append("digraph DirectedGraph {\n");
      dot.append("    rankdir=LR;\n");
      dot.append("    node [shape=ellipse, style=filled, fillcolor=white];\n");
      dot.append("    edge [fontsize=12];\n\n");

      // 添加节点
      for (String node : graph.getNodes()) {
        dot.append("    \"").append(node).append("\";\n");
      }
      dot.append("\n");

      // 创建高亮边集合
      Set<String> highlightEdges = new HashSet<>();
      if (highlightPath != null && highlightPath.size() > 1) {
        for (int i = 0; i < highlightPath.size() - 1; i++) {
          highlightEdges.add(highlightPath.get(i) + "->" + highlightPath.get(i + 1));
        }
      }

      // 添加边
      for (String source : graph.getEdges().keySet()) {
        Map<String, Integer> targets = graph.getEdges().get(source);
        for (Map.Entry<String, Integer> entry : targets.entrySet()) {
          String target = entry.getKey();
          Integer weight = entry.getValue();

          String edgeKey = source + "->" + target;
          if (highlightEdges.contains(edgeKey)) {
            dot.append("    \"").append(source).append("\" -> \"").append(target)
                    .append("\" [label=\"").append(weight)
                    .append("\", color=red, penwidth=2.0];\n");
          } else {
            dot.append("    \"").append(source).append("\" -> \"").append(target)
                    .append("\" [label=\"").append(weight).append("\"];\n");
          }
        }
      }

      dot.append("}\n");

      // 写入DOT文件
      Files.write(Paths.get("directed_graph.dot"), dot.toString().getBytes(StandardCharsets.UTF_8));

      // 尝试调用Graphviz生成PNG
      try {
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", "directed_graph.dot", 
                "-o", "directed_graph.png");
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
          return "图形已生成并保存为 directed_graph.png\nDOT文件已保存为 directed_graph.dot";
        } else {
          return "DOT文件已保存为 directed_graph.dot\n"
                  + "请手动运行: dot -Tpng directed_graph.dot -o directed_graph.png";
        }
      } catch (IOException | InterruptedException e) {
        return "DOT文件已保存为 directed_graph.dot\n"
                +
                "无法自动调用Graphviz，请确保已安装Graphviz并添加到PATH\n"
                +
                "手动生成图片: dot -Tpng directed_graph.dot -o directed_graph.png";
      }

    } catch (IOException e) {
      return "生成DOT文件时出错: " + e.getMessage();
    }
  }

  /**
   * 查询桥接词.
   *
   * @param word1 第一个单词
   * @param word2 第二个单词
   * @return 桥接词查询结果
   */
  public static String queryBridgeWords(String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    Set<String> nodes = graph.getNodes();

    // 检查单词是否在图中
    if (!nodes.contains(word1) && !nodes.contains(word2)) {
      return String.format("No \"%s\" and \"%s\" in the graph!", word1, word2);
    }
    if (!nodes.contains(word1)) {
      return String.format("No \"%s\" in the graph!", word1);
    }
    if (!nodes.contains(word2)) {
      return String.format("No \"%s\" in the graph!", word2);
    }

    // 查找桥接词
    final Map<String, Map<String, Integer>> edges = graph.getEdges();
    List<String> bridgeWords = new ArrayList<>();
    Map<String, Integer> word1Edges = edges.get(word1);
    if (word1Edges != null) {
      for (String bridge : word1Edges.keySet()) {
        Map<String, Integer> bridgeEdges = edges.get(bridge);
        if (bridgeEdges != null && bridgeEdges.containsKey(word2)) {
          bridgeWords.add(bridge);
        }
      }
    }

    if (bridgeWords.isEmpty()) {
      return String.format("No bridge words from \"%s\" to \"%s\"!", word1, word2);
    } else if (bridgeWords.size() == 1) {
      return String.format("The bridge words from \"%s\" to \"%s\" is: \"%s\"",
              word1, word2, bridgeWords.get(0));
    } else {
      String lastWord = bridgeWords.remove(bridgeWords.size() - 1);
      String bridgesStr = String.join("\", \"", bridgeWords);
      return String.format("The bridge words from \"%s\" to \"%s\" are: \"%s\" and \"%s\".",
              word1, word2, bridgesStr, lastWord);
    }
  }

  /**
   * 根据bridge word生成新文本.
   *
   * @param inputText 输入文本
   * @return 生成的新文本
   */
  public static String generateNewText(String inputText) {
    // 预处理输入文本
    inputText = inputText.replaceAll("[^a-zA-Z\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase();
    String[] words = inputText.split("\\s+");

    if (words.length < 2) {
      return inputText;
    }

    Set<String> nodes = graph.getNodes();
    Map<String, Map<String, Integer>> edges = graph.getEdges();
    Random random = graph.getRandom();

    List<String> result = new ArrayList<>();
    result.add(words[0]);

    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];

      // 查找桥接词
      List<String> bridgeCandidates = new ArrayList<>();
      if (nodes.contains(word1) && nodes.contains(word2)) {
        Map<String, Integer> word1Edges = edges.get(word1);
        if (word1Edges != null) {
          for (String bridge : word1Edges.keySet()) {
            Map<String, Integer> bridgeEdges = edges.get(bridge);
            if (bridgeEdges != null && bridgeEdges.containsKey(word2)) {
              bridgeCandidates.add(bridge);
            }
          }
        }
      }

      // 如果有桥接词，随机选一个插入
      if (!bridgeCandidates.isEmpty()) {
        String bridge = bridgeCandidates.get(random.nextInt(bridgeCandidates.size()));
        result.add(bridge);
      }

      result.add(word2);
    }

    return String.join(" ", result);
  }

  /**
   * 计算两个单词之间的最短路径.
   *
   * @param word1 起始单词
   * @param word2 目标单词
   * @return 最短路径描述
   */
  public static String calcShortestPath(String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    Set<String> nodes = graph.getNodes();

    // 检查单词是否在图中
    if (!nodes.contains(word1)) {
      return String.format("No \"%s\" in the graph!", word1);
    }
    if (!nodes.contains(word2)) {
      return String.format("No \"%s\" in the graph!", word2);
    }
    if (word1.equals(word2)) {
      return String.format("The path from \"%s\" to \"%s\" is: %s", word1, word2, word1);
    }

    // 使用Dijkstra算法计算最短路径
    final Map<String, Map<String, Integer>> edges = graph.getEdges();
    Map<String, Integer> distances = new HashMap<>();
    PriorityQueue<NodeDistance> unvisited = new PriorityQueue<>();

    // 初始化距离
    for (String node : nodes) {
      distances.put(node, Integer.MAX_VALUE);
    }
    distances.put(word1, 0);
    unvisited.offer(new NodeDistance(word1, 0));

    final Map<String, String> previousNodes = new HashMap<>();
    while (!unvisited.isEmpty()) {
      NodeDistance current = unvisited.poll();
      String currentNode = current.node;
      int currentDistance = current.distance;

      // 如果找到目标节点，构建路径
      if (currentNode.equals(word2)) {
        List<String> path = new ArrayList<>();
        String node = currentNode;
        while (node != null) {
          path.add(0, node);
          node = previousNodes.get(node);
        }
        // 路径长度应该是边的数量，即节点数量减1
        int pathLength = path.size() - 1;
        return String.format("The shortest path from \"%s\" to \"%s\" is: %s%nPath length: %d",
                word1, word2, String.join(" → ", path), pathLength);
      }

      // 如果当前节点已处理过，跳过
      if (currentDistance > distances.get(currentNode)) {
        continue;
      }

      // 检查所有邻居节点
      Map<String, Integer> neighbors = edges.get(currentNode);
      if (neighbors != null) {
        for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
          String neighbor = entry.getKey();
          int weight = entry.getValue();
          int distance = currentDistance + weight;

          if (distance < distances.get(neighbor)) {
            distances.put(neighbor, distance);
            previousNodes.put(neighbor, currentNode);
            unvisited.offer(new NodeDistance(neighbor, distance));
          }
        }
      }
    }

    return String.format("No path from \"%s\" to \"%s\"!", word1, word2);
  }

  /**
   * 计算单词的PageRank值.
   *
   * @param word 要计算PR值的单词
   * @return PageRank值
   */
  public static Double calPageRank(String word) {
    word = word.toLowerCase();

    Set<String> nodes = graph.getNodes();
    Map<String, List<String>> inEdges = graph.getInEdges();

    if (!nodes.contains(word)) {
      return null; // 单词不在图中
    }

    double d = 0.85; // 阻尼系数
    int maxIterations = 100;
    double tolerance = 1e-6;

    // 初始化PageRank值
    Map<String, Double> pr = new HashMap<>();
    for (String node : nodes) {
      pr.put(node, 1.0 / nodes.size());
    }

    // 迭代计算PageRank
    final Map<String, Map<String, Integer>> edges = graph.getEdges();
    for (int iteration = 0; iteration < maxIterations; iteration++) {
      Map<String, Double> prevPr = new HashMap<>(pr);

      // 收集出度为0的节点的PR值总和
      double sinkPrSum = 0;
      for (String node : nodes) {
        Map<String, Integer> outEdges = edges.get(node);
        if (outEdges == null || outEdges.isEmpty()) {
          sinkPrSum += prevPr.get(node);
        }
      }

      // 计算每个节点新的PR值
      for (String node : nodes) {
        double inSum = 0;
        List<String> inNodes = inEdges.get(node);
        if (inNodes != null) {
          for (String inNode : inNodes) {
            Map<String, Integer> outEdges = edges.get(inNode);
            if (outEdges != null && !outEdges.isEmpty()) {
              inSum += prevPr.get(inNode) / outEdges.size();
            }
          }
        }

        pr.put(node, (1 - d) / nodes.size() + d * (inSum + sinkPrSum / nodes.size()));
      }

      // 检查收敛性
      double diff = 0;
      for (String node : nodes) {
        diff += Math.abs(pr.get(node) - prevPr.get(node));
      }
      if (diff < tolerance) {
        break;
      }
    }

    return pr.get(word);
  }

  /**
   * 随机游走.
   *
   * @return 随机游走路径
   */
  public static String randomWalk() {
    Set<String> nodes = graph.getNodes();
    Map<String, Map<String, Integer>> edges = graph.getEdges();
    Random random = graph.getRandom();

    if (nodes.isEmpty()) {
      return "图为空，无法进行随机游走!";
    }

    // 随机选择起点
    List<String> nodeList = new ArrayList<>(nodes);
    String current = nodeList.get(random.nextInt(nodeList.size()));
    List<String> path = new ArrayList<>();
    path.add(current);
    Set<String> visitedEdges = new HashSet<>();

    while (true) {
      // 获取当前节点的所有出边
      Map<String, Integer> neighbors = edges.get(current);
      if (neighbors == null || neighbors.isEmpty()) {
        break;
      }

      List<String> neighborList = new ArrayList<>(neighbors.keySet());
      String nextNode = neighborList.get(random.nextInt(neighborList.size()));
      String currentEdge = current + "->" + nextNode;

      // 如果这条边已经访问过，结束游走
      if (visitedEdges.contains(currentEdge)) {
        path.add(nextNode);
        break;
      }

      visitedEdges.add(currentEdge);
      path.add(nextNode);
      current = nextNode;
    }

    // 将路径保存到文件
    String result = String.join(" ", path);
    try {
      Files.write(Paths.get("random_walk_result.txt"), result.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      System.err.println("保存随机游走结果时出错: " + e.getMessage());
    }

    return result;
  }

  // ================ 辅助函数 ================

  /**
   * 辅助类：节点距离对.
   */
  private static class NodeDistance implements Comparable<NodeDistance> {
    String node;
    int distance;

    NodeDistance(String node, int distance) {
      this.node = node;
      this.distance = distance;
    }

    @Override
    public int compareTo(NodeDistance other) {
      return Integer.compare(this.distance, other.distance);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      NodeDistance other = (NodeDistance) obj;
      return distance == other.distance
              && (node == null ? other.node == null : node.equals(other.node));
    }

    @Override
    public int hashCode() {
      return 31 * distance + (node == null ? 0 : node.hashCode());
    }
  }

  /**
   * 辅助函数：验证文件路径安全性，防止路径遍历攻击.
   *
   * @param userPath 用户输入的文件路径
   *
   * @return 安全验证后的规范化路径，如果不安全则返回null
   */
  private static Path validateAndNormalizePath(String userPath) {
    if (userPath == null || userPath.trim().isEmpty()) {
      System.err.println("路径不能为空");
      return null;
    }

    // 预处理：清理输入路径
    String cleanPath = userPath.trim();
    
    // 检查危险字符和模式
    if (cleanPath.contains("..")
            || cleanPath.contains("~")
            || cleanPath.startsWith("/")
            || cleanPath.contains("\\")
            || cleanPath.matches(".*[<>:\"|?*].*")) {
      System.err.println("安全错误: 路径包含非法字符或路径遍历模式");
      return null;
    }
    
    // 检查文件扩展名白名单（在路径解析之前）
    String fileName = cleanPath.toLowerCase();
    if (fileName.contains(".")) {
      String extension = fileName.substring(fileName.lastIndexOf("."));
      if (!(".txt".equals(extension)
              || ".text".equals(extension)
              || ".md".equals(extension)
              || ".log".equals(extension))) {
        System.err.println("安全错误: 只允许读取文本文件 (.txt, .text, .md, .log)");
        return null;
      }
    }

    try {
      // 获取当前工作目录作为基准
      Path currentDir = Paths.get("").toAbsolutePath();
      
      // 使用resolve()而不是直接解析用户路径，确保相对于当前目录
      Path safePath = currentDir.resolve(cleanPath).normalize();
      
      // 验证解析后的路径仍然在当前目录下
      if (!safePath.startsWith(currentDir)) {
        System.err.println("安全错误: 路径超出允许范围");
        return null;
      }
      
      // 检查文件是否存在且可读
      if (!Files.exists(safePath)) {
        System.err.println("文件不存在: " + safePath.getFileName());
        return null;
      }
      
      if (!Files.isReadable(safePath)) {
        System.err.println("文件不可读: " + safePath.getFileName());
        return null;
      }
      
      // 检查是否为普通文件（不是目录或特殊文件）
      if (!Files.isRegularFile(safePath)) {
        System.err.println("不是普通文件: " + safePath.getFileName());
        return null;
      }
      
      return safePath;
      
    } catch (Exception e) {
      System.err.println("路径验证错误: " + e.getMessage());
      return null;
    }
  }

  /**
   * 辅助函数：读取文本文件.
   */
  private static String loadTextFile(String[] args, Scanner scanner) {
    String userInputPath;

    if (args.length > 0) {
      userInputPath = args[0];
    } else {
      System.out.print("请输入文本文件路径 (或直接回车使用默认测试文本): ");
      userInputPath = scanner.nextLine().trim();

      if (userInputPath.isEmpty()) {
        System.out.println("使用默认测试文本...");
        return getDefaultTestText();
      }
    }

    // 安全验证用户输入的路径
    Path safePath = validateAndNormalizePath(userInputPath);
    if (safePath == null) {
      System.out.println("由于安全原因无法读取文件，使用默认测试文本...");
      return getDefaultTestText();
    }

    try {
      String text = Files.readString(safePath);
      System.out.println("成功读取文件: " + safePath);
      return text;
    } catch (IOException e) {
      System.err.println("读取文件错误: " + e.getMessage());
      System.out.println("使用默认测试文本...");
      return getDefaultTestText();
    }
  }

  /**
   * 辅助函数：获取默认测试文本.
   */
  private static String getDefaultTestText() {
    return "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.";
  }

  /**
   * 辅助函数：显示菜单.
   */
  private static void showMenu() {
    System.out.println("\n" + "=".repeat(40));
    System.out.println("有向图文本分析系统 - 功能菜单");
    System.out.println("=".repeat(40));
    System.out.println("1. 展示有向图(文本形式)");
    System.out.println("2. 展示有向图(图形形式)");
    System.out.println("3. 查询桥接词");
    System.out.println("4. 根据桥接词生成新文本");
    System.out.println("5. 计算最短路径");
    System.out.println("6. 计算PageRank值");
    System.out.println("7. 随机游走");
    System.out.println("0. 退出程序");
    System.out.println("=".repeat(40));
    System.out.print("请输入选择 (0-7): ");
  }

  /**
   * 辅助函数：处理图形可视化.
   */
  private static void handleVisualizeGraph() {
    System.out.println("正在生成图形可视化...");
    String result = visualizeGraph(null);
    System.out.println(result);
  }

  /**
   * 辅助函数：处理桥接词查询.
   */
  private static void handleQueryBridgeWords(Scanner scanner) {
    System.out.print("请输入第一个单词: ");
    String word1 = scanner.nextLine().trim();
    System.out.print("请输入第二个单词: ");
    String word2 = scanner.nextLine().trim();

    if (word1.isEmpty() || word2.isEmpty()) {
      System.out.println("单词不能为空");
      return;
    }

    String result = queryBridgeWords(word1, word2);
    System.out.println("查询结果: " + result);
  }

  /**
   * 辅助函数：处理新文本生成.
   */
  private static void handleGenerateNewText(Scanner scanner) {
    System.out.print("请输入一段文本: ");
    String text = scanner.nextLine().trim();

    if (text.isEmpty()) {
      System.out.println("文本不能为空");
      return;
    }

    System.out.println("原始文本: " + text);
    String result = generateNewText(text);
    System.out.println("生成文本: " + result);
  }

  /**
   * 辅助函数：处理最短路径计算.
   */
  private static void handleCalcShortestPath(Scanner scanner) {
    System.out.print("请输入起始单词: ");
    String word1 = scanner.nextLine().trim();
    System.out.print("请输入目标单词: ");
    String word2 = scanner.nextLine().trim();

    if (word1.isEmpty() || word2.isEmpty()) {
      System.out.println("单词不能为空");
      return;
    }

    String result = calcShortestPath(word1, word2);
    System.out.println("路径计算结果:\n" + result);

    // 如果找到了路径，询问是否生成可视化
    if (result.contains("The shortest path from")) {
      System.out.print("是否生成最短路径可视化图？(y/n): ");
      String choice = scanner.nextLine().trim().toLowerCase();
      if (choice.equals("y") || choice.equals("yes")) {
        // 从结果中提取路径
        List<String> path = extractPathFromResult(result);
        if (path != null) {
          System.out.println("正在生成最短路径可视化...");
          String visualResult = visualizeGraph(path);
          System.out.println(visualResult);
          System.out.println("红色路径表示最短路径");
        }
      }
    }
  }

  /**
   * 辅助函数：从结果字符串中提取路径.
   */
  private static List<String> extractPathFromResult(String result) {
    try {
      String[] lines = result.split("\n");
      for (String line : lines) {
        if (line.contains("The shortest path from")) {
          int start = line.indexOf("is: ") + 4;
          if (start > 3) {
            String pathStr = line.substring(start);
            if (pathStr.contains("\n")) {
              pathStr = pathStr.substring(0, pathStr.indexOf("\n"));
            }
            return Arrays.asList(pathStr.split(" → "));
          }
        }
      }
    } catch (Exception e) {
      System.err.println("解析路径时出错: " + e.getMessage());
    }
    return null;
  }

  /**
   * 辅助函数：处理PageRank计算.
   */
  private static void handleCalcPageRank(Scanner scanner) {
    System.out.print("请输入单词: ");
    String word = scanner.nextLine().trim();

    if (word.isEmpty()) {
      System.out.println("单词不能为空");
      return;
    }

    Double result = calPageRank(word);
    if (result != null) {
      System.out.printf("单词 '%s' 的PageRank值: %.6f%n", word, result);
    } else {
      System.out.printf("No \"%s\" in the graph!%n", word);
    }
  }

  /**
   * 辅助函数：处理随机游走.
   */
  private static void handleRandomWalk() {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    System.out.println("正在执行随机游走...");
    String result = randomWalk();
    System.out.println("随机游走路径: " + result);
    System.out.println("路径已保存到 random_walk_result.txt");

    System.out.print("是否生成随机游走路径可视化图？(y/n): ");
    String choice = scanner.nextLine().trim().toLowerCase();
    if (choice.equals("y") || choice.equals("yes")) {
      List<String> path = Arrays.asList(result.split(" "));
      System.out.println("正在生成随机游走可视化...");
      String visualResult = visualizeGraph(path);
      System.out.println(visualResult);
      System.out.println("红色路径表示随机游走轨迹");
    }
  }
}