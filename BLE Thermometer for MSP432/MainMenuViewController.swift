//
//  MainMenuViewController.swift
//  BLE Thermometer for MSP432
//
//  Created by Timothy Logan on 11/15/16.
//  Copyright © 2016 Shintako LLC. All rights reserved.
//

import UIKit

class MainMenuViewController: UIViewController
{

    override func viewDidLoad()
    {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: Actions
    @IBAction func helpPressed(_ sender: UIButton)
    {
        UIApplication.shared.openURL(URL(string: "http://msp432ble.shintako.com")!)
    }

    @IBAction func shintakoPressed(_ sender: UIButton)
    {
        UIApplication.shared.openURL(URL(string: "http://www.shintako.com")!)
    }
    
    

    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?)
    {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    
    override func viewWillAppear(_ animated: Bool)
    {
        self.navigationController?.setNavigationBarHidden(true,
                                                          animated: false)
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool)
    {
        self.navigationController?.setNavigationBarHidden(false,
                                                          animated: false)
        super.viewWillDisappear(animated)
    }

}
