package ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom;
import java.util.Objects;

public class QitemCustom {
    private int item;
    private int quantityMin;
    private int quantityMax;

    public QitemCustom() {}
    // Range Q-items
    public QitemCustom(int i, int qMin, int qMax) {
        this.item = i;
        this.quantityMin = qMin;
        this.quantityMax = qMax;
    }

    // Exact Q-items
    public QitemCustom(int i, int q) {
        this.item = i;
        this.quantityMin = q;
        this.quantityMax = q;
    }

    public int getItem() {
        return this.item;
    }
    public int getQuantityMin() {
        return this.quantityMin;
    }
    public int getQuantityMax() {
        return this.quantityMax;
    }
    public void setItem(int i) {
        this.item = i;
    }
    public void setQuantityMin(int q) {
        this.quantityMin = q;
    }
    public void setQuantityMax(int q) {
        this.quantityMax = q;
    }

    public void copy(QitemCustom q) {
        this.item = q.item;
        this.quantityMin = q.quantityMin;
        this.quantityMax = q.quantityMax;
    }

    public boolean isRange() {
        if (this.quantityMin == this.quantityMax)
            return false;
        return true;
    }

    public String toString() {
        String str = "";
        if (!this.isRange())
            str += "(" + this.item + "," + this.quantityMin + ")";
        else
            str += "(" + this.item + "," + this.quantityMin + "," + this.quantityMax + ")";
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof QitemCustom)) {
            return false;
        }
        QitemCustom x = (QitemCustom) o;
        return item == x.item
                && quantityMin == x.quantityMin
                && quantityMax == x.quantityMax;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, quantityMin, quantityMax);
    }
}

