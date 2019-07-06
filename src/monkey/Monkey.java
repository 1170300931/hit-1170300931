package monkey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import ladder.Ladder;
import ladder.Rung;
import simulator.Simulator;
import strategy.ChoseBridge;
import strategy.Closestrealspeed;

/**
 * 猴子数据结构.
 * @author 王程
 *
 */
public class Monkey implements Runnable{
  private Map<Ladder, List<Map<Rung, Monkey>>> ladders = null;
  private int ID = 0;// 编号
  private String direction = null;// 方向
  private int v = 0;// 初始速度
  private boolean onRung = false;// 该猴子是否在板上
  private CountDownLatch threadsSignal;// 等待子线程结束
  private int realv = 0;
  private int borntime = 0;// 出生时间，用于计算公平性的
  private ChoseBridge strategy;
//  private Ladder ladderlock = new Ladder(0, "1");// rung锁
  
  // RI:
  //    ID是自然数
  //    direction是string类型，只能是：R->L或L->R
  //    v是正整数，在[1,MV]之间
  //    threadsSignal里的个数不能为负数
  //
  // AF:
  //    整个ADT表示一只已经准备过梯子的猴子.
  //
  // safety from rep exposure:
  //    只有两个返回方法：
  //      getID返回的ID是一个基本数据类型
  //      getDirection返回时进行防御性拷贝
  
  /**
   * 通过参数构造一只新猴子.
   * @param ID 猴子的编号，自然数.
   * @param v 猴子的速度，属于[1,MV]的整数.
   * @param ladders 当前所有梯子，用于猴子获知梯子状态.
   * @param direction 猴子需要的前进方向.
   * @param threadsSignal 当猴子过河后在维护的进程列表中将该进程删除.
   */
  public Monkey(int ID, int v, Map<Ladder, List<Map<Rung, Monkey>>> ladders, String direction, CountDownLatch threadsSignal, int borntime, ChoseBridge strategy) {
    this.ID = ID;
    this.ladders = ladders;
    this.threadsSignal = threadsSignal;
    this.direction = direction;
    this.v = v;
    this.realv = v;
    this.borntime = borntime;
    this.strategy = strategy;
  }

  /**
   * 返回ID.
   * @return 返回猴子的ID，和构造时的一样.
   */
  public int getID() {
    return this.ID;
  }
  
  /**
   * 返回方向.
   * @return 和构造时的一样.
   */
  public String getDirection() {
    return this.direction;
  }
  
  /**
   * 返回速度.
   * @return 和构造时的一样.
   */
  public int getV() {
    return this.v;
  }
  
  /**
   *  返回真实速度.
   */
  public int getRealV() {
    return this.realv;
  }
  
  /**
   * 返回出生时间.
   * @return 正整数.
   */
  public int getBorntime() {
    return this.borntime;
  }
  
  /**
   * 重写run，进行线程内容.
   */
  @Override
  public void run() {
    Ladder chosenladder = null;// 选择的梯子
    List<Map<Rung, Monkey>> rungs = new ArrayList<Map<Rung,Monkey>>();// 该梯子上的板
    int h = 0;// 板子个数
    
    synchronized (this) {
      // 策略选择梯子
      while (!this.onRung) {
        chosenladder = this.strategy.chosenbridge(this, ladders);
        rungs = ladders.get(chosenladder);
        if(chosenladder == null) {
          continue;
        }
        h = rungs.size();
        synchronized (rungs) {
          // 选好若空直接上梯子
          // L->R
          if(this.direction.equals("L->R") && chosenladder.getdirection().equals("L->R")) {
            if (rungs.get(0).get(new Rung(1)) == null) {
              rungs.get(0).put(new Rung(1), this);
              Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子上ID为" + chosenladder.getID() + "的梯子, 当前位置为" + 1);
              this.onRung = true;
            }
          }
          // R->L
          if(this.direction.equals("R->L") && chosenladder.getdirection().equals("R->L")) {
            if (rungs.get(h-1).get(new Rung(h)) == null) {
              rungs.get(h-1).put(new Rung(h), this);
              Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子上ID为" + chosenladder.getID() + "的梯子, 当前位置为" + h);
              this.onRung = true;
            }
          }
        }
      }
    }
    
    if (!this.onRung) {
      // 等待上梯子
      // L->R
      if(this.direction.equals("L->R")) {
        while(rungs.get(0).get(new Rung(1)) != null && !rungs.get(0).get(new Rung(1)).equals(this));
      }
      // R->L
      if(this.direction.equals("R->L")) {
        while(rungs.get(h-1).get(new Rung(h)) != null && !rungs.get(h-1).get(new Rung(h)).equals(this));
      }
    }
    
    if (!this.onRung) {
      // 上梯子
      synchronized (rungs) {
        // L->R
        if(this.direction.equals("L->R") && chosenladder.getdirection().equals("L->R")) {
          if (rungs.get(0).get(new Rung(1)) == null) {
            Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子上ID为" + chosenladder.getID() + "的梯子, 当前位置为" + 1);
            rungs.get(0).put(new Rung(1), this);
            this.onRung = true;
          }
        }
        // R->L
        if(this.direction.equals("R->L") && chosenladder.getdirection().equals("R->L")) {
          if (rungs.get(h-1).get(new Rung(h)) == null) {
            Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子上ID为" + chosenladder.getID() + "的梯子, 当前位置为" + h);
            rungs.get(h-1).put(new Rung(h), this);
            this.onRung = true;
          }
        }
      }
    }
    
    // 前进
    if(this.onRung) {
      // L->R
      if(this.direction.equals("L->R")) {
        int selfposition = 0;// 记录自己当前位置(数组下标，实际上等于ID-1)
        boolean resist = false;// 计算前方有没有猴子阻挡
        int frontposition = 0;// 记录前方阻挡猴子的位置
        
        while (true) {
          synchronized (rungs) {
            // 检查前方v内是否有猴子
            resist = false;
            for(int i = selfposition + 1;i <= selfposition + this.v;i++) {
              if(i <= h-1 && rungs.get(i).get(new Rung(i+1)) != null) {
                resist = true;
                frontposition = i;
                break;
              } else if (i == h-1) {
                break;
              }
            }
              
            // 前方<=v 内没猴子,保持自己的速率
            if(!resist) {
              realv = v;// 没阻塞则为自己速度
              // 离终点大于v
              if (selfposition + this.v <= h-1) {
                rungs.get(selfposition + v).put(new Rung(selfposition + v + 1), this);
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子从位置" + selfposition + "到达位置" + (selfposition + v));
                selfposition += v;
              }else {// 直接到对岸
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                // 到达对岸结束进程
                System.out.println("梯子为" + chosenladder.getID() + ":" + "ID为" + String.valueOf(this.ID) + "的猴子已经到达对岸");
                Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + String.valueOf(this.ID) + "的猴子已经到达对岸");
                Simulator.monkeys.remove(this);
                threadsSignal.countDown();//线程结束时计数器减1
                return;
              }
            }
              
            // 前方<=v 内有猴子,跑到它的后面
            if(resist) {
              this.realv = rungs.get(frontposition).get(new Rung(frontposition + 1)).v;// 阻塞则为前方猴子速度
              rungs.get(frontposition - 1).put(new Rung(frontposition), this);
              Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子从位置" + selfposition + "到达位置" + (frontposition - 1));
              // 若原地不动则不删除原来位置猴子
              if(selfposition != (frontposition - 1)) {
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子在位置" + selfposition + "被阻塞，只能等待");
              }
              selfposition = frontposition - 1;
            }
          }
          // 1秒决策一次
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
      // R->L
      if(this.direction.equals("R->L")) {
        int selfposition = h-1;// 记录自己当前位置(数组下标，实际上等于ID-1)
        boolean resist = false;// 计算前方有没有猴子阻挡
        int frontposition = 0;// 记录前方阻挡猴子的位置
        
        while (true) {
          synchronized (rungs) {
            // 检查前方v内是否有猴子
            resist = false;
            for(int i = selfposition - 1;i >= selfposition - this.v;i--) {
              if(i >= 0 && rungs.get(i).get(new Rung(i+1)) != null) {
                resist = true;
                frontposition = i;
                break;
              } else if (i == 0) {
                break;
              }
            }
              
            // 前方<=v 内没猴子,保持自己的速率
            if(!resist) {
              realv = v;// 没阻塞则为自己速度
              // 离终点大于v
              if (selfposition - this.v >= 0) {
                rungs.get(selfposition - v).put(new Rung(selfposition - v + 1), this);
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子从位置" + selfposition + "到达位置" + (selfposition - v));
                selfposition -= v;
              }else {// 直接到对岸
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                System.out.println("梯子为" + chosenladder.getID() + ":" + "ID为" + String.valueOf(this.ID) + "的猴子已经到达对岸");

                // 到达对岸结束进程
                System.out.println("梯子为" + chosenladder.getID() + ":" + "ID为" + String.valueOf(this.ID) + "的猴子已经到达对岸");
                Simulator.monkeys.remove(this);// 删除该猴子
                threadsSignal.countDown();//线程结束时计数器减1
                Simulator.monkeyssequence.add(this);// 加入到过河序列中
                return;
              }
            }
              
            // 前方<=v 内有猴子,跑到它的后面
            if(resist) {
              this.realv = rungs.get(frontposition).get(new Rung(frontposition + 1)).v;// 阻塞则为前方猴子速度
              rungs.get(frontposition + 1).put(new Rung(frontposition + 2), this);
              Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子从位置" + selfposition + "到达位置" + (frontposition + 2));
              // 若原地不动则不删除原来位置猴子
              if(selfposition != (frontposition + 1)) {
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("梯子为" + chosenladder.getID() + ":" + "ID为" + this.getID() + "的猴子在位置" + selfposition + "被阻塞，只能等待");
              }
              selfposition = frontposition + 1;
            }
          }
          // 1秒决策一次
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
  }
}
