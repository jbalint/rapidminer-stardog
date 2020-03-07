/*
 * Copyright 2020 Jess Balint
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jbalint.rapidminer.stardog.stardog;

import com.rapidminer.gui.MainFrame;


/**
 * This class provides hooks for initialization and its methods are called via reflection by
 * RapidMiner Studio. Without this class and its predefined methods, an extension will not be
 * loaded.
 *
 * @author REPLACEME
 */
public final class PluginInitStardog {

		private PluginInitStardog() {
				// Utility class constructor
		}

		/**
		 * This method will be called directly after the extension is initialized. This is the first
		 * hook during start up. No initialization of the operators or renderers has taken place when
		 * this is called.
		 */
		public static void initPlugin() {}

		/**
		 * This method is called during start up as the second hook. It is called before the gui of the
		 * mainframe is created. The Mainframe is given to adapt the gui. The operators and renderers
		 * have been registered in the meanwhile.
		 *
		 * @param mainframe
		 *            the RapidMiner Studio {@link MainFrame}.
		 */
		public static void initGui(MainFrame mainframe) {}

		/**
		 * The last hook before the splash screen is closed. Third in the row.
		 */
		public static void initFinalChecks() {}

		/**
		 * Will be called as fourth method, directly before the UpdateManager is used for checking
		 * updates. Location for exchanging the UpdateManager. The name of this method unfortunately is
		 * a result of a historical typo, so it's a little bit misleading.
		 */
		public static void initPluginManager() {}
}
