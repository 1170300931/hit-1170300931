package ladder;

import java.util.List;
import java.util.Map;
import monkey.Monkey;

/**
 * 这是梯子的数据结构.
 * @author 王程
 *
 */
public class Ladder {
  private int ID = 0;// ID编号
  private String direction = "";// 初始化方向防止锁死
  
  // RI:
  //    ID表示该梯子的标号，是唯一的.
  // AF:
  //    ID是对每个梯子的编号
  // safety from rep exposure:
  //    ID是不可变类型，唯一的返回方法使用了防御性切换指针.
  
  /**
   * 新生成一个梯子并传入编号.
   * @param ID 梯子的编号.
   */
  public Ladder(int ID, String direction) {
    this.ID = ID;
    this.direction = direction;
  }

  /**
   * 返回ID编号.
   * @return ID 梯子编号，是一个自然数.
   */
  public int getID() {
    int copy = 0;
    copy = this.ID;
    return copy;
  }

  /**
   * 返回梯子初始方向.
   * @return
   */
  public String getdirection() {
    return direction;
  }
  
  /**
   * 设置方向.
   * @param direction 设置方向
   * @return
   */
  public void setdirection(String direction) {
    this.direction = direction;
  }
  
  /**
   * 判断踏板是否为空.
   * @param rungs 该梯子上的所有踏板.
   * @return 如果空则返回true，否则返回false.
   */
  public synchronized static boolean isEmpty(List<Map<Rung, Monkey>> rungs) {
    for(Map<Rung, Monkey> rung:rungs) {
      for(Rung r:rung.keySet()) {
        if(rung.get(r) != null) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ID;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Ladder other = (Ladder) obj;
    if (ID != other.ID)
      return false;
    return true;
  }
}
