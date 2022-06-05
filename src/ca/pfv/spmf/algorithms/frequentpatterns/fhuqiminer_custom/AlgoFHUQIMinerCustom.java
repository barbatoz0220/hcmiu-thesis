package ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer.EnumCombination;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer.Qitem;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer.UtilityListFHUQIMiner;
import ca.pfv.spmf.tools.MemoryLogger;

public class AlgoFHUQIMinerCustom {

    // System
    /** Output file path */
    public String outputFile;
    /** Input file path */
    public String inputDatabase;
    /** Object to write results to file */
    private BufferedWriter writer_hqui = null;

    // Maps
    /** map of a qitem to  its TWU */
    private Hashtable<QitemCustom, Integer> twuTable;
    /** map of an item to its profit */
    private Hashtable<Integer, Integer> profitTable;
    /** map of transasction to its utility */
    private Hashtable<Integer, Integer> transactionUtility;
    /** map of an item to its FMAP */
    private Map<QitemCustom, Map<QitemCustom, Integer>> mapFMAP;
    /** map of an item to its PSet */
    private Map<QitemCustom, List<Integer>> mapPSet;
    private Map<Integer, Map<QitemCustom, List<QitemCustom>>> mapProjectedTrans;

    // Algorithm Parameters
    /** minimum utility threshold */
    private long minUtil;
    /** total utility */
    private long totalUtil;
    /** qrc coefficient */
    private int coefficient;
    /** combining method */
    private EnumCombination combiningMethod;

    // For Evaluation
    /** start and end time */
    private long startTime, endTime;
    /** percent */
    private float percent;
    /** number of HUQIs that have been found */
    private int HUQIcount = 0;
    /** number of utility lists */
    private int countUL = 0;
    /** number of construction for utility lists */
    private int countConstruct = 0;
    /** the current Qitem */
    private QitemCustom currentQitem;
    /** the size of a temporary buffer for storing itemsets */
    private final int BUFFERS_SIZE = 200;
    /** a temporary buffer for storing itemsets */
    private QitemCustom[] qItemsetBuffer = null;

    /** Constructor */
    public AlgoFHUQIMinerCustom() {
    }

    /** Execution function */
    public void runAlgorithm(String inputData, String inputProfit, float percentage, int coeff,
                             EnumCombination combineMethod, String output) throws IOException {
        System.gc();

        // Initialization
        MemoryLogger.getInstance().reset();
        startTime = System.currentTimeMillis();
        writer_hqui = new BufferedWriter(new FileWriter(output));
        qItemsetBuffer = new QitemCustom[BUFFERS_SIZE];
        coefficient = coeff;
        percent = percentage;
        combiningMethod = combineMethod;
        profitTable = new Hashtable<>();
        transactionUtility = new Hashtable<>();
        totalUtil = 0;

        ArrayList<QitemCustom> promisingQItems = new ArrayList<>();
        Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList = new Hashtable<>();

        System.out.println("Stage 1. Build Initial Q-Utility Lists");
        buildInitialQUtilityLists(inputData, inputProfit, promisingQItems, mapItemToUtilityList);
        MemoryLogger.getInstance().checkMemory();

        System.out.println("Stage 2. Find Initial High Utility Range Q-items");
        ArrayList<QitemCustom> candidateList = new ArrayList<>();
        ArrayList<QitemCustom> hwQUI = new ArrayList<>();
        findInitialRHUQIs(promisingQItems, mapItemToUtilityList, candidateList, hwQUI);
        MemoryLogger.getInstance().checkMemory();

        System.out.println("Stage 3. Recursive Mining Procedure");
        miner(qItemsetBuffer, 0, null, mapItemToUtilityList, promisingQItems, writer_hqui, hwQUI);
        MemoryLogger.getInstance().checkMemory();
        endTime = System.currentTimeMillis();

        // writeFileStatistics();
        writer_hqui.close();
    }

    /**
     * Print statistics about the algorithm execution
     *
     * @param @inputData
     */
    public void printStatistics() {
        System.out.println("============= FHUQI-MINER v 2.45 Statistical results ===============");
        System.out.println("MinUtil(%): " + percent);
        System.out.println("MinUtil threshold: " + minUtil);
        System.out.println("Coefficient: " + coefficient);
        System.out.println("HUQIcount: " + HUQIcount);
        System.out.println("Runtime: " + (double) (endTime - startTime) / 1000 + " (s)");
        System.out.println("Memory usage: " + MemoryLogger.getInstance().getMaxMemory() + " (Mb)");
        System.out.println("Join operation count: " + countConstruct);
        System.out.println("================================================");
    }

    /**
     * Build the initial q-utility lists
     *
     * @param inputData            the input file path for the database with
     *                             quantities
     * @param inputProfit          the input file path for items with profit
     *                             information
     * @param promisingQItems        the list of qitems
     * @param mapItemToUtilityList a map of each qitem to its utility list
     * @throws IOException if error while reading or writing to file
     */
    private void buildInitialQUtilityLists(String inputData, String inputProfit, ArrayList<QitemCustom> promisingQItems,
                                           Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList)
            throws IOException {

        BufferedReader br_profitTable = new BufferedReader(new FileReader(inputProfit));
        BufferedReader br_inputDatabase = new BufferedReader(new FileReader(inputData));

        // Build profitTable
        String str;
        while ((str = br_profitTable.readLine()) != null) {
            String[] itemProfit = str.split(", ");

            if (itemProfit.length >= 2) {
                int profit = Integer.parseInt(itemProfit[1]);
                if (profit == 0)
                    profit = 1;
                int item = Integer.parseInt(itemProfit[0]);
                profitTable.put(item, profit);
            }
        }
        br_profitTable.close();

        /***
         * @1st database scan
         * Build TWU for all items
         */
        twuTable = new Hashtable<>();
        int tid = 0;
        currentQitem = new QitemCustom(0, 0);
        QitemCustom Q;
        while ((str = br_inputDatabase.readLine()) != null) {
            tid++;
            String[] itemInfo = str.split(" ");
            int transactionUtil = 0;
            // Calculate all transaction utilities
            for (String s : itemInfo) {
                int itemName = Integer.parseInt(s.substring(0, s.indexOf(',')));
                int i_util = Integer.parseInt(s.substring(s.indexOf(',') + 1));
                transactionUtil += i_util * profitTable.get(itemName);
            }
            // Match the TWU to all items in transaction - add new items or increase TWU values
            for (String s : itemInfo) {
                int i_util = Integer.parseInt(s.substring(s.indexOf(',') + 1));
                currentQitem.setItem(Integer.parseInt(s.substring(0, s.indexOf(','))));
                currentQitem.setQuantityMin(i_util);
                currentQitem.setQuantityMax(i_util);
                Q = new QitemCustom();
                Q.copy(currentQitem);
                if (!twuTable.containsKey(Q))
                    // For new items, add entry to TWU table
                    twuTable.put(Q, transactionUtil);
                else
                    // For existing items, increase TWU for entry
                    twuTable.put(Q, twuTable.get(Q) + transactionUtil);
            }
            totalUtil += transactionUtil;
        }
        // Set the minimum utility of that database
        minUtil = (long) (totalUtil * percent);
        // Pruning with TWU - potential HUQI will have their UtilList created
        for (QitemCustom item : twuTable.keySet()) {
            if (twuTable.get(item) >= Math.floor(minUtil / coefficient)) {
                UtilityListCustom itemUtilList = new UtilityListCustom(item, 0);
                mapItemToUtilityList.put(item, itemUtilList);
                promisingQItems.add(item);
            }
        }
        br_inputDatabase.close();
        MemoryLogger.getInstance().checkMemory();

        /***
         * @2nd database scan
         * Build TQCS Structure (FMap) with MapItemToUtilityList
         ***/
        br_inputDatabase = new BufferedReader(new FileReader(inputData));
        tid = 0;
        // Sort the final list of Q-itemsets according to their utilities (in decreasing utility order)
        promisingQItems.sort(new Comparator<>() {
            public int compare(QitemCustom o1, QitemCustom o2) {
                return compareQItems(o1, o2);
            }
        });

        mapFMAP = new HashMap<QitemCustom, Map<QitemCustom, Integer>>();
        mapPSet = new HashMap<>();
        mapProjectedTrans = new HashMap<>();

        while ((str = br_inputDatabase.readLine()) != null) {
            tid++;
            String[] itemInfo = str.split(" ");
            int remainingUtility = 0;
            Integer newTWU = 0; // NEW OPTIMIZATION
            List<QitemCustom> revisedTransaction = new ArrayList<>();
            for (String s : itemInfo) {
                Q = new QitemCustom();
                int i_util = Integer.parseInt(s.substring(s.indexOf(',') + 1));
                Q.setItem(Integer.parseInt(s.substring(0, s.indexOf(','))));
                Q.setQuantityMin(i_util);
                Q.setQuantityMax(i_util);
                if (mapItemToUtilityList.containsKey(Q)) {
                    revisedTransaction.add(Q);
                    remainingUtility += Q.getQuantityMin() * profitTable.get(Q.getItem());
                    newTWU += Q.getQuantityMin() * profitTable.get(Q.getItem());
                }
                transactionUtility.put(tid, newTWU);
            }

            // Reorder the transaction being reviewed in (decreasing utility) order
            revisedTransaction.sort(new Comparator<QitemCustom>() {
                public int compare(QitemCustom o1, QitemCustom o2) {
                    return compareQItems(o1, o2);
                }
            });

            // After the transaction is re-ordered
            for (int i = 0; i < revisedTransaction.size(); i++) {
                QitemCustom q = revisedTransaction.get(i);
                mapPSet.computeIfAbsent(q, k -> new ArrayList<>());
                mapPSet.get(q).add(tid);
                mapProjectedTrans.computeIfAbsent(tid, k -> new HashMap<>());
                List<QitemCustom> projectedTransaction = revisedTransaction.subList(i+1,revisedTransaction.size());
                mapProjectedTrans.get(tid).put(q, projectedTransaction);
                // subtract the utility of this item from the remaining utility to get the rUtil for that item
                remainingUtility -= q.getQuantityMin() * profitTable.get(q.getItem());
                // get the utility list of this item
                UtilityListCustom utilityListOfItem = mapItemToUtilityList.get(q);
                // Add a new Element to the utility list of this item corresponding to this transaction
                QItemTransCustom elementTransaction = new QItemTransCustom(tid,
                        q.getQuantityMin() * profitTable.get(q.getItem()),
                        remainingUtility);
                utilityListOfItem.addTrans(elementTransaction);
                utilityListOfItem.addTWU(transactionUtility.get(tid));

                // BEGIN NEW OPTIMIZATION
                // If current item doesn't have TQCS yet, create new TQCS
                Map<QitemCustom, Integer> mapFMAPItem = mapFMAP.get(q);
                if (mapFMAPItem == null) {
                    mapFMAPItem = new HashMap<>();
                    mapFMAP.put(q, mapFMAPItem);
                }
                // After evaluating and creating TQCS of item
                for (int j = i + 1; j < revisedTransaction.size(); j++) {
                    QitemCustom qAfter = revisedTransaction.get(j);
                    Integer existingTWU = mapFMAPItem.get(qAfter);
                    if (existingTWU == null)
                        mapFMAPItem.put(qAfter, newTWU);
                    else
                        mapFMAPItem.put(qAfter, existingTWU + newTWU);
                }
            }
        }
        MemoryLogger.getInstance().checkMemory();
    }

    /**
     * Find the initial RHUQIs
     *
     * @param promisingQItems        a list of qitems (sorted in decreasing utility order)
     * @param mapItemToUtilityList a map from qitems to their utility lists
     * @param candidateList        a list of candidate q-items
     * @param hwQUI                another list
     * @throws IOException if error while reading or writing to file
     */
    private void findInitialRHUQIs(ArrayList<QitemCustom> promisingQItems,
                                   Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList,
                                   ArrayList<QitemCustom> candidateList,
                                   ArrayList<QitemCustom> hwQUI) throws IOException {
        // Check if a Q-itemset is:
        // 1. High,
        // 2. Candidate,
        // 3. To be explored or to be directly pruned
        StringBuilder sb = new StringBuilder();
        for (QitemCustom qItem : promisingQItems) {
            long sum_iutil = mapItemToUtilityList.get(qItem).getSumIUtils();
            long sum_rutil = mapItemToUtilityList.get(qItem).getSumRUtils();
            // High utility already -> hwQUI (set H)
            if (sum_iutil >= minUtil) {
                sb.delete(0, sb.length());
                sb.append(qItem);
                sb.append(" #UTIL: ");
                sb.append(sum_iutil);
                sb.append("\r\n");
                writer_hqui.write(sb.toString());
                hwQUI.add(qItem);
                HUQIcount++;
            } else {
                // To be extended - set E
                if (sum_iutil + sum_rutil >= minUtil) {
                    hwQUI.add(qItem);
                }
                // To be combined - set C
                if ((combiningMethod != EnumCombination.COMBINEMAX && sum_iutil >= Math.floor(minUtil / coefficient))
                        || (combiningMethod == EnumCombination.COMBINEMAX && sum_iutil >= Math.floor(minUtil / 2))) {
                    candidateList.add(qItem);
                }
            }
        }
        MemoryLogger.getInstance().checkMemory();
        // Perform the combination process on the candidate q-itemsets
        combineMethod(null, 0, candidateList, promisingQItems, mapItemToUtilityList, hwQUI);
    }



    // Combining methods
    /**
     * Combine method
     * @param prefix a prefix
     * @param prefixLength the length of the prefix
     * @param candidateList the candidate list
     * @param promisingQItems the qtiem list
     * @param mapItemToUtilityList a map of item to utility list
     * @param hwQUI hwQUI
     * @return the result
     * @throws IOException
     */
    ArrayList<QitemCustom> combineMethod(QitemCustom[] prefix, int prefixLength, ArrayList<QitemCustom> candidateList,
                                   ArrayList<QitemCustom> promisingQItems, Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList,
                                   ArrayList<QitemCustom> hwQUI) throws IOException {
        // Sort the candidate Q-itemsets according to items than Qte-Min than Qte-Max
        if (candidateList.size() >= 2) {
            candidateList.sort(new Comparator<QitemCustom>() {
                public int compare(QitemCustom o1, QitemCustom o2) {
                    return compareCandidateItems(o1, o2);
                }
            });
            if (EnumCombination.COMBINEALL.equals(combiningMethod)) {
                combineAll(prefix, prefixLength, candidateList, promisingQItems, mapItemToUtilityList, hwQUI);
            } else if (EnumCombination.COMBINEMIN.equals(combiningMethod)) {
                combineMin(prefix, prefixLength, candidateList, promisingQItems, mapItemToUtilityList, hwQUI);
            } else if (EnumCombination.COMBINEMAX.equals(combiningMethod)) {
                combineMax(prefix, prefixLength, candidateList, promisingQItems, mapItemToUtilityList, hwQUI);
            }
            MemoryLogger.getInstance().checkMemory();
        }
        return promisingQItems;
    }

    /**
     * Combine All
     *
     * @param prefix               a prefix of an itemset
     * @param prefixLength         the length of the prefix
     * @param candidateList        a list of candidate qitems
     * @param promisingQItems        another list of qitems
     * @param mapItemToUtilityList a map of qitems to utility list
     * @param hwQUI                another list of qitems
     * @throws IOException if error while reading or writing to file
     */
    private void combineAll(QitemCustom[] prefix, int prefixLength, ArrayList<QitemCustom> candidateList,
                            ArrayList<QitemCustom> promisingQItems, Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList,
                            ArrayList<QitemCustom> hwQUI) throws IOException {
        // delete non necessary candidate q-items
        deleteUnnecessaryCandidate(candidateList);

        // make the combination process
        Map<QitemCustom, UtilityListCustom> mapRangeToUtilityList = new HashMap<>();

        int count;
        for (int i = 0; i < candidateList.size(); i++) {
            int currentItem = candidateList.get(i).getItem();

            mapRangeToUtilityList.clear();
            count = 1;
            for (int j = i + 1; j < candidateList.size(); j++) {
                int nextItem = candidateList.get(j).getItem();
                if (currentItem != nextItem)
                    break;
                else {
                    UtilityListCustom res;

                    if (j == i + 1) {

                        if (candidateList.get(j).getQuantityMin()!= candidateList.get(i).getQuantityMax()+ 1)
                            break;

                        res = constructForCombine(mapItemToUtilityList.get(candidateList.get(i)),
                                mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count > coefficient)
                            break;

                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                        if (res.getSumIUtils() > minUtil) {
                            HUQIcount++;
                            writeOut2(prefix, prefixLength, res.getSingleItemsetName(), res.getSumIUtils());
                            hwQUI.add(res.getSingleItemsetName());
                            mapItemToUtilityList.put(res.getSingleItemsetName(), res);
                            int site = promisingQItems.indexOf(candidateList.get(j));
                            promisingQItems.add(site, res.getSingleItemsetName());
                        }
                    } else {
                        if (candidateList.get(j).getQuantityMin() != candidateList.get(j - 1).getQuantityMax() + 1)
                            break;
                        QitemCustom qItem1 = new QitemCustom(currentItem, candidateList.get(i).getQuantityMin(),
                                candidateList.get(j - 1).getQuantityMax());
                        UtilityListCustom ulQitem1 = mapRangeToUtilityList.get(qItem1);
                        res = constructForCombine(ulQitem1, mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count > coefficient)
                            break;
                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                        if (res.getSumIUtils() > minUtil) {
                            HUQIcount++;
                            writeOut2(prefix, prefixLength, res.getSingleItemsetName(), res.getSumIUtils());
                            hwQUI.add(res.getSingleItemsetName());
                            mapItemToUtilityList.put(res.getSingleItemsetName(), res);
                            int site = promisingQItems.indexOf(candidateList.get(j));
                            promisingQItems.add(site, res.getSingleItemsetName());
                        }
                    }
                }
            }
        }
        MemoryLogger.getInstance().checkMemory();
    }

    /**
     * Combine Min
     *
     * @param prefix               a prefix of an itemset
     * @param prefixLength         the length of the prefix
     * @param candidateList        a list of candidate qitems
     * @param promisingQItems        another list of qitems
     * @param mapItemToUtilityList a map of qitems to utility list
     * @param hwQUI                another list of qitems
     * @throws IOException if error while reading or writing to file
     */
    private void combineMin(QitemCustom[] prefix, int prefixLength, ArrayList<QitemCustom> candidateList,
                            ArrayList<QitemCustom> promisingQItems, Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList,
                            ArrayList<QitemCustom> hwQUI) throws IOException {

        // delete non necessary candidate q-items
        deleteUnnecessaryCandidate(candidateList);

        // make the combination process
        int count;
        ArrayList<QitemCustom> temporaryArrayList = new ArrayList<>();
        Map<QitemCustom, UtilityListCustom> temporaryMap = new HashMap<>();
        Map<QitemCustom, UtilityListCustom> mapRangeToUtilityList = new HashMap<>();

        for (int i = 0; i < candidateList.size(); i++) {
            int currentItem = candidateList.get(i).getItem();
            mapRangeToUtilityList.clear();
            count = 1;
            for (int j = i + 1; j < candidateList.size(); j++) {
                int nextItem = candidateList.get(j).getItem();
                if (currentItem != nextItem)
                    break;

                else {
                    UtilityListCustom res;
                    if (j == i + 1) {
                        if (candidateList.get(j).getQuantityMin() != candidateList.get(i).getQuantityMax() + 1)
                            break;
                        res = constructForCombine(mapItemToUtilityList.get(candidateList.get(i)),
                                mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count > coefficient)
                            break;
                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                        if (res.getSumIUtils() > minUtil) {
                            if ((temporaryArrayList.isEmpty())
                                    || (res.getSingleItemsetName().getItem() != temporaryArrayList
                                    .get(temporaryArrayList.size() - 1).getItem())
                                    || (res.getSingleItemsetName().getQuantityMax() > temporaryArrayList
                                    .get(temporaryArrayList.size() - 1).getQuantityMax())) {
                                temporaryArrayList.add(res.getSingleItemsetName());
                                temporaryMap.put(res.getSingleItemsetName(), res);
                            } else {
                                temporaryMap.remove(temporaryArrayList.get(temporaryArrayList.size() - 1));
                                temporaryArrayList.remove(temporaryArrayList.size() - 1);
                                temporaryArrayList.add(res.getSingleItemsetName());
                                temporaryMap.put(res.getSingleItemsetName(), res);
                            }
                            ;
                            break;
                        }
                    } else {
                        if (candidateList.get(j).getQuantityMin() != candidateList.get(j - 1).getQuantityMax() + 1)
                            break;
                        QitemCustom qItem1 = new QitemCustom(currentItem, candidateList.get(i).getQuantityMin(),
                                candidateList.get(j - 1).getQuantityMax());
                        UtilityListCustom ulQitem1 = mapRangeToUtilityList.get(qItem1);
                        res = constructForCombine(ulQitem1, mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count > coefficient)
                            break;
                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                        if (res.getSumIUtils() > minUtil) {
                            if ((temporaryArrayList.isEmpty())
                                    || (res.getSingleItemsetName().getItem() != temporaryArrayList
                                    .get(temporaryArrayList.size() - 1).getItem())
                                    || (res.getSingleItemsetName().getQuantityMax() > temporaryArrayList
                                    .get(temporaryArrayList.size() - 1).getQuantityMax())) {
                                temporaryArrayList.add(res.getSingleItemsetName());
                                temporaryMap.put(res.getSingleItemsetName(), res);
                            } else {
                                temporaryMap.remove(temporaryArrayList.get(temporaryArrayList.size() - 1));
                                temporaryArrayList.remove(temporaryArrayList.size() - 1);
                                temporaryArrayList.add(res.getSingleItemsetName());
                                temporaryMap.put(res.getSingleItemsetName(), res);

                            }

                            break;
                        }
                    }
                }
            }
        }
        for (QitemCustom currentQitem : temporaryArrayList) {
            mapItemToUtilityList.put(currentQitem, temporaryMap.get(currentQitem));
            writeOut2(prefix, prefixLength, currentQitem, temporaryMap.get(currentQitem).getSumIUtils());
            HUQIcount++;
            hwQUI.add(currentQitem);
            QitemCustom q = new QitemCustom(currentQitem.getItem(), currentQitem.getQuantityMax());
            int site = promisingQItems.indexOf(q);
            promisingQItems.add(site, currentQitem);
        }
        temporaryArrayList.clear();
        temporaryMap.clear();
        MemoryLogger.getInstance().checkMemory();
    }
    /**
     * Combine Max
     *
     * @param prefix               a prefix of an itemset
     * @param prefixLength         the length of the prefix
     * @param candidateList        a list of candidate qitems
     * @param promisingQItems        another list of qitems
     * @param mapItemToUtilityList a map of qitems to utility list
     * @param hwQUI                another list of qitems
     * @throws IOException if error while reading or writing to file
     */
    private void combineMax(QitemCustom[] prefix, int prefixLength, ArrayList<QitemCustom> candidateList,
                            ArrayList<QitemCustom> promisingQItems, Hashtable<QitemCustom, UtilityListCustom> mapItemToUtilityList,
                            ArrayList<QitemCustom> hwQUI) throws IOException {
        // delete non necessary candidate q-items
        deleteUnnecessaryCandidate(candidateList);
        // make the combination process
        ArrayList<QitemCustom> temporaryArrayList = new ArrayList<>();
        Map<QitemCustom, UtilityListCustom> temporaryMap = new HashMap<>();
        Map<QitemCustom, UtilityListCustom> mapRangeToUtilityList = new HashMap<>();
        int count;
        for (int i = 0; i < candidateList.size(); i++) {
            UtilityListCustom res = new UtilityListCustom();
            int currentItem = candidateList.get(i).getItem();
            mapRangeToUtilityList.clear();
            count = 1;
            for (int j = i + 1; j < candidateList.size(); j++) {
                int nextItem = candidateList.get(j).getItem();
                if (currentItem != nextItem)
                    break;
                else {
                    if (j == i + 1) {
                        if (candidateList.get(j).getQuantityMin() != candidateList.get(i).getQuantityMax() + 1)
                            break;
                        res = constructForCombine(mapItemToUtilityList.get(candidateList.get(i)),
                                mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count > coefficient - 1)
                            break;
                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                    } else {
                        if (candidateList.get(j).getQuantityMin() != candidateList.get(j - 1).getQuantityMax() + 1)
                            break;
                        QitemCustom qItem1 = new QitemCustom(currentItem, candidateList.get(i).getQuantityMin(),
                                candidateList.get(j - 1).getQuantityMax());
                        UtilityListCustom ulQitem1 = mapRangeToUtilityList.get(qItem1);
                        res = constructForCombine(ulQitem1, mapItemToUtilityList.get(candidateList.get(j)));
                        count++;
                        if (count >= coefficient)
                            break;
                        mapRangeToUtilityList.put(res.getSingleItemsetName(), res);
                    }

                }
            }
            if (res.getSumIUtils() > minUtil) {
                if ((temporaryMap.isEmpty())
                        || (res.getSingleItemsetName().getItem() != temporaryArrayList
                        .get(temporaryArrayList.size() - 1).getItem())
                        || (res.getSingleItemsetName().getQuantityMax() > temporaryArrayList
                        .get(temporaryArrayList.size() - 1).getQuantityMax())) {
                    temporaryMap.put(res.getSingleItemsetName(), res);
                    temporaryArrayList.add(res.getSingleItemsetName());
                }
            }
        }
        for (int k = 0; k < temporaryArrayList.size(); k++) {
            QitemCustom currentQitem = temporaryArrayList.get(k);
            mapItemToUtilityList.put(currentQitem, temporaryMap.get(currentQitem));
            writeOut2(prefix, prefixLength, currentQitem, temporaryMap.get(currentQitem).getSumIUtils());
            HUQIcount++;
            hwQUI.add(currentQitem);
            QitemCustom q = new QitemCustom(currentQitem.getItem(), currentQitem.getQuantityMax());
            int site = promisingQItems.indexOf(q);
            promisingQItems.add(site, currentQitem);
        }

        temporaryArrayList.clear();
        temporaryMap.clear();
        MemoryLogger.getInstance().checkMemory();
    }



    // Utility Itemset methods
    /**
     * Method to construct the utility list of an itemset
     *
     * @param ulQitem1 the utility list of a qitem
     * @param ulQitem2 the utility list of another qitem
     * @return the resulting utility list
     */
    private UtilityListCustom constructForCombine(UtilityListCustom ulQitem1,
                                                  UtilityListCustom ulQitem2) {

        UtilityListCustom resultUtilityList = new UtilityListCustom(new QitemCustom(ulQitem1.getSingleItemsetName().getItem(),
                ulQitem1.getSingleItemsetName().getQuantityMin(), ulQitem2.getSingleItemsetName().getQuantityMax()));

        ArrayList<QItemTransCustom> temp1 = ulQitem1.getQItemTransCustom();
        ArrayList<QItemTransCustom> temp2 = ulQitem2.getQItemTransCustom();
        ArrayList<QItemTransCustom> mainlist = new ArrayList<>();

        resultUtilityList.setSumIUtils(ulQitem1.getSumIUtils() + ulQitem2.getSumIUtils());
        resultUtilityList.setSumRUtils(ulQitem1.getSumRUtils() + ulQitem2.getSumRUtils());
        resultUtilityList.setTwu(ulQitem1.getTwu() + ulQitem2.getTwu());

        int i = 0, j = 0;
        while (i < temp1.size() && j < temp2.size()) {
            int t1 = temp1.get(i).getTid();
            int t2 = temp2.get(j).getTid();
            if (t1 > t2) {
                mainlist.add(temp2.get(j));
                j++;
            } else {
                mainlist.add(temp1.get(i));
                i++;
            }
        }
        if (i == temp1.size()) {
            while (j < temp2.size())
                mainlist.add(temp2.get(j++));
        } else if (j == temp2.size()) {
            while (i < temp1.size()) {
                mainlist.add(temp1.get(i++));
            }
        }
        resultUtilityList.setQItemTransCustom(mainlist);
        MemoryLogger.getInstance().checkMemory();
        return resultUtilityList;
    }

    /**
     * Method to join two utility lists
     *
     * @param ul1 the utility list of an item
     * @param ul2 the utility list of another item
     * @param ul0 the utility list of the prefix
     * @return the resulting utility list
     */
    private UtilityListCustom constructForJoin(UtilityListCustom ul1,
                                               UtilityListCustom ul2,
                                               UtilityListCustom ul0) {
        if (ul1.getSingleItemsetName().getItem() == ul2.getSingleItemsetName().getItem())
            return null;

        ArrayList<QItemTransCustom> qT1 = ul1.getQItemTransCustom();
        ArrayList<QItemTransCustom> qT2 = ul2.getQItemTransCustom();
        UtilityListCustom res = new UtilityListCustom(ul2.getItemsetName());

        if (ul0 == null) {
            int i = 0, j = 0;
            while (i < qT1.size() && j < qT2.size()) {
                int tid1 = qT1.get(i).getTid();
                int tid2 = qT2.get(j).getTid();

                if (tid1 == tid2) {
                    // QItemTrans combine = new QItemTrans();
                    int eu1 = qT1.get(i).getEu();
                    // int ru = qT1.get(i).getRu();
                    int eu2 = qT2.get(j).getEu();

                    if (qT1.get(i).getRu() >= qT2.get(j).getRu()) {
                        QItemTransCustom temp = new QItemTransCustom(tid1, eu1 + eu2, qT2.get(j).getRu());
                        res.addTrans(temp, transactionUtility.get(tid1));
                    }
                    i++;
                    j++;
                } else if (tid1 > tid2) {
                    j++;
                } else {
                    i++;
                }
            }
        } else {
            ArrayList<QItemTransCustom> preQT = ul0.getQItemTransCustom();
            int i = 0, j = 0, k = 0;
            while (i < qT1.size() && j < qT2.size()) {
                int tid1 = qT1.get(i).getTid();
                int tid2 = qT2.get(j).getTid();

                if (tid1 == tid2) {
                    // QItemTrans combine = new QItemTrans();
                    int eu1 = qT1.get(i).getEu();
                    int ru1 = qT1.get(i).getRu();
                    int eu2 = qT2.get(j).getEu();

                    // 褌銉︻爣顎� preitem顎檜tility顓�
                    while (preQT.get(k).getTid() != tid1) {
                        k++;
                    }
                    int preEU = preQT.get(k).getEu();

                    if (qT1.get(i).getRu() >= qT2.get(j).getRu()) {
                        QItemTransCustom temp = new QItemTransCustom(tid1, eu1 + eu2 - preEU, qT2.get(j).getRu());
                        res.addTrans(temp, transactionUtility.get(tid1));
                    }
                    i++;
                    j++;
                } else if (tid1 > tid2) {
                    j++;
                } else {
                    i++;
                }
            }
            // return res;
        }
        MemoryLogger.getInstance().checkMemory();
        if (!res.getQItemTransCustom().isEmpty())
            return res;
        return null;
    }



    // Main algorithm
    /**
     * The main pattern mining procedure
     *
     * @param prefix         a prefix itemset
     * @param prefixLength   the length of the prefix
     * @param prefixUL       the utility list of the prefix
     * @param ULs            the utility lists of some extensions of the prefix
     * @param promisingQItems  a list of qitems
     * @param br_writer_hqui the buffered writer for writing the output
     * @param hwQUI          list of hWQUIs
     * @throws IOException if error reading or writing to file
     */
    private void miner(QitemCustom[] prefix, int prefixLength, UtilityListCustom prefixUL,
                       Hashtable<QitemCustom, UtilityListCustom> ULs,
                       ArrayList<QitemCustom> promisingQItems, BufferedWriter br_writer_hqui,
                       ArrayList<QitemCustom> hwQUI) throws IOException {
        // For pruning range Q-itemsets using TQCS (MapFMap)
        int[] t2 = new int[coefficient];
        ArrayList<QitemCustom> nextNameList = new ArrayList<>();

        for (int i = 0; i < promisingQItems.size(); i++) {
            QitemCustom currentQitem = promisingQItems.get(i);
            nextNameList.clear();
            ArrayList<QitemCustom> nextHWQUI = new ArrayList<>();
            ArrayList<QitemCustom> candidateList = new ArrayList<>();
            Hashtable<QitemCustom, UtilityListCustom> nextHUL = new Hashtable<>();
            Hashtable<QitemCustom, UtilityListCustom> candidateHUL = new Hashtable<>();
            if (!hwQUI.contains(currentQitem)) {
                continue;
            }
            // t2 is the map of positions for each exact Q-item within the range Q-item being considered
            if (currentQitem.isRange()) {
                for (int ii = promisingQItems.get(i).getQuantityMin(); ii <= promisingQItems.get(i).getQuantityMax(); ii++) {
                    t2[ii - promisingQItems.get(i).getQuantityMin()] = promisingQItems.indexOf(new QitemCustom(promisingQItems.get(i).getItem(), ii));
                }
            }

            for (int j = i + 1; j < promisingQItems.size(); j++) {
                QitemCustom nextQitem = promisingQItems.get(j);
                List<Integer> pSet = mapPSet.get(nextQitem);
                if (pSet==null)
                    continue;
                if (currentQitem.isRange() && j==i+1)
                    continue;

                // Improved co-occurrence pruning strategy
                UtilityListCustom afterUL = null;
                int sumTWU = 0;
                for (int ii = currentQitem.getQuantityMin(); ii <= currentQitem.getQuantityMax(); ii++) {
                    QitemCustom exactQitem = new QitemCustom(currentQitem.getItem(), ii);
                    for (int tid : pSet) {
                        List<QitemCustom> projection = mapProjectedTrans.get(tid).get(exactQitem);
                        if (projection != null) {
                            for (QitemCustom qitem : projection) {
                                Integer sum = mapFMAP.get(exactQitem).get(qitem);
                                if (sum == null)
                                    continue;
                                sumTWU += sum;
                            }
                        }
                    }
                }

                if (sumTWU < Math.floor(minUtil / coefficient))
                    continue;
                else {
                    afterUL = constructForJoin(ULs.get(currentQitem), ULs.get(nextQitem),
                            prefixUL);
                    countConstruct++;
                    if (afterUL == null || afterUL.getTwu() < Math.floor(minUtil / coefficient))
                        continue;
                }

                if (afterUL.getTwu() >= Math.floor(minUtil / coefficient)) {
                    // Items to be explored
                    nextNameList.add(afterUL.getSingleItemsetName());
                    nextHUL.put(afterUL.getSingleItemsetName(), afterUL);
                    countUL++;
                    if (afterUL.getSumIUtils() >= minUtil) {
                        writeOut1(prefix, prefixLength, currentQitem, nextQitem, afterUL.getSumIUtils());
                        HUQIcount++;
                        nextHWQUI.add(afterUL.getSingleItemsetName());
                    } else {
                        if (afterUL.getSumIUtils() + afterUL.getSumRUtils() >= minUtil) {
                            nextHWQUI.add(afterUL.getSingleItemsetName());
                        }
                        if ((combiningMethod != EnumCombination.COMBINEMAX
                                && afterUL.getSumIUtils() >= Math.floor(minUtil / coefficient))
                                || (combiningMethod == EnumCombination.COMBINEMAX
                                && afterUL.getSumIUtils() >= Math.floor(minUtil / 2))) {
                            candidateList.add(afterUL.getSingleItemsetName());
                            candidateHUL.put(afterUL.getSingleItemsetName(), afterUL);
                        }
                    }
                }
            }

            if (candidateList.size() > 0) { // combine process
                nextNameList = combineMethod(prefix, prefixLength, candidateList, nextNameList, nextHUL, nextHWQUI);
                candidateHUL.clear();
                candidateList.clear();
            }
            MemoryLogger.getInstance().checkMemory();
            if (nextNameList.size() >= 1) { // recursive call
                qItemsetBuffer[prefixLength] = currentQitem;
                miner(qItemsetBuffer, prefixLength + 1, ULs.get(currentQitem), nextHUL, nextNameList,
                        br_writer_hqui, nextHWQUI);
            }

        }
    }



    // Post-processing methods
    /**
     * Write an itemset to file
     *
     * @param prefix       the prefix
     * @param prefixLength the length of the prefix
     * @param x            an item x that is appended to the prefix
     * @param y            an item y that is appended to the prefix
     * @param utility      the utility of the itemset
     * @throws IOException if error while writing to file
     */
    private void writeOut1(QitemCustom[] prefix, int prefixLength,
                           QitemCustom x, QitemCustom y, long utility) throws IOException {

        // Create a string buffer
        StringBuilder buffer = new StringBuilder();
        // append the prefix
        for (int i = 0; i < prefixLength; i++) {
            buffer.append(prefix[i].toString());
            buffer.append(' ');
        }
        // append the last item
        buffer.append(x.toString() + " " + y.toString() + " #UTIL: ");

        // append the utility value
        buffer.append(utility);

        // write to file
        writer_hqui.write(buffer.toString());
        writer_hqui.newLine();

    }

    /**
     * Write an itemset to file
     *
     * @param prefix       the prefix
     * @param prefixLength the length of the prefix
     * @param x            an item x that is appended to the prefix
     * @param utility      the utility of the itemset
     * @throws IOException if error while writing to file
     */
    private void writeOut2(QitemCustom[] prefix, int prefixLength, QitemCustom x, long utility) throws IOException {

        // Create a string buffer
        StringBuilder buffer = new StringBuilder();
        // append the prefix
        for (int i = 0; i < prefixLength; i++) {
            buffer.append(prefix[i].toString());
            buffer.append(' ');
        }
        // append the last item
        buffer.append(x.toString() + " #UTIL: ");

        // append the utility value

        buffer.append(utility);
        // write to file
        writer_hqui.write(buffer.toString());
        writer_hqui.newLine();

    }

    /**
     * Write statistics about the algorithm execution to the file
     *
     * @throws IOException if error while writing to file
     */
    private void writeFileStatistics() throws IOException {

        StringBuilder buffer = new StringBuilder();

        buffer.append("#HUQIcount:");
        buffer.append(HUQIcount);
        buffer.append(System.lineSeparator());

        buffer.append("#runTime:");
        buffer.append((double) (endTime - startTime) / 1000);
        buffer.append(System.lineSeparator());

        buffer.append("#memory use:");
        buffer.append(MemoryLogger.getInstance().getMaxMemory());
        buffer.append(System.lineSeparator());

        buffer.append("#countUL:");
        buffer.append(countUL);
        buffer.append(System.lineSeparator());

        buffer.append("#countJoin:");
        buffer.append(countConstruct);
        buffer.append(System.lineSeparator());

        // write to file
        writer_hqui.write(buffer.toString());
        writer_hqui.newLine();

    }



    // Comparing methods
    /**
     * Comparator to order qItems
     *
     * @param q1 a qitem
     * @param q2 another qitem
     * @return the comparison result
     */
    private int compareQItems(QitemCustom q1, QitemCustom q2) {
        int compare = (int) ((q2.getQuantityMin() * profitTable.get(q2.getItem()))
                - (q1.getQuantityMin() * profitTable.get(q1.getItem())));
        // if the same, use the lexical order otherwise use the TWU
        return (compare == 0) ? q1.getItem() - q2.getItem() : compare;
    }

    /**
     * Comparator to order candidate qItems
     *
     * @param q1 a qitem
     * @param q2 another qitem
     * @return the comparison result
     */
    private int compareCandidateItems(QitemCustom q1, QitemCustom q2) {
        int compare = q1.getItem() - q2.getItem();
        if (compare == 0)
            compare = q1.getQuantityMin() - q2.getQuantityMin();
        if (compare == 0)
            compare = q1.getQuantityMax() - q2.getQuantityMax();
        return compare;
    }

    private void deleteUnnecessaryCandidate(ArrayList<QitemCustom> inputCandidateList) {
        int s = 1;
        while (s < inputCandidateList.size() - 1) {
            /* If the quantityMax of item before S is smaller than quantityMin of S by 1 unit
            or If the quantityMin of the item after S is larger than quantityMax of S by 1 unit
            */
            if (((inputCandidateList.get(s).getQuantityMin() == inputCandidateList.get(s - 1).getQuantityMax() + 1)
                    && (inputCandidateList.get(s).getItem() == inputCandidateList.get(s - 1).getItem()))
                    || ((inputCandidateList.get(s).getQuantityMax() == inputCandidateList.get(s + 1).getQuantityMin() - 1)
                    && (inputCandidateList.get(s).getItem() == inputCandidateList.get(s + 1).getItem())))
                s++;
            else
                inputCandidateList.remove(s);
        }
        if (inputCandidateList.size() > 2) {
            if ((inputCandidateList.get(inputCandidateList.size() - 1)
                    .getQuantityMin() != inputCandidateList.get(inputCandidateList.size() - 2).getQuantityMax() + 1)
                    || (inputCandidateList.get(inputCandidateList.size() - 2).getItem() != inputCandidateList
                    .get(inputCandidateList.size() - 1).getItem()))
                inputCandidateList.remove(inputCandidateList.size() - 1);
        }
    }
}

