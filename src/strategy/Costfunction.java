package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;

/**
 * 使用代价函数计算每个梯子的代价，代价越低越好.（(n/d)*|selfv-frontv|）
 * @author 王程
 *
 */
public class Costfunction implements ChoseBridge{
  @Override
  public Ladder chosenbridge(Monkey monkey, Map<Ladder, List<Map<Rung, Monkey>>> ladders) {
    synchronized (Rung.class) {
      List<Ladder> forwardladder = new ArrayList<Ladder>();// 保存当前所有正向的梯子
      Ladder lowercostladder = null;// 记录速度离自己最近的梯子
      int h = 0;// 记录踏板数
      double cost = 200;
      boolean existforward = false;
      
      // 遍历梯子
      for(Ladder ladder:ladders.keySet()) {
        List<Map<Rung, Monkey>> rungs = ladders.get(ladder);
        h = rungs.size();
        // 空梯子且同向直接上
        if(Ladder.isEmpty(rungs)) {
          ladder.setdirection(monkey.getDirection());
          return ladder;
        }
      
        // 看是否有反向猴子
ladderloop1: for(int i = 0;i < rungs.size();i++) {
          for(Rung rung:rungs.get(i).keySet()) {
            Monkey m = rungs.get(i).get(rung);
            if(m != null) {
              // 反向直接找下一个梯子
              if(!m.getDirection().equals(monkey.getDirection())) {
                break ladderloop1;
              }
              else {// 先记录正向的，如果没有空的则走这条路
                forwardladder.add(ladder);
                existforward = true;
                break ladderloop1;
              }
            }
          }
        }
      }
      if(existforward) {
        // 提取所有正向梯子的速度
        for(Ladder l:forwardladder) {
          List<Map<Rung, Monkey>> rungs = ladders.get(l);
          // 计算代价
          double thiscost = costfunction(monkey, rungs);
          if(thiscost < cost) {
            lowercostladder = l;
            cost = thiscost;
          }
        }
        return lowercostladder;
      }
      return null;
    }
  }
  
  private double costfunction(Monkey monkey, List<Map<Rung, Monkey>> rungs) {
    double frontv = 1;// 前面猴子速度
    int n = 0;// 该梯子上猴子数
    int d = 1;// 该梯子上第一个猴子距离
    boolean find = false;// 找到第一个猴子
    
    // L->R
    if(monkey.getDirection().equals("L->R")) {
      for(int i = 0;i < rungs.size();i++) {
        for(Rung rung:rungs.get(i).keySet()) {
          if(rungs.get(i).get(rung) != null) {
            n++;
            if(!find) {
              frontv = rungs.get(i).get(rung).getRealV();
              d = i;
              find = true;
            }
          }
        }
      }
    }
    
    // L->R
    if(monkey.getDirection().equals("L->R")){
      for(int i = rungs.size()-1;i >= 0;i--) {
        for(Rung rung:rungs.get(i).keySet()) {
          if(rungs.get(i).get(rung) != null) {
            n++;
            if(!find) {
              frontv = rungs.get(i).get(rung).getRealV();
              d = i;
              find = true;
            }
          }
        }
      }
    }
    
    // 计算代价
    return n*(double)Math.abs(monkey.getV() - frontv)/d;
  }
}
