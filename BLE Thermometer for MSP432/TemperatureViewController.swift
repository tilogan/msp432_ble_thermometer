//
//  TemperatureViewController.swift
//  BLE Thermometer for MSP432
//
//  Created by Timothy Logan on 11/15/16.
//  Copyright © 2016 Shintako LLC. All rights reserved.
//
import CoreBluetooth
import UIKit
import Charts

class TemperatureViewController: UIViewController, CBPeripheralDelegate
{
    // MARK: Properties
    @IBOutlet weak var tempLabel: UILabel!
    @IBOutlet weak var tempChart: LineChartView!
    @IBOutlet weak var unitSwitch: UISwitch!
    @IBOutlet weak var enableSwitch: UISwitch!
    @IBOutlet weak var averageLabel: UILabel!
    @IBOutlet weak var maxLabel: UILabel!
    @IBOutlet weak var minLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var statisticsView: UIView!
    @IBOutlet weak var averageNameLabel: UILabel!
    @IBOutlet weak var maxNameLabel: UILabel!
    @IBOutlet weak var minNameLabel: UILabel!
    @IBOutlet weak var offOnLabel: UILabel!
    @IBOutlet weak var cfLabel: UILabel!
    
    
    // MARK: Variables
    let TEMP_SERVICE_UUID = CBUUID(string: "FFE0")
    let TEMP_CHARF_UUID = CBUUID(string: "FFE2")
    let TEMP_CHARC_UUID = CBUUID(string: "FFE1")
    var tempFChar: CBCharacteristic!
    var tempCChar: CBCharacteristic!
    var btDevice: CBPeripheral!
    let maxNumOfReadings = 15
    var curIndex = 0
    var lineChartDataSet: LineChartDataSet!
    var lineChartData: LineChartData!
    var tempDataEntries: [ChartDataEntry] = []
    var simulationTimer: Timer!
    var connectingCon: DiscoveryViewController!
    
    override func viewDidLoad()
    {
        super.viewDidLoad()
        
        tempChart.rightAxis.enabled = false
        tempChart.noDataText = "Reading Data..."
        tempChart.legend.enabled = false
        tempLabel.adjustsFontSizeToFitWidth = true

        /* Checking to see if we are in simulation mode */
        if(btDevice == nil)
        {
            simulationTimer = Timer.scheduledTimer(timeInterval: 0.5,
                                 target: self,
                                 selector: #selector(self.simulatedCallBack),
                                 userInfo: nil,
                                 repeats: true)
        }
        else
        {
            connectingCon = DiscoveryViewController(message: "Discovering Services...")
            present(connectingCon, animated: true, completion: discoverServicesShown)
        }
        
        titleLabel.adjustsFontSizeToFitWidth = true
        averageNameLabel.adjustsFontSizeToFitWidth = true
        maxNameLabel.adjustsFontSizeToFitWidth = true
        minNameLabel.adjustsFontSizeToFitWidth = true
        offOnLabel.adjustsFontSizeToFitWidth = true
        cfLabel.adjustsFontSizeToFitWidth = true
        statisticsView.layer.borderColor = UIColor.black.cgColor
        statisticsView.layer.borderWidth = 3.0

    }
    
    func discoverServicesShown()
    {
        btDevice.discoverServices(nil)
    }

    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
    }
    
    // MARK: Actions
    @IBAction func unitSwitchClicked(_ sender: UISwitch)
    {
        if(btDevice != nil)
        {
            if sender.isOn
            {
                btDevice.setNotifyValue(false, for: tempCChar)
                lineChartDataSet.values.removeAll()
                curIndex = 0
                btDevice.setNotifyValue(true, for: tempFChar)

            }
            else
            {
                btDevice.setNotifyValue(false, for: tempFChar)
                lineChartDataSet.values.removeAll()
                curIndex = 0
                btDevice.setNotifyValue(true, for: tempCChar)
            }
        }
        else
        {
                simulationTimer.invalidate()
                lineChartDataSet.values.removeAll()
                curIndex = 0
                simulationTimer = Timer.scheduledTimer(timeInterval: 0.5,
                                                       target: self,
                                                       selector: #selector(self.simulatedCallBack),
                                                       userInfo: nil,
                                                       repeats: true)
        }
    }
    
    @IBAction func enableDisableClicked(_ sender: UISwitch)
    {
        if(btDevice != nil)
        {
            if sender.isOn
            {
                unitSwitch.isEnabled = true
                
                if(unitSwitch.isOn)
                {
                    btDevice.setNotifyValue(true, for: tempFChar)
                }
                else
                {
                    btDevice.setNotifyValue(true, for: tempCChar)
                }
            }
            else
            {
                unitSwitch.isEnabled = false
                btDevice.setNotifyValue(false, for: tempFChar)
                btDevice.setNotifyValue(false, for: tempCChar)
            }
        }
        else
        {
            if sender.isOn
            {
                simulationTimer = Timer.scheduledTimer(timeInterval: 0.5,
                                                       target: self,
                                                       selector: #selector(self.simulatedCallBack),
                                                       userInfo: nil,
                                                       repeats: true)
                unitSwitch.isEnabled = true
            }
            else
            {
                simulationTimer.invalidate()
                unitSwitch.isEnabled = false
            }
        }
    }
    
    

    // MARK: CBPeripheralDelegate
    func peripheral(_ peripheral: CBPeripheral,
                    didDiscoverServices error: Error?)
    {
        for service in peripheral.services!
        {
            if service.uuid == TEMP_SERVICE_UUID
            {
                let thisService = service as CBService
                peripheral.discoverCharacteristics(nil, for: thisService)
                return
            }
        }
        
        print("ERROR: Thermometer - UUID Service was not found.")
        connectingCon.dismiss(animated: true, completion: deviceErrorDialog)
        
    }
    
    func peripheral(_ peripheral: CBPeripheral,
                    didDiscoverCharacteristicsFor service: CBService,
                    error: Error?)
    {

        for characteristic in service.characteristics!
        {
            if characteristic.uuid == TEMP_CHARF_UUID
            {
                tempFChar = characteristic
            }
            else if characteristic.uuid == TEMP_CHARC_UUID
            {
                tempCChar = characteristic
            }
            else
            {
                print("ERROR: Thermometer - Invalid characteristic on service")
                connectingCon.dismiss(animated: true, completion: deviceErrorDialog)
            }
            
            if tempFChar != nil && tempCChar != nil
            {
                btDevice.setNotifyValue(true, for: tempFChar)
                connectingCon.dismiss(animated: true, completion: nil)
            }
            
        }
    }
    
    /* Callback for a characteristic read/notification */
    func peripheral(_ peripheral: CBPeripheral,
                    didUpdateValueFor characteristic: CBCharacteristic,
                    error: Error?)
    {
        var values = [UInt8](repeating:0, count:characteristic.value!.count)
        var floatValue: Float = 0.0
        characteristic.value?.copyBytes(to: &values,
                                        count: characteristic.value!.count)
        memcpy(&floatValue, values, 4)
        let formattedFloat = String(format: "%.1f", floatValue)
        
        if characteristic.uuid == TEMP_CHARF_UUID
        {
            print("Temperature notification read: \(formattedFloat)°F")
            tempLabel.text =  "\(formattedFloat)°F"
            
        }
        else if characteristic.uuid == TEMP_CHARC_UUID
        {
            print("Temperature notification read: \(formattedFloat)°C")
            tempLabel.text =  "\(formattedFloat)°C"
        }
        else
        {
            print("Themometer: Invalid characteristic read (\(characteristic.uuid.uuidString))")
            return
        }
        
        if (curIndex == 0)
        {
            lineChartDataSet = LineChartDataSet(values: tempDataEntries,
                                                label: "Temperature")
            adjustChartFormat()
        }
        
        let dataEntry = ChartDataEntry(x: Double(curIndex),
                                       y: Double(floatValue))
        
        if(curIndex == (maxNumOfReadings-1))
        {
            _ = lineChartDataSet.removeFirst()
            
            for dataValue in lineChartDataSet.values
            {
                dataValue.x = dataValue.x - 1
            }
            
        }
        else
        {
            curIndex = curIndex + 1
        }
        
        _ = lineChartDataSet.addEntry(dataEntry)
        lineChartData.notifyDataChanged()
        tempChart.notifyDataSetChanged()
        
        calculateChartStatistics()
        
    }
    
    func calculateChartStatistics()
    {
        var curMin = lineChartDataSet.values[0].y
        var curMax = curMin
        var runningSum = 0.0
        
        for dataValue in lineChartDataSet.values
        {
            if(dataValue.y > curMax)
            {
                curMax = dataValue.y
            }
            
            if(dataValue.y < curMin)
            {
                curMin = dataValue.y
            }
            
            runningSum = runningSum + dataValue.y
        }
        
        let formattedMin = String(format: "%.1f", curMin)
        let formattedMax = String(format: "%.1f", curMax)
        let formattedAverage = String(format: "%.1f",
                                      (runningSum/(Double(curIndex))))
        
        maxLabel.text = formattedMax
        minLabel.text = formattedMin
        averageLabel.text = formattedAverage
    }
    
    /* Debug log for enabling/disabling notifications */
    func peripheral(_ peripheral: CBPeripheral,
                    didUpdateNotificationStateFor characteristic: CBCharacteristic,
                    error: Error?)
    {
        if(characteristic.isNotifying)
        {
            print("Thermometer: Notifications enabled for: \(characteristic.uuid.uuidString)")
        }
        else
        {
            print("Thermometer: Notifications disabled for: \(characteristic.uuid.uuidString)")
        }
    }
    
    func generateRandomDouble(firstNum: Double, secondNum: Double) -> Double
    {
        return Double(arc4random()) / Double(UINT32_MAX)
            * abs(firstNum - secondNum) + min(firstNum, secondNum)
    }
    
    /* Timer callback for simulation mode */
    func simulatedCallBack()
    {
        var randomTempValue: Double
        var formattedVal: String

        
        if(curIndex == 0)
        {
            lineChartDataSet = LineChartDataSet(values: tempDataEntries,
                                                label: "Temperature")
            adjustChartFormat()
        }
        
        if(unitSwitch.isOn)
        {
            randomTempValue = generateRandomDouble(firstNum: 70.0,
                                                       secondNum: 80.0)
            formattedVal = String(format: "%.1f°F", randomTempValue)
        }
        else
        {
            randomTempValue = generateRandomDouble(firstNum: 20.0,
                                                       secondNum: 30.0)
            formattedVal = String(format: "%.1f°C", randomTempValue)
        }
        
        tempLabel.text = formattedVal
        
        let dataEntry = ChartDataEntry(x: Double(curIndex),
                                       y: Double(randomTempValue))
        
        if(curIndex != (maxNumOfReadings - 1))
        {
            curIndex = curIndex + 1
        }
        else
        {
            _ = lineChartDataSet.removeFirst()
            
            for dataValue in lineChartDataSet.values
            {
                dataValue.x = dataValue.x - 1
            }
        }
        
        
        _ = lineChartDataSet.addEntry(dataEntry)
        lineChartData.notifyDataChanged()
        tempChart.notifyDataSetChanged()
        calculateChartStatistics()
    }
    
    /* Dialog for if the chosen device doesn't match our profile */
    func deviceErrorDialog()
    {
        let alert = UIAlertController(title: "Invalid Device",
                                      message: "Selected device does not contain the expected BLE services/characteristics for the thermometer device. Make sure correct firmware is programmed and the appropriate device is being selected.",
                                      preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: okSelected))
        self.present(alert, animated: true, completion: nil)
    }
    
    func okSelected(alert: UIAlertAction)
    {
        _ = self.navigationController?.popViewController(animated: true)
    }
    
    override func viewWillDisappear(_ animated: Bool)
    {
        if(connectingCon != nil)
        {
            connectingCon.dismiss(animated: true, completion: nil)
        }
        
        
    }
    
    func adjustChartFormat()
    {
        let gradientColors = [UIColor(colorLiteralRed: 0.5, green: 0.0, blue: 0.0, alpha: 0.7).cgColor,
                              UIColor(colorLiteralRed: 1.0, green: 0.0, blue: 0.0, alpha: 0.7).cgColor]
        let colorLocations: [CGFloat] = [1.0, 0.0]
        let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                  colors: gradientColors as CFArray,
                                  locations: colorLocations)
        lineChartDataSet.fill = Fill.fillWithLinearGradient(gradient!,
                                                            angle: 90.0)
        lineChartDataSet.setCircleColor(UIColor.black)
        lineChartDataSet.circleRadius = 4.0
        lineChartDataSet.drawCircleHoleEnabled = false
        lineChartDataSet.fillAlpha = 1.0
        lineChartDataSet.drawFilledEnabled = true
        lineChartData = LineChartData()
        lineChartData.addDataSet(lineChartDataSet)
        tempChart.data = lineChartData
        
        /* Adjusting the Margins */
        let yAxis = tempChart.leftAxis
        
        if(unitSwitch.isOn)
        {
            yAxis.axisMaximum = 100
            yAxis.axisMinimum = 30
        }
        else
        {
            yAxis.axisMaximum = 42
            yAxis.axisMinimum = -5
        }
        
        let xAxis = tempChart.xAxis
        xAxis.enabled = false
        
        if btDevice != nil
        {
            tempChart.chartDescription?.text = "Temperature Data from MSP432"
        }
        else
        {
            tempChart.chartDescription?.text = "SIMULATION MODE ONLY!"
        }
        
    }
}
