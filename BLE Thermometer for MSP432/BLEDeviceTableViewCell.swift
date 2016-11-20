//
//  BLEDeviceTableViewCell.swift
//  msp432_ble_plugin
//
//  Created by Timothy Logan on 10/21/16.
//  Copyright © 2016 Shintako LLC. All rights reserved.
//

import UIKit

class BLEDeviceTableViewCell: UITableViewCell
{
    // MARK: Properties
    @IBOutlet weak var deviceText: UILabel!
    @IBOutlet weak var signalImage: UIImageView!
   
    
    override func awakeFromNib()
    {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool)
    {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
    // MARK: Functions
    func setSignalStrengthImage(rssi: Float)
    {
        let sigStrength = (rssi + 100) * 2
        
        if(sigStrength < 16.6)
        {
            signalImage.image = UIImage(named: "signal1")
        }
        else if(sigStrength < 33.3)
        {
            signalImage.image = UIImage(named: "signal2")
        }
        else if(sigStrength < 49.93)
        {
            signalImage.image = UIImage(named: "signal3")
        }
        else if(sigStrength < 66.53)
        {
            signalImage.image = UIImage(named: "signal4")
        }
        else if(sigStrength < 83.13)
        {
            signalImage.image = UIImage(named: "signal5")
        }
        else
        {
            signalImage.image = UIImage(named: "signal6")
        }
    }
    
}
