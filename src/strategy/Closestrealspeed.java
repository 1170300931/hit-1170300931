package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;

/**
 * 策略二：选择最近猴子的速度离自己最近的梯子
 * @author 王程
 *
 */
public class Closestrealspeed implements ChoseBridge {

  @Override
  public Ladder chosenbridge(Monkey monkey, Map<Ladder, List<Map<Rung, Monkey>>> ladders) {
    synchronized (Rung.class) {
      List<Ladder> forwardladder = new ArrayList<Ladder>();// 保存当前所有正向的梯子
      Ladder closestladder = null;// 记录速度离自己最近的梯子
      int h = 0;// 记录踏板数
      int closestspeedmonkey = 10;
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
                  if(Math.abs(m.getRealV() - monkey.getV()) < closestspeedmonkey) { // 速度更近
                    closestspeedmonkey = Math.abs(m.getRealV() - monkey.getV());
                    closestladder = l;
                    break ladderloop2;
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
                  if(Math.abs(m.getRealV() - monkey.getV()) < closestspeedmonkey) { // 速度更近
                    closestspeedmonkey = Math.abs(m.getRealV() - monkey.getV());
                    closestladder = l;
                    break ladderloop3;
                    }
                  }
                }
              }
            }
          }
        return closestladder;
      }
      return null;
    }
  }
}
