package com.deri.l2s.main;

/*
 *  Linked2Safety SPARQL Endpoint Discovery and Linking Framework
 *  
 *  Copyright (C) 2014 Muntazir Mehdi <muntazir.mehdi@insight-centre.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.util.Scanner;

import com.deri.l2s.controllers.MainController;

public class L2SMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MainController mController = new MainController();
		Scanner in = new Scanner(System.in);
		System.out.println("Collect Results: Press 1");
		System.out.println("Collect Variables: Press 2");
		System.out.println("Collect Links: Press 3");
		System.out.println("Break Variables: Press 4");
		int selection = in.nextInt();
		switch(selection){
		case 1:
//			mController.collectResultsWithTimeout(30);
			mController.collectResults();
			break;
		case 2:
			mController.collectVariables();
//			System.out.println("Enable Code");
			break;
		case 3:
			mController.collectLinks();
			break;
		case 4:
			mController.breakVariables();
			break;
		default:
			System.out.println("Invalid Choise!");
			System.exit(0);
			break;
		}
	}
}
