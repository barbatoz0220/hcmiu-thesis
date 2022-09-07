# Efficient Pruning Strategy for Mining High-Utility Quantitative Itemset
By Pham Hoang Nam Anh, ITITIU18007

Under the supervison of Associate Professor Loan Nguyen T. T.

And the review of Doctor Tan L. D.
## Installation
- Download the repository (with SPMF library included)
- Import the project onto an IDE of choice
- The code for this project should be available in the `src/ca/pfv/spmf/algorithms/frequentpatterns/fhuqiminer_custom/` folder
- The main function and other settings can be changed and executed via `src/ca/pfv/spmf/test/MainTestFHUQIMiner.java`

## Introduction
This is the base repository for my Thesis report at International University, HCMC, Vietnam for the Spring Semester of 2022

As an extension from the traditional methods of Pattern Mining, High-Utility Quantitative Itemset Mining (HUIM) has become an important research area that
answers the ever-growing need for useful information from the copious pool of data in reality. Since the nature of this research category is to
unveil all possible important patterns from a database and manage both quantities (internal utility) with prices (external utility) of their items,
algorithms under HUQIM usually work with very large search spaces that could heavily affect execution time.

In 2021, Nourad et al. have recently proposed an efficient algorithm called Fast High-Utility Quantitative Itemset Miner (FHUQI-Miner)
with two adapted strategies from previous research works to narrow down space for item searching and subsequently enhance the overall performance
(source: https://link.springer.com/article/10.1007/s10489-021-02204-w).
While these strategies have been demonstrated to enhance the new algorithm's performance compared to its predecessors, there were certain shortcomings
that the algorithm still faced.

- The **first limitation was how the proposed strategies would not operate as efficiently on dense datasets** as it would on sparse datasets,
deriving from the similarity in structure of the transactions, and thus increasing the number of join operations in the progress. 
- The **second limitation was that the two newly proposed strategies of FHUQI-Miner was based of a pruning strategy that had been developed
for quite some time ago**, and up until now, there exists a number of later introduced strategies that were proven to be more efficient at pruning undesirable items.

## Proposals
Given the context above, the aim of this thesis would be two-fold:
- Firstly, this Thesis will aim to **refine the proposed pruning strategies** of the existing FHUQI-Miner algorithm
- And secondly, it will also **attempt at a more efficient pruning strategy** from the better portion of existing strategies that could adjust to the requirements of HUQIM.

The outcomes of these studies would consist of two modified versions of the original FHUQI-Miner algorithm with the aim of enhancing the overall performance
by firstly uniting the two pruning strategies into one based on the Q-item notation theory, and secondly introducing a novel adaptation of the concepts regarding
projected database and transactions to HUQIM, which were established under the former research field of HUIM.
These resulted alternatives have been verified to be capable of reducing the number of join operations compared to the base algorithm on both sparse and
dense datasets as they effectively remove more unpromising items during the mining processes. 

## Experiments
The datasets used in this Thesis can be found entirely on the Homepage of the SPMF Library
(source: https://www.philippe-fournier-viger.com/spmf/index.php?link=datasets.php) with the characteristics as follows:

![datasets](https://user-images.githubusercontent.com/47182649/175821575-de1ff1cd-b0bc-4e25-b0d6-5be9175e98b7.png)

### On Foodmart dataset
<img src="https://user-images.githubusercontent.com/47182649/175821689-a9441b2a-d27b-4425-91cf-ef53b9428195.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822099-aef5ad1f-68f2-4ba8-8735-b5dfb9f89653.png">

### On BMS1 dataset
<img src="https://user-images.githubusercontent.com/47182649/175821694-1b360d72-f950-41b8-a18e-dc2996b64906.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822198-0971a064-ec89-4517-a364-c0350dc2d218.png">

### On BMS2 dataset
<img src="https://user-images.githubusercontent.com/47182649/175821698-f8dd76df-3267-46fb-904d-c6d5774760cc.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822213-0253fe02-2736-4706-a379-415e105174df.png">

### On Retail dataset

<img src="https://user-images.githubusercontent.com/47182649/175821706-56ac1371-c7d4-444a-acaa-a6f790661717.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822228-58373933-1351-45a5-a77a-7724a9d364ce.png">

### On PUMSB dataset
<img src="https://user-images.githubusercontent.com/47182649/175821827-acfcc650-8732-4f05-96f6-5cb99315593b.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822276-cbdd9536-dbc0-4c3a-b56a-4650cd94aad1.png">

### On Connect dataset
<img src="https://user-images.githubusercontent.com/47182649/175822447-07156d5d-68d2-4dea-96a2-4948b3f519e1.png" width="500">
<img src="https://user-images.githubusercontent.com/47182649/175822501-6e23e71d-c7d0-4247-bf04-2906a24facee.png">


