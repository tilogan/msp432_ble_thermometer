/*
 * Copyright (c) 2016 Shintako LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
