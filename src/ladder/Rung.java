package ladder;

/**
 * 这是梯子上的踏板数据结构.
 * @author 王程
 *
 */
public class Rung {
  // 踏板编号
  private int ID = 0;  
  // RI:
  //    ID是一个自然数
  // AF:
  //    ID表示该踏板的编号，一个梯子上不同踏板编号不同.
  // safety from rep exposure:
  //    没有任何返回方法
  
  /**
   * 传入编号创建一个新的踏板.
   * @param ID 踏板编号，是一个自然数.
   */
  public Rung(int ID) {
    this.ID = ID;
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
    Rung other = (Rung) obj;
    if (ID != other.ID)
      return false;
    return true;
  }
}
