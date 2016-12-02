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
import CoreBluetooth

class BLEDeviceTableViewController: UITableViewController,
    CBCentralManagerDelegate
{
    // MARK: Properties:
    @IBOutlet var deviceTable: UITableView!
    
    // MARK: Variables
    var btManager: CBCentralManager!
    var btDevices: [CBPeripheral]! = []
    var selBTDev: CBPeripheral!
    var rssiReadings: [NSNumber]! = []
    let identifier = "BLEDeviceTableViewCell"
    var simulationMode = false
    var connectionCon: DiscoveryViewController!
    var timeoutTimer: Timer!
    
    override func viewDidLoad()
    {
        super.viewDidLoad()
        let newIndexPath = IndexPath(row: 0, section: 0)
        tableView.insertRows(at: [newIndexPath], with: .bottom)
        btManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: - Table view data source
    override func numberOfSections(in tableView: UITableView) -> Int
    {
        return 1
    }
    
    override func tableView(_ tableView: UITableView,
                            numberOfRowsInSection section: Int) -> Int
    {
        return btDevices.count + 1
    }
    
    override func tableView(_ tableView: UITableView,
                    cellForRowAt indexPath: IndexPath) -> UITableViewCell
    {
        let cell: BLEDeviceTableViewCell =
            tableView.dequeueReusableCell(withIdentifier: identifier, for:
                                        indexPath) as! BLEDeviceTableViewCell
        
        if indexPath.row == 0
        {
            cell.deviceText.text = "*** Simulation Mode ***"
            cell.setSignalStrengthImage(rssi: 100.0)
        }
        else
        {
            let device = btDevices[indexPath.row - 1]
            let devName = device.name
            
            if devName != nil
            {
                cell.deviceText.text = device.name
            }
            else
            {
                cell.deviceText.text = "Unnamed Device"
            }
            
            cell.setSignalStrengthImage(rssi: rssiReadings[indexPath.row - 1].floatValue)
        }
        
        return cell
    }
    
    override func tableView(_ tableView: UITableView,
                            didSelectRowAt indexPath: IndexPath)
    {
        if(indexPath.row != 0)
        {
            selBTDev = btDevices[indexPath.row-1]
            connectionCon = DiscoveryViewController(message: "Connecting...")
            present(connectionCon, animated: true, completion: nil)
            
            timeoutTimer = Timer.scheduledTimer(timeInterval: 5.0,
                                                   target: self,
                                                   selector: #selector(self.completionTimeout),
                                                   userInfo: nil,
                                                   repeats: false)
            
            btManager.connect(selBTDev, options: nil)
        }
        else
        {
            let alert = UIAlertController(title: "Simulation Mode",
                                          message: "Note that simulation mode does not acutally connect to a BLE device and provides random data purely for the sake of demonstration. \nContinue?",
                                          preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "Yes", style: UIAlertActionStyle.default, handler: continueWithSimulation))
            alert.addAction(UIAlertAction(title: "No", style: UIAlertActionStyle.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
            
        }
    }
    
    func continueWithSimulation(action: UIAlertAction)
    {
        simulationMode = true
        self.performSegue(withIdentifier: "gotoTemperature", sender: self)
    }
    
    // MARK: CBCentralManagerDelegate
    func centralManagerDidUpdateState(_ central: CBCentralManager)
    {
        if central.state == .poweredOn
        {
            central.scanForPeripherals(withServices: nil, options: nil)
        }
        else
        {
            let alert = UIAlertController(title: "Bluetooth Disabled",
                                          message: "Bluetooth is currently disabled on your device. Only simulation mode will be available. Enable Bluetooth from the system settings to connect to a BLE device.",
                                          preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    func centralManager(_ central: CBCentralManager,
                        didDiscover peripheral: CBPeripheral,
                        advertisementData: [String : Any],
                        rssi RSSI: NSNumber)
    {
        if(!btDevices.contains(peripheral))
        {
            let newIndexPath = IndexPath(row: btDevices.count+1, section: 0)
            btDevices.append(peripheral)
            rssiReadings.append(RSSI)
            tableView.insertRows(at: [newIndexPath], with: .bottom)
        }
    }
    
    func centralManager(_ central: CBCentralManager,
                        didConnect peripheral: CBPeripheral)
    {
        connectionCon.dismiss(animated: true, completion: nil)
        timeoutTimer.invalidate()
        self.performSegue(withIdentifier: "gotoTemperature", sender: self)
    }
    
    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?)
    {
        if navigationController?.topViewController != self
        {
            let alert = UIAlertController(title: "Disconnected",
                                          message: "Device was disconnected",
                                          preferredStyle: UIAlertControllerStyle.alert)
            alert.addAction(UIAlertAction(title: "OK",
                                          style: UIAlertActionStyle.default,
                                          handler: completionDisconnect))
            present(alert, animated: true, completion: nil)
        }
        
    }
    
    func completionDisconnect(action: UIAlertAction)
    {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    func completionTimeout()
    {
        btManager.cancelPeripheralConnection(selBTDev)
        connectionCon.dismiss(animated: true, completion: nil)
        
        let alert = UIAlertController(title: "Connection Timeout",
                                      message: "Timeout occurred while connecting to device.",
                                      preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "OK",
                                      style: UIAlertActionStyle.default,
                                      handler: nil))
        present(alert, animated: true, completion: nil)
    }
    
    // MARK: - Navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?)
    {
        if segue.identifier == "gotoTemperature"
        {
            let thermViewCon: TemperatureViewController =
                segue.destination as! TemperatureViewController
            
            if(simulationMode == true)
            {
                thermViewCon.btDevice = nil
            }
            else
            {
                thermViewCon.btDevice = selBTDev
                selBTDev.delegate = thermViewCon
                btManager.stopScan()
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool)
    {
        btDevices.removeAll()
        rssiReadings.removeAll()
        tableView.reloadData()
        simulationMode = false
        
        if(btManager.state == .poweredOn)
        {
            btManager.scanForPeripherals(withServices: nil, options: nil)
        }
        
        if(selBTDev != nil && selBTDev.state == .connected)
        {
            btManager.cancelPeripheralConnection(selBTDev)
        }
        
        super.viewWillAppear(animated)
        
    }
}
