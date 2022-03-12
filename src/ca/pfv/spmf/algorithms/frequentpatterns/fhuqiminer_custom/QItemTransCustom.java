package ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom;

public class QItemTransCustom {
    private int transID;
    private int eUtil;
    private int rUtil;

    public QItemTransCustom(int tid, int eu, int ru)
    {
        this.transID=tid;
        this.eUtil=eu;
        this.rUtil=ru;
    }

    public int getTid()
    {
        return this.transID;
    }
    public int getEu()
    {
        return eUtil;
    }
    public int getRu() {return rUtil;}

    public int sum() {
        return eUtil + rUtil;
    }

    public String toString() {
        return transID + " " + eUtil + "	" + rUtil;
    }
}
