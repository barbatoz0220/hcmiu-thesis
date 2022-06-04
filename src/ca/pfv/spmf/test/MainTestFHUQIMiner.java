/* This file is copyright (c) 2020 Mourad Nouioua et al.
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
* 
*/
package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer.AlgoFHUQIMiner;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer.EnumCombination;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom.AlgoFHUQIMinerCustom;

/**
 * Class that shows how to run the FHUQI-Miner algorithm from the source code.
 * @author Mourad Nouioua et al. 2020
 */
public class MainTestFHUQIMiner {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {

//		 Paper dataset (from the example in paper)
//		String inputFileProfitPath = fileToPath("dbHUQI_paper_p.txt");
//		String inputFileDBPath = fileToPath("dbHUQI_paper.txt");
//		float minUtilThreshold = 0.25f;
//		int qrc = 5;
//		String output = "results\\paper\\output_paper.txt";
//		String output = "results\\paper\\output_paper(max).txt";
//		String output = "results\\paper\\output_paper(min).txt";
//		String output = "results\\paper\\output_paper_custom.txt";
//		String output = "results\\paper\\output_paper_custom(min).txt";
//		String output = "results\\paper\\output_paper_custom(max).txt";
//		String output = "results\\paper\\output_paper_custom_test3.txt";
//		String output = "results\\paper\\output_paper_custom_test4.txt";
//		String output = "results\\paper\\output_paper_custom_test(max).txt";


		// FOODMART DATASET
//		String inputFileProfitPath = fileToPath("datasets\\foodmartf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\foodmart.txt");
//		float minUtilThreshold = 0.001f;
//		int qrc = 10;
//		String output = "results\\foodmart\\output_foodmart.txt";
//		String output = "results\\foodmart\\output_foodmart(max).txt";
//		String output = "results\\foodmart\\output_foodmart(min).txt";
//		String output = "results\\foodmart\\output_foodmart_custom_test.txt";
//		String output = "results\\foodmart\\output_foodmart_custom_test3.txt";
//		String output = "results\\foodmart\\output_foodmart_custom(max).txt";
//		String output = "results\\foodmart\\output_foodmart_custom_test3(max).txt";
//		String output = "results\\foodmart\\output_foodmart_custom(min).txt";
//		String output = "results\\foodmart\\output_foodmart_custom_test3(min).txt";


		// BMS2 DATASET
		String inputFileProfitPath = fileToPath("datasets\\bmsf2_p.txt");
		String inputFileDBPath = fileToPath("datasets\\bms2.txt");
		float minUtilThreshold = 0.001f;
		int qrc = 10;
//		String output = "results\\bms2\\output_bms2.txt";
//		String output = "results\\bms2\\output_bms2(max).txt";
//		String output = "results\\bms2\\output_bms2(min).txt";
//		String output = "results\\bms2\\output_bms2_custom_test2.txt";
//		String output = "results\\bms2\\output_bms2_custom_test3.txt";
//		String output = "results\\bms2\\output_bms2_custom(max)_test2.txt";
		String output = "results\\bms2\\output_bms2_custom(max)_test3.txt";
//		String output = "results\\bms2\\output_bms2_custom(min)_test2.txt";


//		// RETAIL DATASET
//		String inputFileProfitPath = fileToPath("datasets\\retailf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\retail.txt");
//		float minUtilThreshold = 0.01f;
//		int qrc = 5;
//		String output = "results\retail\output_retail.txt";
//		String output = "results\retail\output_retail(max).txt";
//		String output = "results\retail\output_retail(min).txt";
//		String output = "results\\retail\\output_retail_custom.txt";
//		String output = "results\retail\output_retail_custom(max).txt";
//		String output = "results\retail\output_retail_custom(min).txt";


		// PUMSB DATASET
//		String inputFileProfitPath = fileToPath("datasets\\pumsbf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\pumsb.txt");
//		float minUtilThreshold = 0.01f;
//		int qrc = 5;
//		String output = "results\\pumsb\\output_pumsb.txt";
//		String output = "results\\pumsb\\output_pumsb(max).txt";
//		String output = "results\\pumsb\\output_pumsb(min).txt";
//		String output = "results\\pumsb\\output_pumsb_custom.txt";
//		String output = "results\\pumsb\\output_pumsb_custom_test2.txt";
//		String output = "results\\pumsb\\output_pumsb_custom(max).txt";
//		String output = "results\\pumsb\\output_pumsb_custom(min).txt";
//		String output = "results\\pumsb\\output_pumsb_custom_test2(max).txt";


		// Run the algorithm
		// The combination method  (there are three possibilities )
		EnumCombination combinationmethod = EnumCombination.COMBINEMAX;
//		EnumCombination combinationmethod = EnumCombination.COMBINEMIN;
//		EnumCombination combinationmethod = EnumCombination.COMBINEALL;

//		AlgoFHUQIMiner algo = new AlgoFHUQIMiner();
		AlgoFHUQIMinerCustom algo = new AlgoFHUQIMinerCustom();
		algo.runAlgorithm(inputFileDBPath, inputFileProfitPath, minUtilThreshold, qrc, combinationmethod, output);
		algo.printStatistics();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestFHUQIMiner.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

}
