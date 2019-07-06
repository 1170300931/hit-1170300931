package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;

/**
 * 最近速度策略：
 * 1:有空梯子则选空梯子.
 * 2:没有空梯子则在无对向行进猴子的梯子中选:
 *    a:选择这样的梯子：离自己最近的猴子的速度略大于自己
 *    b:若上述梯子不存在，则选择离自己最近的猴子的速度最大的梯子
 * 3:若都不存在，则等待
 * @author 王程
 *
 */
public class TheClosestSpeed implements ChoseBridge{
  
  @Override
  public Ladder chosenbridge(Monkey monkey, Map<Ladder, List<Map<Rung, Monkey>>> ladders) {
    synchronized (Rung.class) {
      List<Ladder> forwardladder = new ArrayList<Ladder>();// 保存当前所有正向的梯子
      Ladder slowladder = null;// 记录比自己慢的梯子
      Ladder fastladder = null;// 记录比自己快的梯子
      int h = 0;// 记录踏板数
      int slowmonkey = 0;
      int fastmonkey = 10;
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
          // L->R从左往右扫
          if(monkey.getDirection().equals("L->R")) {
ladderloop2:for(int i = 0;i < rungs.size();i++) {
              for(Rung rung:rungs.get(i).keySet()) {
                Monkey m = rungs.get(i).get(rung);
                if(m != null) { // 查找离自己最近的猴子
                  if(m.getV() < monkey.getV()) { // 比自己速度慢
                    if(m.getV() > slowmonkey) { // 若该猴子是慢中快的，保存这个
                      slowmonkey = m.getV();
                      slowladder = l;
                      break ladderloop2;
                    }
                  }
                  else { // 不超过自己
                    if(m.getV() < fastmonkey) { // 若该猴子是快中慢的，保存这个
                      fastmonkey = m.getV();
                      fastladder = l;
                      break ladderloop2;
                    }
                  }
                }
              }
            }
          }
          
          // R-L从右往左扫
          if(monkey.getDirection().equals("R->L")) {
ladderloop3:for(int i = h-1;i >= 0;i--) {
              for(Rung rung:rungs.get(i).keySet()) {
                Monkey m = rungs.get(i).get(rung);
                if(m != null) { // 查找离自己最近的猴子
                  if(m.getV() < monkey.getV()) { // 比自己速度慢
                    if(m.getV() > slowmonkey) { // 若该猴子是慢中快的，保存这个
                      slowmonkey = m.getV();
                      slowladder = l;
                      break ladderloop3;
                    }
                  }
                  else { // 不超过自己
                    if(m.getV() < fastmonkey) { // 若该猴子是快中慢的，保存这个
                      fastmonkey = m.getV();
                      fastladder = l;
                      break ladderloop3;
                    }
                  }
                }
              }
            }
          }
          // 存在比自己速度大的猴子
          if(fastladder != null) {
            fastladder.setdirection(monkey.getDirection());
            return fastladder;
          }
          slowladder.setdirection(monkey.getDirection());
          return slowladder;
        }
      }
      return null;
    }
  }
}
