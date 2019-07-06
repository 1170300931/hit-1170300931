package strategy;

import java.util.List;
import java.util.Map;
import ladder.Ladder;
import ladder.Rung;
import monkey.Monkey;

/**
 * 策略选择接口.
 * @author 王程
 *
 */
public interface ChoseBridge {
  /**
   * 进行策略选择函数.
   * @param monkey 在思考的猴子.
   * @param ladders 所有梯子.
   * @return 选中的梯子.
   */
  public Ladder chosenbridge(Monkey monkey, Map<Ladder, List<Map<Rung, Monkey>>> ladders);
}
