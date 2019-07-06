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
 * �������ݽṹ.
 * @author ����
 *
 */
public class Monkey implements Runnable{
  private final Map<Ladder, List<Map<Rung, Monkey>>> ladders = null;
  private final int ID = 0;// ���
  private final String direction = null;// ����
  private int v = 0;// ��ʼ�ٶ�
  private boolean onRung = false;// �ú����Ƿ��ڰ���
  private CountDownLatch threadsSignal;// �ȴ����߳̽���
  private int realv = 0;
  private int borntime = 0;// ����ʱ�䣬���ڼ��㹫ƽ�Ե�
  private ChoseBridge strategy;
//  private Ladder ladderlock = new Ladder(0, "1");// rung��
  
  // RI:
  //    ID����Ȼ��
  //    direction��string���ͣ�ֻ���ǣ�R->L��L->R
  //    v������������[1,MV]֮��
  //    threadsSignal��ĸ�������Ϊ����
  //
  // AF:
  //    ����ADT��ʾһֻ�Ѿ�׼�������ӵĺ���.
  //
  // safety from rep exposure:
  //    ֻ���������ط�����
  //      getID���ص�ID��һ��������������
  //      getDirection����ʱ���з����Կ���
  
  /**
   * ͨ����������һֻ�º���.
   * @param ID ���ӵı�ţ���Ȼ��.
   * @param v ���ӵ��ٶȣ�����[1,MV]������.
   * @param ladders ��ǰ�������ӣ����ں��ӻ�֪����״̬.
   * @param direction ������Ҫ��ǰ������.
   * @param threadsSignal �����ӹ��Ӻ���ά���Ľ����б��н��ý���ɾ��.
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
   * ����ID.
   * @return ���غ��ӵ�ID���͹���ʱ��һ��.
   */
  public int getID() {
    return this.ID;
  }
  
  /**
   * ���ط���.
   * @return �͹���ʱ��һ��.
   */
  public String getDirection() {
    return this.direction;
  }
  
  /**
   * �����ٶ�.
   * @return �͹���ʱ��һ��.
   */
  public int getV() {
    return this.v;
  }
  
  /**
   *  ������ʵ�ٶ�.
   */
  public int getRealV() {
    return this.realv;
  }
  
  /**
   * ���س���ʱ��.
   * @return ������.
   */
  public int getBorntime() {
    return this.borntime;
  }
  
  /**
   * ��дrun�������߳�����.
   */
  @Override
  public void run() {
    Ladder chosenladder = null;// ѡ�������
    List<Map<Rung, Monkey>> rungs = new ArrayList<Map<Rung,Monkey>>();// �������ϵİ�
    int h = 0;// ���Ӹ���
    
    synchronized (this) {
      // ����ѡ������
      while (!this.onRung) {
        chosenladder = this.strategy.chosenbridge(this, ladders);
        rungs = ladders.get(chosenladder);
        if(chosenladder == null) {
          continue;
        }
        h = rungs.size();
        synchronized (rungs) {
          // ѡ������ֱ��������
          // L->R
          if(this.direction.equals("L->R") && chosenladder.getdirection().equals("L->R")) {
            if (rungs.get(0).get(new Rung(1)) == null) {
              rungs.get(0).put(new Rung(1), this);
              Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����IDΪ" + chosenladder.getID() + "������, ��ǰλ��Ϊ" + 1);
              this.onRung = true;
            }
          }
          // R->L
          if(this.direction.equals("R->L") && chosenladder.getdirection().equals("R->L")) {
            if (rungs.get(h-1).get(new Rung(h)) == null) {
              rungs.get(h-1).put(new Rung(h), this);
              Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����IDΪ" + chosenladder.getID() + "������, ��ǰλ��Ϊ" + h);
              this.onRung = true;
            }
          }
        }
      }
    }
    
    if (!this.onRung) {
      // �ȴ�������
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
      // ������
      synchronized (rungs) {
        // L->R
        if(this.direction.equals("L->R") && chosenladder.getdirection().equals("L->R")) {
          if (rungs.get(0).get(new Rung(1)) == null) {
            Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����IDΪ" + chosenladder.getID() + "������, ��ǰλ��Ϊ" + 1);
            rungs.get(0).put(new Rung(1), this);
            this.onRung = true;
          }
        }
        // R->L
        if(this.direction.equals("R->L") && chosenladder.getdirection().equals("R->L")) {
          if (rungs.get(h-1).get(new Rung(h)) == null) {
            Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����IDΪ" + chosenladder.getID() + "������, ��ǰλ��Ϊ" + h);
            rungs.get(h-1).put(new Rung(h), this);
            this.onRung = true;
          }
        }
      }
    }
    
    // ǰ��
    if(this.onRung) {
      // L->R
      if(this.direction.equals("L->R")) {
        int selfposition = 0;// ��¼�Լ���ǰλ��(�����±꣬ʵ���ϵ���ID-1)
        boolean resist = false;// ����ǰ����û�к����赲
        int frontposition = 0;// ��¼ǰ���赲���ӵ�λ��
        
        while (true) {
          synchronized (rungs) {
            // ���ǰ��v���Ƿ��к���
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
              
            // ǰ��<=v ��û����,�����Լ�������
            if(!resist) {
              realv = v;// û������Ϊ�Լ��ٶ�
              // ���յ����v
              if (selfposition + this.v <= h-1) {
                rungs.get(selfposition + v).put(new Rung(selfposition + v + 1), this);
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ��Ӵ�λ��" + selfposition + "����λ��" + (selfposition + v));
                selfposition += v;
              }else {// ֱ�ӵ��԰�
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                // ����԰���������
                System.out.println("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + String.valueOf(this.ID) + "�ĺ����Ѿ�����԰�");
                Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + String.valueOf(this.ID) + "�ĺ����Ѿ�����԰�");
                Simulator.monkeys.remove(this);
                threadsSignal.countDown();//�߳̽���ʱ��������1
                return;
              }
            }
              
            // ǰ��<=v ���к���,�ܵ����ĺ���
            if(resist) {
              this.realv = rungs.get(frontposition).get(new Rung(frontposition + 1)).v;// ������Ϊǰ�������ٶ�
              rungs.get(frontposition - 1).put(new Rung(frontposition), this);
              Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ��Ӵ�λ��" + selfposition + "����λ��" + (frontposition - 1));
              // ��ԭ�ز�����ɾ��ԭ��λ�ú���
              if(selfposition != (frontposition - 1)) {
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����λ��" + selfposition + "��������ֻ�ܵȴ�");
              }
              selfposition = frontposition - 1;
            }
          }
          // 1�����һ��
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
        int selfposition = h-1;// ��¼�Լ���ǰλ��(�����±꣬ʵ���ϵ���ID-1)
        boolean resist = false;// ����ǰ����û�к����赲
        int frontposition = 0;// ��¼ǰ���赲���ӵ�λ��
        
        while (true) {
          synchronized (rungs) {
            // ���ǰ��v���Ƿ��к���
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
              
            // ǰ��<=v ��û����,�����Լ�������
            if(!resist) {
              realv = v;// û������Ϊ�Լ��ٶ�
              // ���յ����v
              if (selfposition - this.v >= 0) {
                rungs.get(selfposition - v).put(new Rung(selfposition - v + 1), this);
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ��Ӵ�λ��" + selfposition + "����λ��" + (selfposition - v));
                selfposition -= v;
              }else {// ֱ�ӵ��԰�
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                System.out.println("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + String.valueOf(this.ID) + "�ĺ����Ѿ�����԰�");

                // ����԰���������
                System.out.println("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + String.valueOf(this.ID) + "�ĺ����Ѿ�����԰�");
                Simulator.monkeys.remove(this);// ɾ���ú���
                threadsSignal.countDown();//�߳̽���ʱ��������1
                Simulator.monkeyssequence.add(this);// ���뵽����������
                return;
              }
            }
              
            // ǰ��<=v ���к���,�ܵ����ĺ���
            if(resist) {
              this.realv = rungs.get(frontposition).get(new Rung(frontposition + 1)).v;// ������Ϊǰ�������ٶ�
              rungs.get(frontposition + 1).put(new Rung(frontposition + 2), this);
              Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ��Ӵ�λ��" + selfposition + "����λ��" + (frontposition + 2));
              // ��ԭ�ز�����ɾ��ԭ��λ�ú���
              if(selfposition != (frontposition + 1)) {
                rungs.get(selfposition).put(new Rung(selfposition + 1), null);
                Simulator.writelog("����Ϊ" + chosenladder.getID() + ":" + "IDΪ" + this.getID() + "�ĺ�����λ��" + selfposition + "��������ֻ�ܵȴ�");
              }
              selfposition = frontposition + 1;
            }
          }
          // 1�����һ��
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
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ID;
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Rung other = (Rung) obj;
    if (ID != other.getID()) return false;
    return true;
  }
}
  