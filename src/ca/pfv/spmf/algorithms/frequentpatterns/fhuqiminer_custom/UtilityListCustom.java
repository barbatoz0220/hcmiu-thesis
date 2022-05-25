package ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom;


import java.util.ArrayList;
public class UtilityListCustom {
    private ArrayList<QitemCustom> itemsetName;
    private long sumIUtils;
    private long sumRUtils;
    private long twu;

    private ArrayList<QItemTransCustom> qItemTrans = null;

    public UtilityListCustom(ArrayList<QitemCustom> qitemset, long twu) {
        // this.prefix="";
        this.itemsetName = new ArrayList<QitemCustom>();
        this.itemsetName = qitemset;
        this.sumIUtils = 0;
        this.sumRUtils = 0;
        this.twu = twu;
        this.qItemTrans = new ArrayList<QItemTransCustom>();
    }

    public UtilityListCustom(ArrayList<QitemCustom> qitemset) {
        // this.prefix="";
        this.itemsetName = new ArrayList<QitemCustom>();
        this.itemsetName = qitemset;
        this.sumIUtils = 0;
        this.sumRUtils = 0;
        this.twu = 0;
        this.qItemTrans = new ArrayList<QItemTransCustom>();
    }

    public UtilityListCustom(QitemCustom name) {
        this.itemsetName = new ArrayList<QitemCustom>();
        this.itemsetName.add(name);
        this.sumIUtils = 0;
        this.sumRUtils = 0;
        this.twu = 0;
        qItemTrans = new ArrayList<QItemTransCustom>();
    }

    /**
     * Constructor
     * @param qitem a q-item
     * @param twu the twu
     */
    public UtilityListCustom(QitemCustom qitem, long twu) {
        this.itemsetName = new ArrayList<QitemCustom>();
        this.itemsetName.add(qitem);
        this.sumIUtils = 0;
        this.sumRUtils = 0;
        this.twu = twu;
        qItemTrans = new ArrayList<QItemTransCustom>();
    }

    public UtilityListCustom() {

    }

    public void addTWU(int twu) {
        this.twu += twu;
    }

    public void setTWUtoZero() {
        this.twu = 0;
    }

    public void addTrans(QItemTransCustom qTid, long twu) {
        this.sumIUtils += qTid.getEu();
        this.sumRUtils += qTid.getRu();
        qItemTrans.add(qTid);
        this.twu += twu;
    }

    public void addTrans(QItemTransCustom qTid) {
        this.sumIUtils += qTid.getEu();
        this.sumRUtils += qTid.getRu();
        qItemTrans.add(qTid);
    }

    public long getSumIUtils() {
        return this.sumIUtils;
    }

    public long getSumRUtils() {
        return this.sumRUtils;
    }

    public void setSumIUtils(long x) {
        this.sumIUtils = x;
    }

    public void setSumRUtils(long x) {
        this.sumRUtils = x;
    }

    public long getTwu() {
        return twu;
    }

    public void setTwu(long twu) {
        this.twu = twu;
    }

    public ArrayList<QitemCustom> getItemsetName() {
        return this.itemsetName;
    }

    public QitemCustom getSingleItemsetName() {
        return this.itemsetName.get(0);
    }

    public ArrayList<QItemTransCustom> getQItemTransCustom() {
        return qItemTrans;
    }

    public void setQItemTransCustom(ArrayList<QItemTransCustom> elements) {
        this.qItemTrans = elements;
    }

    public QItemTransCustom QitemTransAdd(QItemTransCustom a, QItemTransCustom b) {
        QItemTransCustom x;
        x = new QItemTransCustom(a.getTid(), a.getEu() + b.getEu(), a.getRu() + b.getRu());
        return x;
    }

    public void addUtilityList2(UtilityListCustom next) {
        ArrayList<QItemTransCustom> temp = next.getQItemTransCustom();
        ArrayList<QItemTransCustom> mainlist = new ArrayList<QItemTransCustom>();
        this.sumIUtils += next.getSumIUtils();
        this.sumRUtils += next.getSumRUtils();
        this.twu += next.getTwu();

        if (qItemTrans.size() == 0) {
            for (int k = 0; k < temp.size(); k++) {
                qItemTrans.add(temp.get(k));
            }
        } else {
            int i = 0, j = 0;
            // System.out.println("qItemTrans="+qItemTrans.size()+" temp="+temp.size());

            while (i < qItemTrans.size() && j < temp.size()) {
                int t1 = qItemTrans.get(i).getTid();
                int t2 = temp.get(j).getTid();
                if (t1 > t2) {
                    mainlist.add(temp.get(j));
                    j++;
                } else if (t1 < t2) {
                    mainlist.add(qItemTrans.get(i));
                    i++;
                } else {

                    mainlist.add(t1, QitemTransAdd(qItemTrans.get(i), temp.get(j)));
                }

            }
            if (i == qItemTrans.size()) {
                while (j < temp.size()) {
                    mainlist.add(temp.get(j++));
                }
            } else if (j == temp.size()) {
                while (i < qItemTrans.size()) {
                    mainlist.add(qItemTrans.get(i++));
                }
            }
            qItemTrans.clear();
            qItemTrans = mainlist;

        }

    }

    public String toString() {
        String str = "\n=================================\n";
        str += itemsetName + "\r\n";
        str += "sumEU=" + this.sumIUtils + " sumRU=" + this.sumRUtils + " twu=" + twu + "\r\n";

        for (int i = 0; i < qItemTrans.size(); i++) {
            str += qItemTrans.get(i).toString() + "\r\n";
        }
        str += "=================================\n";
        return str;
    }

    /**
     * Get the q-item transaction count
     * @return the count
     */
    public int getqItemTransLength() {
        if (qItemTrans == null)
            return 0;
        else
            return qItemTrans.size();
    }
}

