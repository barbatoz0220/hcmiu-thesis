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
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom.AlgoFHUQIMinerCombined;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom.AlgoFHUQIMinerImproved;
import ca.pfv.spmf.algorithms.frequentpatterns.fhuqiminer_custom.AlgoFHUQIMinerPSet;

/**
 * Class that shows how to run the FHUQI-Miner algorithm from the source code.
 * @author Mourad Nouioua et al. 2020
 */
public class MainTestFHUQIMiner {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {

		// EXAMPLE IN PAPER
		String inputFileProfitPath = fileToPath("dbHUQI_paper_p.txt");
		String inputFileDBPath = fileToPath("dbHUQI_paper.txt");
		float minUtilThreshold = 0.25f;
		int qrc = 5;
		String output = "results\\paper\\output.txt";


//		 FOODMART DATASET
//		String inputFileProfitPath = fileToPath("datasets\\foodmartf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\foodmart.txt");
//		float minUtilThreshold = 0.0002f;
//		int qrc = 10;
//		String output = "results\\foodmart\\output.txt";


		// BMS1 DATASET
//		String inputFileProfitPath = fileToPath("datasets\\bmsf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\bms1.txt");
//		float minUtilThreshold = 0.006f;
//		int qrc = 10;
//		String output = "results\\bms1\\output_bms1.txt";


		// BMS2 DATASET
//		String inputFileProfitPath = fileToPath("datasets\\bmsf2_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\bms2.txt");
//		float minUtilThreshold = 0.005f;
//		int qrc = 10;
//		String output = "results\\bms2\\output_bms2.txt";


		// RETAIL DATASET
//		String inputFileProfitPath = fileToPath("datasets\\retailf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\retail.txt");
//		float minUtilThreshold = 0.009f;
//		int qrc = 10;
//		String output = "results\\retail\\output_retail0.txt";


		// CONNECT DATASET
//		String inputFileProfitPath = fileToPath("datasets\\connectf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\connect.txt");
//		float minUtilThreshold = 3f;
//		int qrc = 5;
//		String output = "results\\connect\\output_connect.txt";


		// PUMSB DATASET
//		String inputFileProfitPath = fileToPath("datasets\\pumsbf1_p.txt");
//		String inputFileDBPath = fileToPath("datasets\\pumsb.txt");
//		float minUtilThreshold = 0.012f;
//		int qrc = 5;
//		String output = "results\\pumsb\\output_pumsb.txt";


		// Run the algorithm
		// The combination method  (there are three possibilities )
		EnumCombination combinationmethod = EnumCombination.COMBINEALL;
//		EnumCombination combinationmethod = EnumCombination.COMBINEMIN;
//		EnumCombination combinationmethod = EnumCombination.COMBINEMAX;

		AlgoFHUQIMiner algo = new AlgoFHUQIMiner();
//		AlgoFHUQIMinerImproved algo = new AlgoFHUQIMinerImproved();
//		AlgoFHUQIMinerPSet algo = new AlgoFHUQIMinerPSet();
//		AlgoFHUQIMinerCombined algo = new AlgoFHUQIMinerCombined();
		algo.runAlgorithm(inputFileDBPath, inputFileProfitPath, minUtilThreshold, qrc, combinationmethod, output);
		algo.printStatistics();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException {
		URL url = MainTestFHUQIMiner.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}

}
