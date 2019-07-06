package strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;

/**
 * 策略一：优先选择没有梯子的猴子，若所有梯子上都有猴子，则选择没有对向的猴子，若有多个，随机选，
 * 若没有满足条件的，等待.
 * @author 王程
 *
 */
public class Strategyone implements ChoseBridge {
  @Override
  public Ladder chosenbridge(Monkey monkey, Map<Ladder, List<Map<Rung, Monkey>>> ladders) {
    synchronized (Rung.class) {
      List<Ladder> forwardladder = new ArrayList<Ladder>();// 保存当前所有正向的梯子
      boolean existforward = false;
      Random random = new Random();
      
      for(Ladder ladder:ladders.keySet()) {
        List<Map<Rung, Monkey>> rungs = ladders.get(ladder);
        // 空梯子且同向直接上
        if(Ladder.isEmpty(rungs)) {
          ladder.setdirection(monkey.getDirection());
          return ladder;
        }
      
        // 看是否有反向猴子
ladderloop:for(int i = 0;i < rungs.size();i++) {
          for(Rung rung:rungs.get(i).keySet()) {
            Monkey m = rungs.get(i).get(rung);
            if(m != null) {
              // 反向直接找下一个梯子
              if(!m.getDirection().equals(monkey.getDirection())) {
                break ladderloop;
              }
              else {// 先记录正向的，如果没有空的则走这条路
                forwardladder.add(ladder);
                existforward = true;
              }
            }
          }
        }
    }
      if(existforward) {
        return forwardladder.get(random.nextInt(forwardladder.size()));
      }
      return null;
    }
  }
}
