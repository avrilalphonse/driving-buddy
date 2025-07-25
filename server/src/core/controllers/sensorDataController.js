import fs from 'fs';
import csv from 'csv-parser';
import SensorData from '../models/sensorDataModel.js';

async function importCsvToMongoDB(csvPath) {
  const results = [];
  return new Promise((resolve, reject) => {
    fs.createReadStream(csvPath)
      .pipe(csv())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        const docs = results.map(row => {
          const speed = parseFloat(row.Speed);
          const acceleration = parseFloat(row.Acceleration);
          const rpm = parseFloat(row.RPM);
          const engine_load = parseFloat(row['Engine Load']);
          // const steering_angle_deg = parseFloat(row['Steering Angle']);

          // flags (independent)
          const hard_braking = acceleration <= -3.0;
          // const sharp_turn = Math.abs(steering_angle_deg) >= 25;
          const inconsistent_speed = acceleration >= 3.0 && rpm >= 3500;
          // const lane_deviation = ...; // uncomment when logic figured out

          return {
            speed,
            acceleration,
            rpm,
            engine_load,
            // steering_angle_deg, 
            hard_braking,
            // sharp_turn,
            inconsistent_speed
            // lane_deviation
          };
        });
        await SensorData.insertMany(docs);
        resolve();
      })
      .on('error', reject);
  });
}

// helper function for day suffixes!
function getDaySuffix(day) {
    if (day >= 11 && day <= 13) {
        return 'th';
    }
    switch (day % 10) {
        case 1: return 'st';
        case 2: return 'nd';
        case 3: return 'rd';
        default: return 'th';
    }
}

export const getBucketedData = async (req, res) => {
    try {
        const { windowSeconds = 10 } = req.query;

        console.log('Fetching data separated by drives...');
        
        // Get all unique tripIDs (drives)
        const tripIDs = await SensorData.distinct('tripID');
        console.log(`Found ${tripIDs.length} drives:`, tripIDs);
        
        const driveData = [];
        
        for (const tripID of tripIDs) {
            // Get hard braking incidents for this drive
            const hardBrakingData = await SensorData.find({ 
                tripID: tripID,
                hard_braking: true 
            }).sort({ timestamp: 1 }).lean();
            
            // Get inconsistent speed incidents for this drive
            const inconsistentSpeedData = await SensorData.find({ 
                tripID: tripID,
                inconsistent_speed: true 
            }).sort({ timestamp: 1 }).lean();
            
            // Get lane deviation incidents for this drive (when lane_offset_direction is left or right, not centre)
            let laneDeviationData = await SensorData.find({ 
                tripID: tripID,
                lane_offset_direction: { 
                    $exists: true, 
                    $ne: null,
                    $nin: [" centre", "centre", " center", "center"] // Exclude centre/center variations
                }
            }).sort({ timestamp: 1 }).lean();
            
            // Check if lane deviation count is above 50, then set count to 0
            const originalLaneDeviationCount = laneDeviationData.length;
            let laneDeviationCount = originalLaneDeviationCount;
            if (originalLaneDeviationCount > 50) {
                laneDeviationCount = 0;
            }
            
            // Get trip date from tripID (format: DDMMYYHHMMSS)
            const dateStr = tripID.substring(0, 6);
            const day = parseInt(dateStr.substring(0, 2));
            const month = parseInt(dateStr.substring(2, 4));
            const year = 2000 + parseInt(dateStr.substring(4, 6));
            const driveDate = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
            
            driveData.push({
                tripID: tripID,
                date: driveDate,
                displayDate: `July ${day}${getDaySuffix(day)}, 2025 Drive`,
                incidents: {
                    sudden_braking: hardBrakingData.length,
                    inconsistent_speed: inconsistentSpeedData.length,
                    lane_deviation: laneDeviationCount
                },
                raw_data: {
                    sudden_braking: hardBrakingData,
                    inconsistent_speed: inconsistentSpeedData,
                    lane_deviation: laneDeviationData
                }
            });
            
            console.log(`Drive ${tripID} (${driveDate}): ${hardBrakingData.length} hard braking, ${inconsistentSpeedData.length} inconsistent speed, ${laneDeviationCount} lane deviations`);
        }
        
        // Sort drives by date (newest first)
        driveData.sort((a, b) => b.tripID.localeCompare(a.tripID));
        
        // Calculate totals
        const totals = {
            sudden_braking: driveData.reduce((sum, drive) => sum + drive.incidents.sudden_braking, 0),
            inconsistent_speed: driveData.reduce((sum, drive) => sum + drive.incidents.inconsistent_speed, 0),
            lane_deviation: driveData.reduce((sum, drive) => sum + drive.incidents.lane_deviation, 0)
        };
        totals.total = totals.sudden_braking + totals.inconsistent_speed + totals.lane_deviation;
        
        res.json({
            drives: driveData,
            summary: {
                total_drives: driveData.length,
                ...totals
            }
        });
        
    } catch (error) {
        console.error('Error fetching drive data:', error);
        res.status(500).json({ error: 'Failed to fetch drive data', details: error.message });
    }
};
export { importCsvToMongoDB };

