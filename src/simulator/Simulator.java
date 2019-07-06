package simulator;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;
import strategy.ChoseBridge;
import strategy.Closestrealspeed;
import strategy.Strategyone;
import strategy.TheClosestSpeed;

/**
 * 猴子过河模拟器.
 * @author 王程
 *
 */
public class Simulator {
  public static  Map<Ladder, List<Map<Rung, Monkey>>> ladders = Collections.synchronizedMap(new HashMap<>());// 梯子
  public static  List<Monkey> monkeys = Collections.synchronizedList(new ArrayList<>());// 猴子
  private  List<String[]> datas = new ArrayList<String[]>();// 猴子数据(产生时间，ID， direction，v)
  private  List<Thread> subthreads = new ArrayList<Thread>();// 线程
  private int count = 0;// 记录猴子编号
  public static CountDownLatch threadSignal = null;//初始化countDown
  public static List<Monkey> monkeyssequence = Collections.synchronizedList(new ArrayList<>());// 猴子上岸序列
  public static FileWriter fw;
  private ChoseBridge strategy;// 策略
  private static Scanner in = new Scanner(System.in);

  
  // RI:
  //    ladders，一旦生成则长度不能发生改变
  //    monkeys，初始和结束均为空
  //    datas，一旦生成不能改变
  //    count，连续的自然数，最后等于猴子个数
  //    monkeyssequence，最后长度为猴子总数
  //
  // AF:
  //    ladders表示整个梯子.
  //    monkeys保存了整个猴子列表
  //    datas包含了整个数据集
  //    subthreads存储整个猴子线程列表
  //    monkeyssequence将猴子过河结果按时间顺序排序保存
  //
  // rep from exposure:
  //    datas, subthreads, count为private型.
  
  /**
   * 生成梯子.
   * @param n 梯子个数，为一个自然数.
   * @param h 每个梯子上的踏板数，初始化为20.
   */
  private void generateLadders(int n, int h) {
    Ladder l = null;// 梯子
    
    for(int i = 1;i <= n;i++) {
      List<Map<Rung, Monkey>> temp = Collections.synchronizedList(new ArrayList<Map<Rung,Monkey>>());// 踏板:猴子
      for (int j = 1; j <= h; j++) {
        Map<Rung, Monkey> map = Collections.synchronizedMap(new HashMap<>());// 踏板:猴子
        map.put(new Rung(j), null);
        temp.add(map);
      }
      l = new Ladder(i, "");
      ladders.put(l, temp);
    }
    
    // 将某处一个设置一只猴子
//    ladders.get(new Ladder(1)).get(19).put(new Rung(20), new Monkey(100, 10, ladders));
  }
  
  /**
   * 根据datas生成猴子保存在monkeys里.
   * @throws InterruptedException 休眠是否被打断.
   */
  private void generateMonkeys() throws InterruptedException {
    try {
      fw = new FileWriter(new File("src/output/log.txt"));
      fw.write("");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("日志文件打开失败！");
    }
    threadSignal = new CountDownLatch(datas.size());
    // 生成猴子
    Monkey monkey = null;
    int allmonkeysnum = datas.size();
    int circletime = 0;
    int permonkeysnum = allmonkeysnum;
    // 先找到周期时间和周期生产数
    for(int i = 0;i < allmonkeysnum-1;i++) {
      if(!datas.get(i)[0].equals(datas.get(i+1)[0])) {
        permonkeysnum = i+1;
        circletime = Integer.valueOf(datas.get(i+1)[0]);
        break;
      }
    }
    
    // 开始生产
    int batch = 1;
    strategy = choseStrategy();
    System.out.println("本次过河猴子总数为: " + datas.size() + "只");
    Simulator.writelog("本次过河猴子总数为: " + datas.size() + "只");
    System.out.println("梯子总数为：" + ladders.size());
    Simulator.writelog("梯子总数为：" + ladders.size());
    System.out.println("每" + circletime + "秒" + "生成" + permonkeysnum + "只猴子");
    Simulator.writelog("每" + circletime + "秒" + "生成" + permonkeysnum + "只猴子");
    Random random = new Random();// 随机选择策略
    
    // 记录所有猴子状态
    for(String[] s:datas) {
      Simulator.writelog("ID为" + s[1] + "的猴子方向为" + s[2] + ",速度为" + s[3]);
    }
    Thread.sleep(1000);
    System.out.println("正在生产…………");
    while (count < allmonkeysnum) {
      System.out.println("正在生产第" + batch + "批中…………");
      batch++;
      // 当不足生成N-k个
      if(count + permonkeysnum > allmonkeysnum) {
        for(int i = 0;i < allmonkeysnum - count;i++) {
          if(strategy == null) {
            int strat = random.nextInt(3);
            if(strat == 0) {
              monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new Strategyone());
            }
            if(strat == 1) {
              monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new TheClosestSpeed());
            }
            if(strat == 2) {
              monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new Closestrealspeed());
            }
          }else {
            monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), strategy);
          }
          monkeys.add(monkey);
          count++;
        return;
        }
      }
      
      // 当可以生成k个
      for(int i = 0;i < permonkeysnum;i++) {
        if(strategy == null) {
          int strat = random.nextInt(3);
          if(strat == 0) {
            monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new Strategyone());
          }
          if(strat == 1) {
            monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new TheClosestSpeed());
          }
          if(strat == 2) {
            monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), new Closestrealspeed());
          }
        }else {
          monkey = new Monkey(Integer.valueOf(datas.get(count)[1]), Integer.valueOf(datas.get(count)[3]), ladders, datas.get(count)[2], threadSignal, Integer.valueOf(datas.get(count)[0]), strategy);
        }        
        monkeys.add(monkey);
        count++;
      }
      Thread.sleep(circletime*1000);
    }
    System.out.println("生产完成。");
  }
  
  /**
   * 开始进行模拟过河.
   */
  private void startsimulator() {
    for(Monkey monkey:monkeys) {
      Thread thread = new Thread(monkey);
      subthreads.add(thread);
      thread.start();
    }
  }
 
  /**
   * 输出当前过河情况.
   */
  private static void print() {
    for(Ladder ladder:ladders.keySet()) {
      List<Map<Rung, Monkey>> rungs = ladders.get(ladder);
      Map<Rung, Monkey> rung = null;
      for(int i = 0;i < rungs.size();i++) {
        rung = rungs.get(i);
        for(Rung r:rung.keySet()) {
          if(rung.get(r) == null) {
            System.out.print("---  ");
          } else {
            System.out.print(String.format("%03d", rung.get(r).getID()) + "  ");
          }
        }
      }
      System.out.println();
    }
    System.out.println();
  }
  
  /**
   * 写日志.
   * @param context 内容.
   */
  public static void writelog(String context) {
     try {
      fw.write(context + "\n");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("文件写入失败！");
    }
  }
  
  /**
   * 通过文件建立模拟器.
   * @throws InterruptedException
   */
  private void readData() throws InterruptedException {
    int choice;
    
    System.out.println("**********");
    System.out.println("1:Competition_1.txt");
    System.out.println("2:Competition_2.txt");
    System.out.println("3:Competition_3.txt");
    System.out.println("4:输入文件名");
    System.out.println("5:Competition_4.txt");
    choice = in.nextInt();
    
    switch (choice) {
      case 1:
        fileData("Competition_1.txt");
        break;
      case 2:
        fileData("Competition_2.txt");
        break;
      case 3:
        fileData("Competition_3.txt");
        break;
      case 4:
        System.out.println("请输入文件名:");
        in.nextLine();
        String filename = in.nextLine();
        fileData(filename);
        break;
      case 5:
        fileData("Competition_4.txt");
        break;
      default:
        System.out.println("输入有误。");
        break;
    }
    
  }
  
  /**
   * 从文件提取信息.
   * @param filename 文件名.
   * @throws InterruptedException
   */
  private void fileData(String filename) throws InterruptedException {
    int rungsnum = 0;
    int laddersnum = 0;
    
    File file = new File("src/input/" + filename);
    try {
      BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String line;
      while ((line = bf.readLine()) != null) {
        // 梯子数
        if(line.startsWith("n")) {
          laddersnum = Integer.valueOf(line.split("=")[1]);
        } else if (line.startsWith("h")) {// 踏板数
          rungsnum = Integer.valueOf(line.split("=")[1]);
        } else {// 猴子
          int length = line.length();
          String[] data = line.substring(8, length-1).split(",");// 获取数据
          datas.add(data);
        }
      }
      bf.close();
      // 生成梯子
      generateLadders(laddersnum, rungsnum);
      
      // 开始生成猴子
      generateMonkeys();
    } catch (FileNotFoundException e) {
      System.out.println("文件打开失败。");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("文件关闭失败。");
    }
  }
  
  /**
   * 策略选择.
   * @return 所选的策略.
   */
  private ChoseBridge choseStrategy() {
    while (true) {
      System.out.println("*************");
      System.out.println("请选择策略：");
      System.out.println("1:strategy1");
      System.out.println("2:The closest speed I");
      System.out.println("3:The closest speed II");
      System.out.println("4:随机选择策略");
      int choice = in.nextInt();
      
      switch (choice) {
        case 1:
          return new Strategyone();
        case 2:
          return new TheClosestSpeed();
        case 3:
          return new Closestrealspeed();
        case 4:
          return null;
        default:
          System.out.println("输入错误，请重新输入！");
          break;
      }
    }
  }
  
  /**
   * 猴子生成器.
   * @throws InterruptedException
   */
  private void inputData() throws InterruptedException {
    int rungsnum = 20;
    System.out.println("请输入梯子个数(1-10)：");
    int laddersnum = in.nextInt();
    System.out.println("请输入踏板个数(默认20)：");
    rungsnum = in.nextInt();
    System.out.println("请输入产生周期时间(1-5)：");
    int circletime = in.nextInt();
    System.out.println("请输入猴子总数(2-1000)：");
    int allmonkeysnum = in.nextInt();
    System.out.println("请输入周期产生猴子数(1-50)：");
    int permonkeysnum = in.nextInt();
    System.out.println("请输入猴子最大速度(5-10)：");
    int MV = in.nextInt();
    
    // 随机生成方向和速度
    int bornnum = 0;
    int borntime = 0;
    int ID = 1;
    Random random = new Random();
    while (bornnum < allmonkeysnum) {
      // 随机生成方向
      String direction;
      int direct = random.nextInt(2);
      if(direct == 0) {
        direction = "L->R";
      }else {
        direction = "R->L";
      }
      // 随机生成速度
      int v = random.nextInt(MV-1)+1;
      
      // 加入到datas里
      String[] data = new String[4];
      data[0] = String.valueOf(borntime);
      data[1] = String.valueOf(ID);
      data[2] = direction;
      data[3] = String.valueOf(v);
      datas.add(data);
      ID++;
      bornnum++;
      // 每次生成
      if((bornnum % permonkeysnum) == 0){
        borntime += circletime;
      }
    }
    
    // 生成梯子
    generateLadders(laddersnum, rungsnum);
    
    // 开始生成猴子
    generateMonkeys();
  }
  
  /**
   * 计算吞吐率和公平性.
   * @param simulator 模拟器对象.
   * @throws InterruptedException
   * @throws AWTException
   */
  private static void calculateTime(Simulator simulator) throws InterruptedException{
    // 开始计时
    // 猴子总数
    int monkeysnum = monkeys.size();
    System.out.println("开始过河。");
    Simulator.writelog("开始过河。");
    long start = System.currentTimeMillis();
    simulator.startsimulator();
    while (!monkeys.isEmpty()) {
      print();
      System.out.println("&&还差猴子：" + monkeys.size());
      Thread.sleep(1000);
    }
    threadSignal.await();//等待所有子线程执行完
    long end = System.currentTimeMillis();
    System.out.println("过桥完成，耗时：" + (end - start) + "ms");
    Simulator.writelog("过桥完成，耗时：" + (end - start) + "ms");
    System.out.println("吞吐率为" + (double)monkeysnum*1000/(end - start));
    Simulator.writelog("吞吐率为" + (double)monkeysnum*1000/(end - start));
    calculatefairness();
    try {
      fw.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("文件关闭失败！");
    }
    return;
  }
  
  /**
   * 计算公平性.
   */
  private static void calculatefairness() {
    int monkeynum = monkeyssequence.size();
    int sigma = 0;//  记录sigma求和
    double denominator = monkeynum*(monkeynum-1)/(double)2;// 分母
    for(int i = 0;i < monkeynum;i++) {
      for(int j = 0;j < monkeynum;j++) {
        if(i < j) {
          if(monkeyssequence.get(i).getBorntime() <= monkeyssequence.get(j).getBorntime()) {
            sigma ++;
          }else {
            sigma --;
          }
        } else if (i > j) {
          if(monkeyssequence.get(i).getBorntime() >= monkeyssequence.get(j).getBorntime()) {
            sigma ++;
          }else {
            sigma --;
          }
        }
      }
    }
    Simulator.writelog("公平性为：" + (double)sigma/denominator);
    System.out.println("公平性为：" + (double)sigma/denominator);
  }
  
  /**
   * 主程序.
   * @param args
   * @throws InterruptedException
   * @throws AWTException
   */
  public static void main(String[] args) throws InterruptedException {
    Simulator simulator = new Simulator();
    int choice = 0;// 选择输入方式
    while (true) {
      Simulator.ladders.clear();
      Simulator.monkeys.clear();
      simulator.datas.clear();
      simulator.subthreads.clear();
      simulator.count = 0;
      Simulator.monkeyssequence.clear();
      threadSignal = null;
      System.out.println("*******************");
      System.out.println("请选择数据输入方式：");
      System.out.println("1:读文件");
      System.out.println("2:手动输入");
      System.out.println("0:退出");
      choice = in.nextInt();
      
      switch (choice) {
        case 1:
          simulator.readData();
          calculateTime(simulator);
          break;
        case 2:
          simulator.inputData();
          calculateTime(simulator);
          break;
        case 0:
          in.close();
          System.exit(0);
          break;
         default:
           System.out.println("输入不合法，请重新输入。");
           break;
      }
    }
  }

}
